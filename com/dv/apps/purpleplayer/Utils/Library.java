package com.dv.apps.purpleplayer.Utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.dv.apps.purpleplayer.Models.Song;
import com.dv.apps.purpleplayerv2.Models.Song;

import java.util.ArrayList;

/**
 * Created by Dhaval on 12-05-2018.
 */

public class Library {

    public static MediaDescriptionCompat getMediaDescription(Context context, Song song) {
        Bundle extras = new Bundle();
        Bitmap bitmap = song.getImageBitmap();
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap);
        return new MediaDescriptionCompat.Builder()
                .setMediaId(String.valueOf(song.getId()))
                .setIconBitmap(bitmap)
                .setTitle(song.getTitle())
                .setDescription(song.getArtist())
                .setExtras(extras)
                .build();
    }

    public ArrayList<Song> getSongs(Context context){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String projection[] = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Albums.ALBUM_ID,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.YEAR};
        String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
        ArrayList<Song> songList = new ArrayList<>();
        Cursor songCursor = context.getContentResolver().query(uri, projection, selection, null, MediaStore.Audio.Media.TITLE);
        if (songCursor != null && songCursor.moveToFirst()) {
            int songId = songCursor.getColumnIndex((MediaStore.Audio.Media._ID));
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID);
            int songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int songYear = songCursor.getColumnIndex(MediaStore.Audio.Media.YEAR);

            do {
                String currentTitle = songCursor.getString(songTitle);
                long currentId = songCursor.getLong(songId);
                int currentDuration = songCursor.getInt(songDuration);
                String currentArtist = songCursor.getString(songArtist);
                long currentAlbumId = songCursor.getLong(songAlbumId);
                String currentData = songCursor.getString(songData);
                long currentYear = songCursor.getLong(songYear);

                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentAlbumId);

                songList.add(new Song(context, currentTitle, currentId, currentDuration, currentArtist, albumArtUri, currentData));
            } while (songCursor.moveToNext());
            songCursor.close();
        }
        return songList;
    }
}
