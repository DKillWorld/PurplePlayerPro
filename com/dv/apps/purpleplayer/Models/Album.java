package com.dv.apps.purpleplayer.Models;

import android.content.Context;
import android.net.Uri;

/**
 * Created by Dhaval on 19-12-2017.
 */

public class Album {

    private String albumName;
    private Uri image;
    private long id, year;
    private int numberOfSongs;
    private Context context;

    public Album(Context context, String albumName, long id, int numberOfSongs, long year, Uri albumArt) {
        this.albumName = albumName;
        this.id = id;
        this.numberOfSongs = numberOfSongs;
        this.year = year;
        this.image = albumArt;
        this.context = context;
    }

    public String getAlbumName(){
        return albumName;
    }

    public long getId(){
        return id;
    }

    public int getNumberOfSongs(){
        return numberOfSongs;
    }

    public Uri getImage() {
        return image;
    }

    public long getYear(){ return year; }
}
