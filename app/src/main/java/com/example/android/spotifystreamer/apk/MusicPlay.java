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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class MusicPlay extends ActionBarActivity implements AudioManager.OnAudioFocusChangeListener{
    private MediaPlayer mediaPlayer;
    public TextView songName, runTime, duration, artistName, ablume;
    private double timeElapsed = 0, finalTime = 0;
    private double timeRemaining;
    private int forwardTime = 2000, backwardTime = 2000;
    private Handler durationHandler = new Handler();
    private ImageView image;
    private String artist;
    boolean isPlaying = false;
    public ImageButton playPause;

    private SeekBar seekbar;
    private MusicData music;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("Object")) {
            music = (MusicData) intent.getSerializableExtra("Object");
            artist= intent.getStringExtra(Intent.EXTRA_TEXT);
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

        duration.setText(String.format("%d:%d ", TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));

        runTime=(TextView)findViewById(R.id.runTime);

        seekbar = (SeekBar) findViewById(R.id.seekBar);
        songName.setText(music.id.trackName);
        seekbar.setMax((int) finalTime);
        seekbar.setClickable(false);


        playPause = (ImageButton) findViewById(R.id.media_play);
        playPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isPlaying) {
                    mediaPlayer.pause();
                    //playPause.setImageResource(getResources().getDrawable(android.R.drawable.presence_busy));
                    playPause.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                } else {
                    play(playPause);
                    playPause.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                }
                isPlaying = !isPlaying;
            }
        });

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

            //repeat yourself that again in 100 miliseconds
            durationHandler.postDelayed(this, 100);

            timeRemaining = finalTime - timeElapsed;
            runTime.setText(String.format("%d:%d ", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining),
                    TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));
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
            timeElapsed = timeElapsed + backwardTime;
            //seek to the exact second of the track
            mediaPlayer.seekTo((int) timeElapsed);
        }
    }
    public void backward(View view) {
        //check if we can go forward at forwardTime seconds before song endes
        if ((timeElapsed - backwardTime)>0) {
            timeElapsed = timeElapsed - backwardTime;
            //seek to the exact second of the track
            mediaPlayer.seekTo((int) timeElapsed);
        }
    }
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initializeViews();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
               mediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_media_player, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) mediaPlayer.release();
    }
}
