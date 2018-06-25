package com.dv.apps.purpleplayer.ListFragments;


import android.animation.LayoutTransition;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
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
public class PlaylistListFragment extends Fragment {


    ListView listView, listViewDetailMode;
    GridView gridView;
    public static boolean in_detail_view_playlist = false;
    SearchView searchView;

    PlaylistAdapter playlistAdapter;
    SongAdapter songAdapter;
    ArrayList<Song> tempSongList;
    ArrayList<Playlist> arrayList;

    public PlaylistListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        listView = view.findViewById(R.id.fragment_playlist_list);
        listViewDetailMode = view.findViewById(R.id.fragment_playlist_list_detail);
        gridView = view.findViewById(R.id.fragment_playlist_grid);
        registerForContextMenu(listView);
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String projection[] = {MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME};
        arrayList = new ArrayList<>();
        final Cursor playlistCursor = getContext().getContentResolver().query(uri, projection, null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        if (playlistCursor != null && playlistCursor.moveToFirst()) {
            do {
                String playlistName = playlistCursor.getString(playlistCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                long id = playlistCursor.getLong(playlistCursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                arrayList.add(new Playlist(getContext(), playlistName, id));
            } while (playlistCursor.moveToNext());
        }
        playlistAdapter = new PlaylistAdapter(getActivity(), arrayList);
        listView.setAdapter(playlistAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!in_detail_view_playlist) {
//                    String s = playlistAdapter.getItem(position).;
//                    playlistCursor.moveToPosition(arrayList.indexOf(s));
                    Playlist playlist = playlistAdapter.getItem(position);

                    Uri uri1 = MediaStore.Audio.Playlists.Members.getContentUri("external",playlist.getId());

                    tempSongList = new ArrayList<Song>();
                    Cursor songCursor = getActivity().getContentResolver().query(uri1, null, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
                    if (songCursor != null && songCursor.moveToFirst()) {
                        int songId = songCursor.getColumnIndex((MediaStore.Audio.Playlists.Members.AUDIO_ID));
                        int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE);
                        int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DURATION);
                        int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST);
                        int songAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM_ID);

                        do {
                            String currentTitle = songCursor.getString(songTitle);
                            long currentId = songCursor.getLong(songId);
                            int currentDuration = songCursor.getInt(songDuration);
                            String currentArtist = songCursor.getString(songArtist);
                            long currentAlbumId = songCursor.getLong(songAlbumId);

                            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentAlbumId);

                            tempSongList.add(new Song(getActivity(), currentTitle, currentId, currentDuration, currentArtist, albumArtUri));
                        } while (songCursor.moveToNext());
                        songCursor.close();
                        songAdapter = new SongAdapter(getActivity(), tempSongList);
                    }

                    if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("show_track_as", true)){
                        gridView.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        listViewDetailMode.setVisibility(View.GONE);
                        gridView.setAdapter(songAdapter);
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Song tempSong = songAdapter.getItem(position);
                                MusicService.getInstance().setSongList(tempSongList);
                                MediaControllerCompat.getMediaController(getActivity()).getTransportControls()
                                        .playFromSearch(tempSong.getTitle(), null);
                            }
                        });
                    }else {
                        gridView.setVisibility(View.GONE);
                        listView.setVisibility(View.GONE);
                        listViewDetailMode.setVisibility(View.VISIBLE);
                        listViewDetailMode.setAdapter(songAdapter);
                        listViewDetailMode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Song tempSong = songAdapter.getItem(position);
                                MusicService.getInstance().setSongList(tempSongList);
                                MediaControllerCompat.getMediaController(getActivity()).getTransportControls()
                                        .playFromSearch(tempSong.getTitle(), null);
                            }
                        });
                    }
                    in_detail_view_playlist = true;
                    getActivity().invalidateOptionsMenu();
                } else {
//                    Song tempSong = songAdapter.getItem(position);
//                    MusicService.getInstance().setSongList(tempSongList);
//                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls()
//                            .playFromSearch(tempSong.getTitle(), null);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        in_detail_view_playlist = false;
        listView.setAdapter(playlistAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_playlist_fragment, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        MenuItem closeItem = menu.findItem(R.id.close);

        if (!in_detail_view_playlist) {
            searchItem.setVisible(true);
            searchView = (SearchView) searchItem.getActionView();
            LinearLayout searchBar = (LinearLayout) searchView.findViewById(R.id.search_bar);
            searchBar.setLayoutTransition(new LayoutTransition());
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    playlistAdapter.getFilter().filter(newText);
                    return true;
                }
            });
            closeItem.setVisible(false);


        }else {
            closeItem.setVisible(true);
            searchItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.close:
                in_detail_view_playlist = false;
                if (songAdapter != null){
                    songAdapter.clear();
                }
                if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("show_track_as", true)){
                    gridView.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }else {
                    listViewDetailMode.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
                listView.setAdapter(playlistAdapter);
                playlistAdapter.getFilter().filter("");
                getActivity().invalidateOptionsMenu();
//                ContentValues cv = new ContentValues();
//                cv.put(MediaStore.Audio.Playlists.NAME, "New 21/01");
//                getActivity().getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, cv);
                return true;

            case R.id.add_playlist:
                MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                        .title(R.string.add_playlist)
                        .customView(R.layout.add_playlist, false)
                        .positiveText(R.string.ok)
                        .show();

                final EditText editText = dialog.getCustomView().findViewById(R.id.add_playlist_name);

                dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String name = editText.getText().toString();
                        ContentValues cv = new ContentValues();
                        cv.put(MediaStore.Audio.Playlists.NAME, name);

                        if (name.length() != 0) {
                            Uri uri = getContext().getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, cv);
                            if (uri != null) {
                                Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
                                cursor.moveToFirst();
                                String pName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                                Playlist playlist = new Playlist(getContext(), pName, id);
                                playlistAdapter.add(playlist);
                                Toast.makeText(getActivity(), R.string.playlist_added, Toast.LENGTH_SHORT).show();
                            }

                        }else {
                            Toast.makeText(getActivity(), R.string.emptyNameNotAllowed, Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if(listView.getAdapter().getItem(0) instanceof Playlist){
            getActivity().getMenuInflater().inflate(R.menu.menu_playlist_context, menu);
        }else {
//            getActivity().getMenuInflater().inflate(R.menu.menu_song_context, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()){
            case R.id.remove_playlist:
                Playlist playlist = playlistAdapter.getItem(info.position);
                if (playlist != null){
                    getContext().getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                            MediaStore.Audio.Playlists._ID + "=" + playlist.getId() , null);
                    arrayList.remove(info.position);
                    playlistAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), R.string.removed, Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }
}
