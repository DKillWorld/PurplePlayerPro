package com.dv.apps.purpleplayer;

import android.content.ComponentName;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dv.apps.purpleplayer.Models.Song;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView listView;
    ArrayList<String> folderList;
    ArrayAdapter<String> adapter;

    private List<String> item = null;
    private List<String> path = null;
    private String root= "/storage";


    private MediaBrowserCompat mediaBrowserCompat;
    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback(){
        @Override
        public void onConnected() {
            super.onConnected();
            MediaSessionCompat.Token token = mediaBrowserCompat.getSessionToken();
            MediaControllerCompat mediaControllerCompat = null;
            try {
                mediaControllerCompat = new MediaControllerCompat(FolderActivity.this, token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            MediaControllerCompat.setMediaController(FolderActivity.this, mediaControllerCompat);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aesthetic.attach(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.folders);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.folder_listview);
        listView.setOnItemClickListener(this);

        getDirectory(root);
        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), connectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserCompat.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaBrowserCompat.disconnect();
    }

    @Override
    protected void onPause() {
        Aesthetic.pause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Aesthetic.resume(this);
    }


    private void getDirectory(String dirPath){

        item = new ArrayList<String>();
        path = new ArrayList<String>();



        File f = new File(dirPath);
        File[] files = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()){
                    if (pathname.canRead()) {
                        if (pathname.listFiles().length != 0) {
                            return true;
                        }
                    }
                }else if (pathname.isFile()){
                    if (pathname.getName().contains(".mp3")){
                        return true;
                    }
                }
                return false;
            }
        });

        if(!dirPath.equals(root)){

            item.add(root);
            path.add(root);

            item.add("../");
            path.add(f.getParent());

        }else {
            item.add("sdcard/");
            path.add(Environment.getExternalStorageDirectory().getPath());
        }

        if (files != null) {
            for (int i = 0; i < files.length; i++) {

                File file = files[i];
                path.add(file.getPath());

                if (file.isDirectory()) {
                    item.add(file.getName() + "/");
                } else {
                    item.add(file.getName());
                }
            }
        }

        adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.songName, item);
        listView.setAdapter(adapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = new File(path.get(position));
//        adapter.notifyDataSetChanged();

        if (file.isDirectory()){
            if(file.canRead()) {
                getDirectory(path.get(position));
            }else {
                new MaterialDialog.Builder(this)
                        .title("[" + file.getName() + "] folder can't be read!")
                        .positiveText("OK")
                        .show();
            }
        }else {
            if (file.getName().endsWith(".mp3") || file.getName().endsWith(".MP3")){
                Toast.makeText(getApplicationContext(), R.string.fetching_from_folder_wait, Toast.LENGTH_LONG).show();
                GetSongsFromFolder getSongsFromFolder = new GetSongsFromFolder();
                getSongsFromFolder.execute(file);
            }
        }

    }


    public class GetSongsFromFolder extends AsyncTask<File, Void, Void>{

        @Override
        protected Void doInBackground(File... files) {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(files[0].getPath());
            String songName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (songName == null) {
                File file2 = new File(files[0].getName());
                String temp = files[0].getName();
                songName = temp.substring(0, temp.lastIndexOf("."));
            }


            File file1 = new File(files[0].getParent());
            File[] filesToAddToNowPlaying = file1.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().contains(".mp3");
                }
            });


            //todo Fix this
            ArrayList<Song> tempSongList = new ArrayList<Song>();
            MusicService.getInstance().setSongList(MusicService.getInstance().globalSongList);
            for(int i = 0; i < filesToAddToNowPlaying.length; i++){
                String temp = filesToAddToNowPlaying[i].getName();
                String songToAdd = temp.substring(0, temp.lastIndexOf("."));

                Song song = MusicService.getInstance().getSongByName(filesToAddToNowPlaying[i]);
                if (!song.getTitle().equals("Error")) {
                    tempSongList.add(song);
                }
            }
//                MusicService.getInstance().songList.clear();
//                MusicService.getInstance().songList.addAll(tempSongList);
            MusicService.getInstance().setSongList(tempSongList);
            MediaControllerCompat.getMediaController(FolderActivity.this).getTransportControls().playFromSearch(songName, null);
//                MusicService.getInstance().mediaSessionCompat.getController().getTransportControls()
            return null;
        }
    }
}