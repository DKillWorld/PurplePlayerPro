package com.dv.apps.purpleplayer.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.dv.apps.purpleplayer.R;

/**
 * Created by Dhaval on 31-12-2017.
 */

public class RadioStation {

    private String title, artist, radioStreamLink;
    private Uri stationUri;
    private long id;
    private int duration;
    private Context context;

    public RadioStation(){
        //Firebase Constructor
    }

    public RadioStation(Context context, String title, int duration, String artist, String radioStreamLink) {
        this.title = title;
        this.id = id;
        this.duration = duration;
        this.artist = artist;
        this.stationUri = Uri.parse(radioStreamLink);
        this.context = context;
    }

    public String getTitle(){
        return title;
    }

    public long getId(){
        return id;
    }

    public int getDuration(){
        return duration;
    }

    public String getArtist(){
        return artist;
    }

    public Uri getStationUri() {
        return stationUri;
    }

    public Bitmap getImageBitmap(){
        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_web);
    }
}
