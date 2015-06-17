package com.example.android.spotifystreamer.apk;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    //private ArrayAdapter<String> mArtistListAdapter;
    private MainAdapter mArtistListAdapter;
    String[] artistName={"fdlajsfd","dflasdjf"};

    String[] id;
    String[] imagesUrlS={"https://i.scdn.co/image/f444d668bfa1a057e1759c6266f8fbf471eb6c04", "https://i.scdn.co/image/48c420405d19d09381d5541d239a9b7ae9bd3ed8"};


    /*
    @Override
    public void onStart(){
        super.onStart();
        updateArtistList();
    }
    */

    public MainActivityFragment() {
    }

    private void updateArtistList(){
        FetchArtistTask artistTask= new FetchArtistTask();
        artistTask.execute("tania");
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
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
            int id = item.getItemId();
            if (id == R.id.action_refresh) {
                updateArtistList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_main, container, false);
        String[] data = {
                "Mon 6/23?- Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        List<String> listArtist = new ArrayList<String>(Arrays.asList(data));
        mArtistListAdapter=new MainAdapter(getActivity(),artistName,imagesUrlS);
        //mArtistListAdapter=new ArrayAdapter<String>
          //      (getActivity(), R.layout.list_item_artist_textview,
           //             R.id.list_item_artist_textview, listArtist);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_artist);
        listView.setAdapter(mArtistListAdapter); //shoot the ArrayAdapter on to Screen

        return rootView;
    }


    public class FetchArtistTask extends AsyncTask<String, Void, String[]>{
        private final String LOG_TAG =FetchArtistTask.class.getSimpleName();



        protected String[] doInBackground(String... params) {
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

            //curl -X GET "https://api.spotify.com/v1/search?q=tania*&type=artist" -H "Accept: application/json"
            try{

                final String ARTIST_BASE_URL="https://api.spotify.com/v1/search?";
                final String QUERY_PARAM = "q";
                final String TYPE_PARAM="type";
                final String ARTIST_TYPE_PARAM="artist";
                //URL url= new URL("https://api.spotify.com/v1/search?q=tania*&type=artist");
                Uri builtUri = Uri.parse(ARTIST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(TYPE_PARAM, ARTIST_TYPE_PARAM)
                        .build();
                URL url=new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI"+ builtUri.toString());
                urlConnection =(HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream=urlConnection.getInputStream();
                StringBuffer buffer= new StringBuffer();
                if (inputStream==null){
                    return null;
                }
                reader=new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line=reader.readLine()) !=null){
                    buffer.append(line+"\n");

                }
                if (buffer.length()==0){
                    return null;

                }
                artistJsonStr=buffer.toString();
                //Log.v(LOG_TAG, "artisList JSON String"+artistJsonStr);

            }catch (IOException e){
                Log.e("LOG_TAG", "Error "+e);
                return null;

            }finally {
                if (urlConnection!=null){
                    urlConnection.disconnect();
                }
                if (reader!=null){
                    try{
                        reader.close();
                    }
                    catch (final IOException e ){
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



        private String[] getArtistDataFromJson(String musicJsonStr)
                throws JSONException {

            //Log.v(LOG_TAG, "artisList JSON String"+musicJsonStr);

            // These are the names of the JSON objects that need to be extracted.
            final String SPOTIFY_ITEMS = "items";
            final String SPOTIFY_ID = "id";
            final String SPOTIFY_IMAGE="images";
            final String SPOTIFY_ARITIS="artists";
            JSONObject musicJson = new JSONObject(musicJsonStr).getJSONObject(SPOTIFY_ARITIS);
            JSONArray musicArray = musicJson.getJSONArray(SPOTIFY_ITEMS);
            //Log.v(LOG_TAG, "artisList JSON String"+musicArray.toString());
            int numberOfAritist=musicArray.length();

            String[] resultStrs = new String[numberOfAritist];
            artistName= new String[numberOfAritist];
            id=new String[numberOfAritist];
            imagesUrlS=new String[numberOfAritist];


            for (int i = 0; i < musicArray.length(); i++) {
                final String OWM_NAME="name";
                final String OWM_URL="url";
                final int SMALL_IMG_SEQUENCE=2;




                // Get the JSON object representing the day
                JSONObject artistObject=musicArray.getJSONObject(i);
                id[i]=artistObject.getString(SPOTIFY_ID);
                //JSONObject images= artistObject.getJSONArray(SPOTIFY_IMAGE).getJSONObject(SMALL_IMG_SEQUENCE);
                JSONArray images=artistObject.getJSONArray(SPOTIFY_IMAGE);
                if (images.length()>0){
                    imagesUrlS[i]=images.getJSONObject(SMALL_IMG_SEQUENCE).getString(OWM_URL);
                }
                else{
                    imagesUrlS[i]="";
                }

                artistName[i]=artistObject.getString(OWM_NAME);
                resultStrs[i]=artistName[i];
            }

            //output the  the formated data
            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }


        /*
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mArtistListAdapter.clear();
                mArtistListAdapter.addAll(artistName);


                //for (String Str : result) {
                 //   mArtistListAdapter.add(Str);
                //}
            }

        }
        */

    }



}
