package com.example.android.spotifystreamer.apk;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;


public class MusicPlay extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_music_play);
/*
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        //MusicPlayFragment fragment=new MusicPlayFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_media_play, fragment)
                .commit();


        FragmentManager fm = getFragmentManager();
        MusicPlayFragment dialogFragment = new MusicPlayFragment ();
        //dialogFragment.show(fm, "Sample Fragment");
        dialogFragment.showDialog();
        */


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }




}
