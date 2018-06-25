package com.dv.apps.purpleplayer.ListFragments;


import android.animation.LayoutTransition;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.widget.SearchView;
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

import com.dv.apps.purpleplayer.ListAdapters.ArtistAdapter;
import com.dv.apps.purpleplayer.ListAdapters.SongAdapter;
import com.dv.apps.purpleplayer.Models.Artist;
import com.dv.apps.purpleplayer.Models.Song;
import com.dv.apps.purpleplayer.MusicService;
import com.dv.apps.purpleplayer.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistListFragment extends Fragment {

    ListView listView, listViewDetailMode;
    GridView gridView;
    public static boolean in_detail_view_artist = false;
    SearchView searchView;

    ArtistAdapter artistAdapter;
    SongAdapter songAdapter;
    ArrayList<Song> tempSongList;

    public ArtistListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        listView = view.findViewById(R.id.fragment_artist_list);
        listViewDetailMode = view.findViewById(R.id.fragment_artist_list_detail);
        gridView = view.findViewById(R.id.fragment_artist_grid);
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        final ArrayList<Artist> arrayList = new ArrayList<>();
        final Cursor artistCursor = getContext().getContentResolver().query(uri, null, null, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
        if (artistCursor != null && artistCursor.moveToFirst()) {
            do {
                String artistName = artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
                int numberOfTracks = artistCursor.getInt(artistCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
                long id = artistCursor.getLong(artistCursor.getColumnIndex(MediaStore.Audio.Artists._ID));
                int numberOfAlbums = artistCursor.getInt(artistCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS));

                Artist artist = new Artist(getActivity(), artistName, id, numberOfTracks, numberOfAlbums);

                arrayList.add(artist);
            } while (artistCursor.moveToNext());
        }
        artistAdapter = new ArtistAdapter(getActivity(), arrayList);
        listView.setAdapter(artistAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!in_detail_view_artist) {
                    String s = artistAdapter.getItem(position).getArtistName();
                    artistCursor.moveToPosition(arrayList.indexOf(s));

                    Uri uri1 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    String selection = MediaStore.Audio.Albums.ARTIST + " = ?";
                    String selectrionArgs[] = {s};

                    tempSongList = new ArrayList<Song>();
                    Cursor songCursor = getActivity().getContentResolver().query(uri1, null, selection, selectrionArgs, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                    if (songCursor != null && songCursor.moveToFirst()) {
                        int songId = songCursor.getColumnIndex((MediaStore.Audio.Media._ID));
                        int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                        int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                        int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                        int songAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID);

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

                    in_detail_view_artist = true;
                    getActivity().invalidateOptionsMenu();
                }else {
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
        in_detail_view_artist = false;
        listView.setAdapter(artistAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        MenuItem closeItem = menu.findItem(R.id.close);

        if (!in_detail_view_artist) {
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
                    artistAdapter.getFilter().filter(newText);
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
                in_detail_view_artist = false;
                if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("show_track_as", true)){
                    gridView.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }else {
                    listViewDetailMode.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
                listView.setAdapter(artistAdapter);
                artistAdapter.getFilter().filter("");
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
