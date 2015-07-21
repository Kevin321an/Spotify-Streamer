package com.example.android.spotifystreamer.apk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements MainActivityFragment.Callback {

    private static boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static String mLocation;

    public static boolean getMTwoPane(){
        return mTwoPane;
    }
    public static String getMLocation(){
        return mLocation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.top10Track_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.top10Track_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            //his will get rid of an unnecessary shadow below the action bar for smaller screen devices like phones.
            // Then the action bar and Today item will appear to be on the same plane
            // (as opposed to two different planes, where one casts a shadow on the other).
        }
        //get the location
        mLocation = getCountryCode(getPreferredLocation(this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onItemSelected(MusicData trackList){
        if(mTwoPane){

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args=new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, trackList);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setSubtitle(trackList.artist);

            DetailActivityFragment fragment=new DetailActivityFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top10Track_detail_container, fragment,DETAILFRAGMENT_TAG)
                    .commit();
        }else{
            Intent intent=new Intent(this, DetailActivity.class)
                    .putExtra("Object", trackList);
            startActivity(intent);
        }
    }
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));

    }
    /*
    private enum Country {
        sweden, china, canada, america,
        usa, australia, blgium,name
    }
    */
    private String getCountryCode(String countryName){
        //Country name=Country.valueOf(countryName.toLowerCase());
        switch (countryName.toLowerCase()){
            case "sweden" :return "SE";
            case "china" :return "CN";
            case "canada":return "CA";
            case "america":return "US";
            case "usa":return "US";
            case "australia": return"AT";
            case "belgium":return"BE";
            default:return "SE";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location=getCountryCode(getPreferredLocation(this));
        if (location!=null&&!location.equals(mLocation))
        {
            mLocation = location;
        }
    }

}
