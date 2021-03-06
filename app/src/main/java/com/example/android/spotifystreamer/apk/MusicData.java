package com.example.android.spotifystreamer.apk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by FM on 6/17/2015.
 */

public class MusicData implements Parcelable {
    public static final Parcelable.Creator<MusicData> CREATOR =
            new Parcelable.Creator<MusicData>() {
                @Override
                public MusicData createFromParcel(Parcel in) {
                    return new MusicData(in);
                }
                @Override
                public MusicData[] newArray(int size) {
                    return new MusicData[size];
                }
            };

    public String artist;
    public String image;
    public String image1000;
    //    Here would be a Null Pointer Exception err if do not initialize the id
    public TrackData id = new TrackData();

    public MusicData(String artist, String image, String image1000,String id) {
        this.artist = artist;
        this.image = image;
        this.image1000=image1000;
        this.id.id = id;
    }

    private MusicData(Parcel input) {
        id=(TrackData) input.readParcelable((TrackData.class.getClassLoader()));
        artist = input.readString();
        image = input.readString();
        image1000=input.readString();

    }

    public MusicData(String id, String trackName, String albumName, String albumImage600, String albumImage300, String previewURL, String external_urls) {
        this.id.id = id;
        this.id.trackName = trackName;
        this.id.albumName = albumName;
        this.id.albumImage600 = albumImage600;
        this.id.albumImage300 = albumImage300;
        this.id.previewUrl = previewURL;
        this.id.external_urls=external_urls;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(id, flags);
        out.writeString(artist);
        out.writeString(image);
        out.writeString(image1000);
    }

    @Override
    public String toString() {
        return "artist: " + artist + "image: " + image + "id: " + id + "\n"
                + this.id.trackName + "\n"
                + this.id.albumName + "\n"
                + this.id.albumImage300 + "\n"
                + this.id.albumImage600 + "\n"
                + this.id.previewUrl + "\n"
                +this.id.external_urls;

    }
    public static class TrackData implements Parcelable {
        String id;
        String trackName;
        String albumName;
        String albumImage600;
        String albumImage300;
        String previewUrl;
        String external_urls;
        public TrackData(){

        }
        private TrackData(Parcel input) {

            id = input.readString();
            trackName = input.readString();
            albumName = input.readString();
            albumImage600 = input.readString();
            albumImage300 = input.readString();
            previewUrl = input.readString();
            external_urls=input.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(id);
            out.writeString(trackName);
            out.writeString(albumName);
            out.writeString(albumImage600);
            out.writeString(albumImage300);
            out.writeString(previewUrl);
            out.writeString(external_urls);
        }
        public  static final Parcelable.Creator<TrackData> CREATOR =
            new Parcelable.Creator<TrackData>() {
                @Override
                public TrackData createFromParcel(Parcel in) {
                    return new TrackData(in);
                }

                @Override
                public TrackData[] newArray(int size) {
                    return new TrackData[size];
                }
            };

    }
}

