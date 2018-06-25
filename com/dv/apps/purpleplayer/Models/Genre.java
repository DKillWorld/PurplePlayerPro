package com.dv.apps.purpleplayer.Models;

import android.content.Context;

/**
 * Created by Dhaval on 04-02-2018.
 */

public class Genre {

    private Context context;
    private String genreName;
    private long id;

    public Genre(Context context, String genreName, long id) {
        this.context = context;
        this.genreName = genreName;
        this.id = id;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
