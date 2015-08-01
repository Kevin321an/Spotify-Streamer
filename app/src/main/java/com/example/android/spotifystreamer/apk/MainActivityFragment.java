package com.example.android.spotifystreamer.apk;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;

import service.Json;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<MusicData> {
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String SAVE_PAGE_KEY = "save_page";
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

        //for  non network or data view
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
                boolean compareS = str.equals(s.toString().trim());

                if (!s.toString().isEmpty() && !compareS) {
                    performSearch(s.toString());
                }
                str = s.toString().trim();


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



                //fetch data by using Pending Intent
                Intent intent = new Intent(getActivity(), Json.class);
                intent.putExtra(Json.ARTIST,search);
                getActivity().startService(intent);

                /*
                 //fetch data by using Pending Intent
                Intent alarmIntent = new Intent(getActivity(), Json.AlarmReceiver.class);
                alarmIntent.putExtra(Json.ARTIST, search);
                //Wrap in a pending intent which only fires once.
                //Parameters:
                //context	The Context in which this PendingIntent should perform the broadcast.
                //requestCode	Private request code for the sender
                //intent	The Intent to be broadcast.
                //flags
                PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);//getBroadcast(context, 0, i, 0);

                AlarmManager am=(AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);

                //Set the AlarmManager to wake up the system.
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
                */
                //FetchArtistTask artistTask = new FetchArtistTask();
                //artistTask.execute(search);

                //Bundle artistArray=new Bundle();

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

    public class FetchTrackTask extends AsyncTask<Void, Void, ArrayList<MusicData>> {
        private final String LOG_TAG = FetchTrackTask.class.getSimpleName();
        protected  ArrayList<MusicData>  doInBackground(Void... params) {

             ArrayList<MusicData> result=Json.getArtistArray().getParcelableArrayList(Json.ARTIST_LIST);
            return result;
        }

        private ArrayList<MusicData> preResult;
       /* protected void onPostExecute(ArrayList<MusicData> result) {
            if (result!=null&&result!=preResult){
                mArtistListAdapter.clear();
                mArtistListAdapter.addAll(result);
                preResult=result;
            }

        }*/

    }

    //Save the ListView state on onSaveInstanceState:
    @Override
    public void onSaveInstanceState(Bundle outState) {

        //outState.putParcelable(SAVE_PAGE_KEY, listView.onSaveInstanceState());
        outState.putParcelableArrayList(SAVE_PAGE_KEY, mListInstanceState);
        super.onSaveInstanceState(outState);
    }
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




    @Override
    public void onLoadFinished(Loader<MusicData> loader, MusicData data) {

        if (result!=null){
            mArtistListAdapter.clear();
            mArtistListAdapter.addAll(result);
        }
        /*mForecastAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
            updateEmptyView();
        }*/
    }
    //release any resource
    @Override
    public void onLoaderReset(Loader<MusicData> Loader) {
        mArtistListAdapter.clear();
    }
    ArrayList<MusicData> result;
    //These are three loader callback function
    @Override
    public Loader<MusicData> onCreateLoader(int i, Bundle bundle) {

        result=Json.getArtistArray().getParcelableArrayList(Json.ARTIST_LIST);

        return new Loader<MusicData>(getActivity());

    }

    private static final int FORECAST_LOADER = 0;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


}



