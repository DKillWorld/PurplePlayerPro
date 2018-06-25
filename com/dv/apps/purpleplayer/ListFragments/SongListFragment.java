package com.dv.apps.purpleplayer.ListFragments;


import android.animation.LayoutTransition;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dv.apps.purpleplayer.ListAdapters.PlaylistAdapter;
import com.dv.apps.purpleplayer.ListAdapters.SongAdapter;
import com.dv.apps.purpleplayer.Models.Playlist;
import com.dv.apps.purpleplayer.Models.Song;
import com.dv.apps.purpleplayer.MusicService;
import com.dv.apps.purpleplayer.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongListFragment extends Fragment {


    ListView listView;
    GridView gridView;
    public SongAdapter adapter;
    ArrayList<Song> songList;
    SearchView searchView;

    public SongListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_list, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.fragment_song_list);
        gridView = view.findViewById(R.id.fragment_song_grid);
        setHasOptionsMenu(true);
        this.songList = getSongs();

        adapter = new SongAdapter(getActivity(), this.songList);


        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("show_track_as", true)){
            gridView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            gridView.setFastScrollEnabled(true);
            registerForContextMenu(gridView);

            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MusicService.getInstance().setSongList(songList);
                    Song tempSong = adapter.getItem(position);
                    Uri playUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, tempSong.getId());
                    Bundle bundle = new Bundle();
                    bundle.putInt("Pos", songList.indexOf(tempSong));
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls()
                            .playFromUri(playUri, bundle);
                }
            });
        }else {
            listView.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
            listView.setFastScrollEnabled(true);
            registerForContextMenu(listView);

            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MusicService.getInstance().setSongList(songList);
                    Song tempSong = adapter.getItem(position);
                    Uri playUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, tempSong.getId());
                    Bundle bundle = new Bundle();
                    bundle.putInt("Pos", songList.indexOf(tempSong));
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls()
                            .playFromUri(playUri, bundle);
                }
            });
        }





    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);

        //SearchView Code
        MenuItem item = menu.findItem(R.id.search);
        searchView = (SearchView) item.getActionView();
        LinearLayout searchBar = (LinearLayout) searchView.findViewById(R.id.search_bar);
        searchBar.setLayoutTransition(new LayoutTransition());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public ArrayList<Song> getSongs(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String projection[] = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Albums.ALBUM_ID,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.YEAR};
        String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
        songList = new ArrayList<>();
        Cursor songCursor = getActivity().getContentResolver().query(uri, projection, selection, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
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

                songList.add(new Song(getActivity(), currentTitle, currentId, currentDuration, currentArtist, albumArtUri));
            } while (songCursor.moveToNext());
            songCursor.close();
        }
        MusicService.getInstance().setGlobalSongList(songList);
        return songList;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.menu_song_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if ((MusicService.getInstance().songList != null) && (MusicService.getInstance().songList.size() != 0)) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.play_next:
                    MusicService.getInstance().songList.add(MusicService.getInstance().songPosn + 1, adapter.getItem(info.position));
                    Toast.makeText(getActivity(), R.string.playing_next, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.add_to_queue:
                    MusicService.getInstance().songList.add(adapter.getItem(info.position));
                    Toast.makeText(getActivity(), R.string.added_to_queue, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.add_to_playlist:
                    final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                            .customView(R.layout.now_playing, false)
                            .show();

                    ArrayList<Playlist> arrayList = new ArrayList<>();
                    final Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            String playlistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                            arrayList.add(new Playlist(getContext(), playlistName, id));
                        } while (cursor.moveToNext());
                    }
                    ListView listView2 = dialog.getCustomView().findViewById(R.id.now_playing_list);
                    dialog.getCustomView().findViewById(R.id.now_playing_grid).setVisibility(View.GONE);
                    listView2.setVisibility(View.VISIBLE);
                    final PlaylistAdapter playlistAdapter = new PlaylistAdapter(getActivity(), arrayList);
                    listView2.setAdapter(playlistAdapter);

                    Song songToAdd = adapter.getItem(info.position);
                    final ContentValues cv = new ContentValues();
                    cv.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songToAdd.getId());
                    cv.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, 0);


                    //TODO : Fix this implementation
                    listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Playlist playlist = playlistAdapter.getItem(position);
                            Uri uri = getContext().getContentResolver().insert(
                                    MediaStore.Audio.Playlists.Members.getContentUri("external", playlist.getId()), cv);
                            Toast.makeText(getContext(), R.string.added_to_playlist, Toast.LENGTH_SHORT).show();
                            dialog.cancel();


                        }
                    });
                    break;
            }
        }else {
            Toast.makeText(getActivity(), R.string.emptyPlaylist, Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
