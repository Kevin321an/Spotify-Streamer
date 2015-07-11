package com.example.android.spotifystreamer.apk;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by FM on 6/16/2015.
 */

public class MainAdapter extends ArrayAdapter<MusicData> {

    //private final Activity context;
    private int mResource;
    private Context mContext;

    //public MainAdapter(Activity context,
    //String[] songName, String[] image, String[] album) {
    public MainAdapter(Activity context, int resource, ArrayList<MusicData> music) {
        super(context, resource, music);
        this.mResource = resource;

        //super(context, R.layout.list_item_artist_textview, songName);
        this.mContext = context;
    }
    class ViewHodler {
        // declare your views here
        TextView artist;
        ImageView iconImage;
        TextView trackName;
        TextView albumName;
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LinearLayout layout;
               // Get the data item for this position
        MusicData music = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHodler holder;
        if (view == null) {
            layout = new LinearLayout(getContext());
            ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(mResource, layout, true);
            holder = new ViewHodler();
            holder.artist=((TextView) layout.findViewById(R.id.list_item_artist_textview));
            holder.iconImage = (ImageView) layout.findViewById(R.id.list_artists_imageview);
            holder.trackName=((TextView) layout.findViewById(R.id.list_item_song_textview));
            holder.albumName=((TextView) layout.findViewById(R.id.list_item_ablum_textview));

            layout.setTag(holder);

            // rootView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist_textview, parent, false);
        } else {
            layout = (LinearLayout) view;
            holder = (ViewHodler) view.getTag();
        }
        //!!!!!!!!!!!!!!!!!THere was a bug as the rooView was not being inflate every time.
        if (mResource == R.layout.list_item_artist_textview) {


            //TextView songNameView= (TextView) layout.findViewById(R.id.list_item_artist_textview);
            holder.artist.setText(music.artist);

            //songNameView.setText(songName[position]);
            //songNameView.setText(music.artist);


            if (music.image != null) {

                //iconView.setImageResource(R.drawable.ic_launcher);
                //Picasso.with(context).load(image[position]).into(iconView);
                try {
                    Picasso.with(mContext).load(music.image).into(holder.iconImage);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
        else if (mResource == R.layout.list_item_artist_ablum) {
            holder.trackName.setText(music.id.trackName);
            holder.albumName.setText(music.id.albumName);


            //TextView songNameView= (TextView) layout.findViewById(R.id.list_item_artist_textview);


            //songNameView.setText(songName[position]);
            //songNameView.setText(music.artist);


            if (music.id.albumImage300 != null) {
                ImageView iconView = (ImageView) layout.findViewById(R.id.list_ablum_imageview);
                //iconView.setImageResource(R.drawable.ic_launcher);
                //Picasso.with(context).load(image[position]).into(iconView);
                try {
                    Picasso.with(mContext).load(music.id.albumImage300).into(iconView);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }

        return layout;
    }

}

