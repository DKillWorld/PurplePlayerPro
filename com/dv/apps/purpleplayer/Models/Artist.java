package com.dv.apps.purpleplayer.Models;

import android.content.Context;

/**
 * Created by Dhaval on 22-12-2017.
 */

public class Artist {

    private String artistName;
    private long id, year;
    private int numberOfSongs, numberOfAlbums;
    private Context context;

    public Artist(Context context, String artistName, long id, int numberOfSongs,int numberOfAlbums) {
        this.artistName = artistName;
        this.id = id;
        this.numberOfSongs = numberOfSongs;
        this.numberOfAlbums = numberOfAlbums;
        this.context = context;
    }

    public String getArtistName(){
        return artistName;
    }

    public long getId(){
        return id;
    }

    public int getNumberOfSongs(){
        return numberOfSongs;
    }

    public int getNumberOfAlbums() { return numberOfAlbums; }

}
