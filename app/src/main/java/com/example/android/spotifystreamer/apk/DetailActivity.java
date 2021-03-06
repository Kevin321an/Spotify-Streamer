package com.example.android.spotifystreamer.apk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_detail);
         if (savedInstanceState == null) {
             // Create the detail fragment and add it to the activity
             // using a fragment transaction.
             Bundle arguments=new Bundle();
             arguments.putParcelable(DetailActivityFragment.DETAIL_URI, getIntent().getData());
             DetailActivityFragment fragment=new DetailActivityFragment();
             fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top10Track_detail_container, fragment)
                    .commit();
        }
        /*
        //set title which will be called from fragment
        ActionBar actionBar = getSupportActionBar();
        Intent intent=getIntent();
        //Bundle com.example.android.spotifystreamer.apk.data = getIntent().getExtras();
        if (intent != null && intent.hasExtra("Object")) {
            music = (MusicData) intent.getExtras().getParcelable("Object");
        }
        if(actionBar != null&&!MainActivity.getMTwoPane()) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(music.artist);
        }
        */
    }
    /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return false;
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

    }*/


}
