package com.example.android.spotifystreamer.apk;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by FM on 6/16/2015.
 */

public class MainAdapter extends  ArrayAdapter<MusicData> {

    //private final Activity context;
    //private final String[] songName;
    //private final String[] image;
    //private final String[] album;

    //public MainAdapter(Activity context,
                        //String[] songName, String[] image, String[] album) {
    public MainAdapter(Activity context, ArrayList<MusicData> music){
        super (context, 0, music);
        /*
        super(context, R.layout.list_item_artist_textview, songName);
        this.context = context;
        this.songName = songName;
        this.image = image;
        //this.album = album;
        */

    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        //LayoutInflater inflater = context.getLayoutInflater();
        View rootView = LayoutInflater.from(context).inflate(R.layout.list_item_artist_textview, parent, false);
        //View rowView= inflater.inflate(R.layout.list_item_music, null, true);

        ImageView iconView = (ImageView) rootView.findViewById(R.id.list_artists_imageview);
        //iconView.setImageResource(R.drawable.ic_launcher);
        Picasso.with(context).load(image[position]).into(iconView);

        TextView songNameView = (TextView) rootView.findViewById(R.id.list_item_artist_textview);
        songNameView.setText(songName[position]);
        // Read weather forecast from cursor
        //String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it

        //TextView albumView = (TextView) rootView.findViewById(R.id.list_item_albumname_textview);
        //albumView.setText(album[position]);

        return rootView;
}
/*
package com.example.poornima_udacity.spotify_project1;

        import android.app.Activity;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.squareup.picasso.Picasso;

public class MusicAdapter extends ArrayAdapter<String> {


    }
    */
}

