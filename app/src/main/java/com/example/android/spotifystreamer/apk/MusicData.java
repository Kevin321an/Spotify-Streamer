package com.example.android.spotifystreamer.apk;

import java.io.Serializable;

/**
 * Created by FM on 6/17/2015.
 */
@SuppressWarnings("serial") //with this annotation are going to hide compiler warning
public class MusicData implements Serializable{
    public String artist;
    public String image;
//    Here would be a Null Pointer Exception err if do not initialize the id
    public TrackData id=new TrackData();
    public MusicData(String artist,String image, String id){
        this.artist=artist;
        this.image=image;
        this.id.id=id;
    }
    public MusicData(String id, String trackName, String albumName, String albumImage600, String albumImage300, String previewURL){
        this.id.id=id;
        this.id.trackName=trackName;
        this.id.albumName=albumName;
        this.id.albumImage600=albumImage600;
        this.id.albumImage300=albumImage300;
        this.id.previewUrl=previewURL;
    }
    //must implements the Serializable or the intent will not deliver
    public class TrackData implements Serializable{
        String id;
        String trackName;
        String albumName;
        String albumImage600;
        String albumImage300;
        String previewUrl;
    }
    @Override
    public String toString() {
        return "artist: " + artist + "image: " + image + "id: " + id+ "\n"
                +this.id.trackName+ "\n"
                +this.id.albumName+ "\n"
                +this.id.albumImage300+ "\n"
                +this.id.albumImage600+ "\n"
                +this.id.previewUrl;

    }
}

