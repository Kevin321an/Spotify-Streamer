package com.example.android.spotifystreamer.apk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
public class MainActivityFragment extends Fragment {

    private static final String SAVE_PAGE_KEY = "save_page";
    final String ARTIST_BASE_URL = "https://api.spotify.com/v1/search?";
    final String QUERY_PARAM = "q";
    final String TYPE_PARAM = "type";
    EditText searchBar;
    private boolean dataIsNull = false;
    private ListView listView;//store the current list.
    private static String str="";//store the searching keyword
    private ArrayList<MusicData> mListInstanceState;
    //SearchView searchBar;

    private MainAdapter mArtistListAdapter;

    public interface Callback{
        public void onItemSelected(MusicData trackList);
    }
    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events

        if (savedInstanceState != null) {
            mListInstanceState = savedInstanceState.getParcelableArrayList(SAVE_PAGE_KEY);
        } else {
            mListInstanceState = new ArrayList<MusicData>();
        }

        setHasOptionsMenu(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        /*if (id == R.id.action_refresh) {
        int id = item.getItemId();
            updateArtistList();
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mArtistListAdapter = new MainAdapter(getActivity(), R.layout.list_item_artist_textview, mListInstanceState);

        listView = (ListView) rootView.findViewById(R.id.listview_artist);
        listView.setAdapter(mArtistListAdapter); //shoot the ArrayAdapter on to Screen
        View emptyView = rootView.findViewById(R.id.listview_music_empty);
        listView.setEmptyView(emptyView);
        //if(mListInstanceState!=null){
        //.onRestoreInstanceState(mListInstanceState);
        //setListAdapter(new ArrayAdapter<MusicData>(this, android.R.layout.listview_artist, list));
        //}
        //Listener for searchBar
        /*
        searchBar=(SearchView)rootView.findViewById(R.id.artists_search_bar);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                performSearch(query);
                return true;

            }
        });
        */
        //Listener for EditText
        searchBar = (EditText) rootView.findViewById(R.id.artists_search_bar);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean compareS=str.equals(s.toString().trim());

                if (!s.toString().isEmpty()&&!compareS) {
                    performSearch(s.toString());
                }
                str=s.toString().trim();


                if (dataIsNull && s.length() > 3) {
                    final String NO_RESULT = getString(R.string.no_result_feedback);
                    Toast.makeText(getActivity(), NO_RESULT, Toast.LENGTH_SHORT).show();
                    dataIsNull = false;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            private void performSearch(String search) {
                FetchArtistTask artistTask = new FetchArtistTask();
                artistTask.execute(search);
                updateEmptyView();
            }
        });
        //listener for listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                MusicData music = mArtistListAdapter.getItem(position);
                //Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                /*Intent showDetail = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("Object", music);
                startActivity(showDetail);*/

                ((Callback) getActivity()).onItemSelected(music);
                //Reference
                //http://developer.android.com/guide/components/intents-filters.html#ExampleExplicit
            }
        });
        return rootView;
    }

    //Save the ListView state on onSaveInstanceState:
    @Override
    public void onSaveInstanceState(Bundle outState) {

        //outState.putParcelable(SAVE_PAGE_KEY, listView.onSaveInstanceState());
        outState.putParcelableArrayList(SAVE_PAGE_KEY, mListInstanceState);
        super.onSaveInstanceState(outState);
        System.out.print("onSaveInstance");
    }

    public class FetchArtistTask extends AsyncTask<String, Void, ArrayList<MusicData>> {
        private final String LOG_TAG = FetchArtistTask.class.getSimpleName();


        protected ArrayList<MusicData> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String artistJsonStr = null;

            //curl -X GET "https://api.spotify.com/v1/search?q=tania*&type=artist" -H "Accept: application/json"
            try {
                final String ARTIST_TYPE_PARAM = "artist";
                //URL url= new URL("https://api.spotify.com/v1/search?q=tania*&type=artist");
                Uri builtUri = Uri.parse(ARTIST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(TYPE_PARAM, ARTIST_TYPE_PARAM)
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
                return getArtistDataFromJson(artistJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
        private ArrayList<MusicData> getArtistDataFromJson(String musicJsonStr)
                throws JSONException {
            ArrayList<MusicData> music;

            //Log.v(LOG_TAG, "artisList JSON String"+musicJsonStr);

            // These are the names of the JSON objects that need to be extracted.
            final String SPOTIFY_ITEMS = "items";
            final String SPOTIFY_ID = "id";
            final String SPOTIFY_IMAGE = "images";
            final String SPOTIFY_ARITIS = "artists";
            JSONObject musicJson = new JSONObject(musicJsonStr).getJSONObject(SPOTIFY_ARITIS);
            JSONArray musicArray = musicJson.getJSONArray(SPOTIFY_ITEMS);
            //Log.v(LOG_TAG, "artisList JSON String"+musicArray.toString());
            int numberOfAritist = musicArray.length();
            if (numberOfAritist == 0) {
                dataIsNull = true;
            }

            music = new ArrayList<MusicData>();
            for (int i = 0; i < numberOfAritist; i++) {
                final String OWM_NAME = "name";
                final String OWM_URL = "url";
                final int SMALL_IMG_SEQUENCE = 2;
                final int Big_IMG_SEQUENCE = 0;
                String images64UrlS,images640UrlS;
                // Get the JSON object representing the day
                JSONObject artistObject = musicArray.getJSONObject(i);
                String id = artistObject.getString(SPOTIFY_ID);
                //JSONObject images= artistObject.getJSONArray(SPOTIFY_IMAGE).getJSONObject(SMALL_IMG_SEQUENCE);
                JSONArray images = artistObject.getJSONArray(SPOTIFY_IMAGE);

                if (images.length() > 0) {
                    images64UrlS = images.getJSONObject(SMALL_IMG_SEQUENCE).getString(OWM_URL);
                } else {
                    images64UrlS = "";
                }
                if (images.length() > 0) {
                    images640UrlS = images.getJSONObject(Big_IMG_SEQUENCE).getString(OWM_URL);
                } else {
                    images640UrlS = "";
                }

                String artistName = artistObject.getString(OWM_NAME);
                music.add(new MusicData(artistName, images64UrlS,images640UrlS, id));

            }

            //output the  the formated data
            for (MusicData s : music) {
                Log.v(LOG_TAG, "Main entry" + s);
            }
            return music;
        }



        protected void onPostExecute(ArrayList<MusicData> result) {
            if (result != null) {

                mArtistListAdapter.clear();
                mArtistListAdapter.addAll(result);
            }

        }
    }
    //check the internet connection
    /*private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");

            if (ipAddr.equals("")) {
                return false;
            } else {return true;}
        } catch (Exception e) {
            return false;
        }

    }
    *
    * */


    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    /*
      Updates the empty list view with contextually relevant information that the user can
        use to determine why they aren't seeing weather.
    */
    private void updateEmptyView() {
        if ( mArtistListAdapter.getCount() == 0 ) {
            TextView tv = (TextView) getView().findViewById(R.id.listview_music_empty);
            if ( null != tv ) {
                // if cursor is empty, why? do we have an invalid location
                int message = R.string.empty_music_list;
                if (!isNetworkAvailable(getActivity()) ) {
                    message = R.string.empty_music_list_no_network;
                }
                tv.setText(message);
            }
        }
    }
}



