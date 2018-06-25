package com.dv.apps.purpleplayer.Models;

import android.content.Context;

/**
 * Created by Dhaval on 04-02-2018.
 */

public class Playlist {

    private Context context;
    private String playlistName;
    private long id;

    public Playlist(Context context, String playlistName, long id) {
        this.context = context;
        this.playlistName = playlistName;
        this.id = id;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
