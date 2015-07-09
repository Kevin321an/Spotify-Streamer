package com.example.android.spotifystreamer.apk;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class MusicPlay extends ActionBarActivity {
    private MediaPlayer mediaPlayer;
    public TextView songName, duration, artistName, ablume;
    private double timeElapsed = 0, finalTime = 0;
    private int forwardTime = 2000, backwardTime = 2000;
    private Handler durationHandler = new Handler();
    private ImageView image;
    private String artist;

    private SeekBar seekbar;
    private MusicData music;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("Object")) {
            //music = intent.getStringExtra(Intent.EXTRA_TEXT);
            music = (MusicData) intent.getSerializableExtra("Object");
            artist=(String) intent.getSerializableExtra("artist");
            //((TextView) rootView.findViewById(R.id.detail_text))
            // .setText(mForecastStr);
        }

        initializeViews();


    }
    public void initializeViews(){
        String url=music.id.previewUrl;
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{ mediaPlayer.setDataSource(url);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)

        }
        catch (IOException e){
            e.printStackTrace();
        }
        //mediaPlayer.start();

        songName = (TextView) findViewById(R.id.songName);
        songName.setText(music.id.trackName);

        artistName=(TextView) findViewById(R.id.artistName);
        artistName.setText(artist);


        image=(ImageView) findViewById(R.id.palyImg);
        if (music.id.albumImage600 != null) {
            //ImageView iconView = (ImageView) layout.findViewById(R.id.list_ablum_imageview);
            //iconView.setImageResource(R.drawable.ic_launcher);
            //Picasso.with(context).load(image[position]).into(iconView);
            try {
                Picasso.with(this.getBaseContext()).load(music.id.albumImage600).into(image);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ablume=(TextView) findViewById(R.id.Ablume);
        ablume.setText(music.id.albumName);

        finalTime = mediaPlayer.getDuration();
        duration = (TextView) findViewById(R.id.songDuration);

        seekbar = (SeekBar) findViewById(R.id.seekBar);
        songName.setText(music.id.trackName);
        seekbar.setMax((int) finalTime);
        seekbar.setClickable(false);
    }
    public void play(View view) {
        mediaPlayer.start();
        timeElapsed = mediaPlayer.getCurrentPosition();
        seekbar.setProgress((int) timeElapsed);
        durationHandler.postDelayed(updateSeekBarTime, 100);
    }
    //handler to change seekBarTime

    private Runnable updateSeekBarTime = new Runnable() {

        public void run() {
            //get current position
            timeElapsed = mediaPlayer.getCurrentPosition();

            //set seekbar progress
            seekbar.setProgress((int) timeElapsed);

            //set time remaing
            double timeRemaining = finalTime - timeElapsed;
            duration.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining),
                    TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));

            //repeat yourself that again in 100 miliseconds
            durationHandler.postDelayed(this, 100);

        }

    };
    // pause mp3 song
    public void pause(View view) {
        mediaPlayer.pause();
    }
    // go forward at forwardTime seconds

    public void forward(View view) {
        //check if we can go forward at forwardTime seconds before song endes
        if ((timeElapsed + forwardTime)<= finalTime) {
            timeElapsed = timeElapsed - backwardTime;
            //seek to the exact second of the track
            mediaPlayer.seekTo((int) timeElapsed);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_media_player, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
