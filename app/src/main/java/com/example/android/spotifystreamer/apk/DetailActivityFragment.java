package com.example.android.spotifystreamer.apk;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private MusicData music;
    private String musicID;
    private MainAdapter mDetailAdapter;
    private String artist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("Object")) {
            //music = intent.getStringExtra(Intent.EXTRA_TEXT);
            music = (MusicData) intent.getSerializableExtra("Object");
            //((TextView) rootView.findViewById(R.id.detail_text))
            // .setText(mForecastStr);
        }
        musicID = music.id.id;
        artist=music.artist;
        FetchTrackTask trackTask = new FetchTrackTask();
        trackTask.execute(music.id.id);
        mDetailAdapter = new MainAdapter(getActivity(), R.layout.list_item_artist_ablum, new ArrayList<MusicData>());
        //mArtistListAdapter=new ArrayAdapter<String>
        //      (getActivity(), R.layout.list_item_artist_textview,
        //             R.id.list_item_artist_textview, listArtist);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_detail);
        listView.setAdapter(mDetailAdapter); //shoot the ArrayAdapter on to Screen


        //listener for listview and sent intent to mediaPlayer
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                MusicData music = mDetailAdapter.getItem(position);

                Intent mediaPlayer = new Intent(getActivity(), MusicPlay.class)
                        .putExtra("Object", music)
                        .putExtra(Intent.EXTRA_TEXT, artist);
                startActivity(mediaPlayer);
                //Reference
                //http://developer.android.com/guide/components/intents-filters.html#ExampleExplicit
            }
        });


        return rootView;
    }

    public class FetchTrackTask extends AsyncTask<String, Void, ArrayList<MusicData>> {
        private final String LOG_TAG = FetchTrackTask.class.getSimpleName();
        protected ArrayList<MusicData> doInBackground(String... params) {

            /*
            if(params.length==0){
                return null;
            }
            */
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String artistJsonStr = null;

            //            GET https://api.spotify.com/v1/artists/{id}/top-tracks
            try {
                final String ARTIST_BASE_URL = "https://api.spotify.com/v1/artists/";
                final String QUERY_PARAM = "q";
                final String TYPE_PARAM = "top-tracks";
                final String COUNTRY_PARAM = "?country=SE";
                //URL url= new URL("https://api.spotify.com/v1/search?q=tania*&type=artist");
//                 "https://api.spotify.com/v1/artists/43ZHCT0cAZBISjO8DG9PnE/top-tracks?country=SE" -H
                Uri builtUri = Uri.parse(ARTIST_BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendEncodedPath(TYPE_PARAM)
                        .appendEncodedPath(COUNTRY_PARAM)
                        .build();
                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI" + builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");

                }
                if (buffer.length() == 0) {
                    return null;

                }
                artistJsonStr = buffer.toString();
                //Log.v(LOG_TAG, "artisList JSON String"+artistJsonStr);

            } catch (IOException e) {
                Log.e("LOG_TAG", "Error " + e);
                return null;

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("LOG_TAG", "Error closing stream", e);
                    }
                }
            }
            try {
                return getTrackDataFromJson(artistJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;


        }
        private ArrayList<MusicData> getTrackDataFromJson(String musicJsonStr)
                throws JSONException {
            ArrayList<MusicData> music;
            String trackName;
            String albumName;
            String albumImage640;
            String albumImage300;
            String previewUrl;
            int NUMBER_OF_DISPLAY_TRACK = 10;

            //Log.v(LOG_TAG, "artisList JSON String"+musicJsonStr);

            // These are the names of the JSON objects that need to be extracted.
            final String SPOTIFY_TRACKS = "tracks";
            final String SPOTIFY_ID = "id";
            final String SPOTIFY_IMAGE = "images";
            JSONObject musicJson = new JSONObject(musicJsonStr);
            JSONArray musicArray = musicJson.getJSONArray(SPOTIFY_TRACKS);
            //Log.v(LOG_TAG, "artisList JSON String"+musicArray.toString());
            int numberOfAritist = musicArray.length();

            if (numberOfAritist < NUMBER_OF_DISPLAY_TRACK) {
                NUMBER_OF_DISPLAY_TRACK = numberOfAritist;    //by any chance if top tracks are less than 10 will result at out of boundary
            }

            music = new ArrayList<MusicData>();

            for (int i = 0; i < NUMBER_OF_DISPLAY_TRACK; i++) {
                final String OWM_ALBUM = "album";
                final String OWM_URL = "url";
                final String OWM_NAME = "name";
                final String OWM_PREVIEWURL = "preview_url";

                final int ALBUMIMAGE640PX_SEQUENCE = 0;
                final int ALBUMIMAGE300PX_SEQUENCE = 1;

                // Get the JSON object representing the day
                JSONObject artistObject = musicArray.getJSONObject(i);
                trackName = artistObject.getString(OWM_NAME);
                previewUrl = artistObject.getString(OWM_PREVIEWURL);

                JSONObject album = artistObject.getJSONObject(OWM_ALBUM);
                albumName = album.getString(OWM_NAME);


                //JSONObject images= artistObject.getJSONArray(SPOTIFY_IMAGE).getJSONObject(SMALL_IMG_SEQUENCE);
                JSONArray images = album.getJSONArray(SPOTIFY_IMAGE);
                if (images.length() > 0) {
                    JSONObject image640 = images.getJSONObject(ALBUMIMAGE640PX_SEQUENCE);
                    if (image640.getString(OWM_URL) != null) {
                        albumImage640 = image640.getString(OWM_URL);
                    } else {
                        albumImage640 = "";

                    }
                    JSONObject image300 = images.getJSONObject(ALBUMIMAGE300PX_SEQUENCE);
                    if (image300.getString(OWM_URL) != null) {
                        albumImage300 = image300.getString(OWM_URL);
                    } else {
                        albumImage300 = "";
                    }
                } else {
                    //TO do add a pic holder
                    albumImage640 = "";
                    albumImage300 = "";
                }
                // music.add(new MusicData(artistName, imagesUrlS, id));
                music.add(new MusicData(musicID, trackName, albumName, albumImage640, albumImage300, previewUrl));

            }
            //output the  the formated data
            for (MusicData s : music) {
                Log.v(LOG_TAG, "Detail entry: " + s);
            }
            return music;

        }
        protected void onPostExecute(ArrayList<MusicData> result) {
            if (result != null) {
                mDetailAdapter.clear();
                mDetailAdapter.addAll(result);

                //for (String Str : result) {
                //   mArtistListAdapter.add(Str);
                //}
            }

        }
    }
}
