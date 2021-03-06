package com.example.android.spotifystreamer.apk;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A placeholder fragment containing a simple view.
 */
public class MusicPlayFragment extends DialogFragment implements AudioManager.OnAudioFocusChangeListener {

    boolean isPlaying = true;
    static private MediaPlayer mediaPlayer;
    private double timeElapsed = 0, finalTime = 0;
    private double timeRemaining;
    private int forwardTime = 2000, backwardTime = 2000;
    private Handler durationHandler = new Handler();

    private String artist;
    private String external_urls;
    private LinearLayout ll;
    private MusicData music;
    private ArrayList<MusicData> musicData;

    private int musicIndex;


    //onSavestate
    private static final String SAVE_PAGE_KEY = "save_page";
    private ArrayList<MusicData> mListInstanceState;
    private ArrayList<MusicData>  onSaveMusic;
    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static final String ARTIST_KEY = "artist";
    private static final String MEDIAPLAYER_KEY = "mediaPlayer";

    private Runnable updateSeekBarTime = new Runnable() {

        public void run() {
            //get current position
            timeElapsed = mediaPlayer.getCurrentPosition();
            ViewHolder viewHolder=new ViewHolder();
            //set seekbar progress
            viewHolder.seekbar.setProgress((int) timeElapsed);
            //set time remaing

            //repeat yourself that again in 100 miliseconds
            durationHandler.postDelayed(this, 100);

            timeRemaining = finalTime - timeElapsed;
            viewHolder.runTime.setText(String.format("%d:%d ", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining),
                    TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));
            if (timeElapsed==mediaPlayer.getDuration()){
                next(viewHolder.next);
            }
        }
    };

    public MusicPlayFragment() {
        setHasOptionsMenu(true);
    }

    public static MusicPlayFragment newInstance(ArrayList<MusicData> music, String artist,int position) {
        MusicPlayFragment myFragment = new MusicPlayFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("music", music);
        args.putString(ARTIST_KEY, artist);
        args.putInt("position", position);
        myFragment.setArguments(args);
        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ll = (LinearLayout) inflater.inflate(R.layout.fragment_music_play, container, false);
        musicData=new ArrayList<>();
        if (savedInstanceState != null) {
            onSaveMusic = savedInstanceState.getParcelableArrayList(SAVE_PAGE_KEY);
            mPosition=savedInstanceState.getInt(SELECTED_KEY);
            musicIndex=mPosition;
            musicData = onSaveMusic;
            artist=savedInstanceState.getString(ARTIST_KEY);
            //mediaPlayer=savedInstanceState.getParcelable(MEDIAPLAYER_KEY);
        } else {
            if (MainActivity.getMTwoPane()) {
                musicData = getArguments().getParcelableArrayList("music");
                musicIndex=getArguments().getInt("position");
                artist = getArguments().getString(ARTIST_KEY);
            } else {
                Intent intent = getActivity().getIntent();
                if (intent != null && intent.hasExtra("Object")) {
                    musicData =  intent.getExtras().getParcelableArrayList("Object");
                    artist = intent.getStringExtra(Intent.EXTRA_TEXT);
                    musicIndex=intent.getIntExtra("position",0);
                }
            }
        }
        initializeViews();
        return ll;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    /*
    public void showDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        MusicPlayFragment newFragment = new MusicPlayFragment();

        if (new MainActivity().getMTwoPane()) {
            // The device is using a large layout, so show the fragment as a dialog
            newFragment.show(fragmentManager, "dialog");
        } else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, newFragment)
                    .addToBackStack(null).commit();
        }
    }
    */
    class ViewHolder {
        public ImageButton playPause, forward, backward,previous,next;
         public TextView songName, runTime, duration, artistName, ablume;
         private ImageView image;
         private SeekBar seekbar;
        public ViewHolder(){
            songName = (TextView) ll.findViewById(R.id.songName);
            artistName = (TextView) ll.findViewById(R.id.artistName);
            image = (ImageView) ll.findViewById(R.id.palyImg);
            ablume = (TextView) ll.findViewById(R.id.Ablume);
            duration = (TextView) ll.findViewById(R.id.songDuration);
            runTime = (TextView) ll.findViewById(R.id.runTime);
            seekbar = (SeekBar) ll.findViewById(R.id.seekBar);
            playPause = (ImageButton) ll.findViewById(R.id.media_play);
            forward = (ImageButton) ll.findViewById(R.id.media_ff);
            backward = (ImageButton) ll.findViewById(R.id.media_bb);
            previous = (ImageButton) ll.findViewById(R.id.previous);
            next = (ImageButton) ll.findViewById(R.id.next);
        }
    }

    private static String oldURL="";
    public void dataLoading(){
        ViewHolder viewHolder=new ViewHolder();
        music=musicData.get(musicIndex-1);
        mPosition=musicIndex;
        String url = music.id.previewUrl;

        if (!oldURL.equals(url)){
            try {
                if (mediaPlayer!=null){mediaPlayer.reset();}
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare(); // might take long! (for buffering, etc)
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        play(viewHolder.playPause);

        viewHolder.playPause.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
        viewHolder.songName.setText(music.id.trackName);
        viewHolder.artistName.setText(artist);
        if (music.id.albumImage600 != null) {
            try {
                Picasso.with(getActivity().getBaseContext()).load(music.id.albumImage600).into(viewHolder.image);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        viewHolder.ablume.setText(music.id.albumName);

        finalTime = mediaPlayer.getDuration();
        viewHolder.duration.setText(String.format("%d:%d ", TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));
        viewHolder.songName.setText(music.id.trackName);
        viewHolder.seekbar.setMax((int) finalTime);
        viewHolder.seekbar.setClickable(false);
        oldURL=url;
    }

    public void initializeViews() {
        dataLoading();
        ViewHolder viewHolder=new ViewHolder();

        viewHolder.forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forward(v);
            }
        });
        viewHolder. backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backward(v);
            }
        });

        viewHolder. previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                previous(v);
            }
        });
        viewHolder. next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                next(v);
            }
        });

        viewHolder. playPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ViewHolder viewHolder = new ViewHolder();
                if (!isPlaying) {
                    play(viewHolder.playPause);
                    viewHolder.playPause.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));

                } else {
                    mediaPlayer.pause();
                    //playPause.setImageResource(getResources().getDrawable(android.R.drawable.presence_busy));
                    viewHolder.playPause.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                }
                isPlaying = !isPlaying;
            }
        });

    }
    public void previous(View view){
        if (musicIndex>1){musicIndex--;}
        mediaPlayer.reset();
       // load the new source
        dataLoading();
       // start
        mediaPlayer.start();
    }

    public void next(View view){
        if (musicIndex<musicData.size()-1){musicIndex++;}
        mediaPlayer.reset();
        dataLoading();
        mediaPlayer.start();
    }

    //handler to change seekBarTime
    public void play(View view) {
        ViewHolder viewHolder=new ViewHolder();
        mediaPlayer.start();
        timeElapsed = mediaPlayer.getCurrentPosition();
        viewHolder.seekbar.setProgress((int) timeElapsed);
        durationHandler.postDelayed(updateSeekBarTime, 100);
    }

    // pause song
    public void pause(View view) {
        mediaPlayer.pause();
    }

    // go forward at forwardTime seconds
    public void forward(View view) {
        //check if we can go forward at forwardTime seconds before song endes
        if ((timeElapsed + forwardTime) <= finalTime) {
            timeElapsed = timeElapsed + backwardTime;
            //seek to the exact second of the track
            mediaPlayer.seekTo((int) timeElapsed);
        }
    }

    public void backward(View view) {
        //check if we can go forward at forwardTime seconds before song endes
        if ((timeElapsed - backwardTime) > 0) {
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


    /*@Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            durationHandler.removeCallbacks(updateSeekBarTime);
            mediaPlayer = null;
        }
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putParcelable(SAVE_PAGE_KEY, listView.onSaveInstanceState());
        outState.putParcelableArrayList(SAVE_PAGE_KEY, musicData);

        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        outState.putString(ARTIST_KEY,artist);
        super.onSaveInstanceState(outState);

    }

    //This part added for share botton
    private ShareActionProvider mShareActionProvider; //use to share the information as the way of message, facebook .etc..
    private static final String SPOTIFY_HASHTAG = " #Spotify Streamer";
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.


        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of com.example.android.spotifystreamer.apk.data they might like to share.
        // If onLoadFinished happens before this, it can go ahead and set the share intent now.
        if (artist != null||music.id.trackName!=null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    //what's going to be sharing.
    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //without this FLAG the, after sharing the info in other app, you will not return you app
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        //sharing plain text
        shareIntent.setType("text/plain");
        external_urls=music.id.external_urls;
        shareIntent.putExtra(Intent.EXTRA_TEXT, artist + music.id.trackName + "-" + external_urls + SPOTIFY_HASHTAG);
        return shareIntent;
    }


}
