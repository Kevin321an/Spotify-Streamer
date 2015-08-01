package com.example.android.spotifystreamer.apk.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.android.spotifystreamer.apk.MainActivityFragment;
import com.example.android.spotifystreamer.apk.MusicData;

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
 * Created by FM on 7/31/2015.
 */
public class Json  extends IntentService {
    private boolean dataIsNull = false;
    private final String LOG_TAG = Json.class.getSimpleName();

    public static final String ARTIST_LIST="RL";
    public static final String ARTIST = "lqe";
    private static Bundle artistArray=new Bundle();
    public static  Bundle getArtistArray(){
        return artistArray;
    }
    private static boolean callBack;
    public static boolean isCallBack(){ return callBack;}
    @Override
    protected void onHandleIntent(Intent intent) {
        String artist = intent.getStringExtra(ARTIST);

            //protected ArrayList<MusicData> doInBackground(String... params) {

                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                // Will contain the raw JSON response as a string.
                String artistJsonStr = null;

                final String ARTIST_BASE_URL = "https://api.spotify.com/v1/search?";
                final String QUERY_PARAM = "q";
                final String TYPE_PARAM = "type";
                //curl -X GET "https://api.spotify.com/v1/search?q=tania*&type=artist" -H "Accept: application/json"
                try {
                    final String ARTIST_TYPE_PARAM = "artist";
                    //URL url= new URL("https://api.spotify.com/v1/search?q=tania*&type=artist");
                    Uri builtUri = Uri.parse(ARTIST_BASE_URL).buildUpon()
                            .appendQueryParameter(QUERY_PARAM, artist)
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
                        return;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");

                    }
                    if (buffer.length() == 0) {
                        return;

                    }
                    artistJsonStr = buffer.toString();
                    //Log.v(LOG_TAG, "artisList JSON String"+artistJsonStr);

                } catch (IOException e) {
                    Log.e("LOG_TAG", "Error " + e);
                    return;

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
                    getArtistDataFromJson(artistJsonStr);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                return;
            }

            private void getArtistDataFromJson(String musicJsonStr)
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

                //output the  the formated com.example.android.spotifystreamer.apk.data
                for (MusicData s : music) {
                    Log.v(LOG_TAG, "Main entry" + s);
                }

                artistArray.putParcelableArrayList(ARTIST_LIST,music);
                callBack=!callBack;
            }


 public Json(){ super("SpotifyStreamer");}


    static public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, MainActivityFragment.class);
            sendIntent.putExtra(Json.ARTIST, intent.getStringExtra(Json.ARTIST));
            context.startService(sendIntent);
        }
    }

}
