package com.dv.apps.purpleplayer.ListAdapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dv.apps.purpleplayer.Models.Album;
import com.dv.apps.purpleplayer.R;
import com.github.florent37.viewanimator.ViewAnimator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Dhaval on 24-10-2017.
 */

public class AlbumAdapter extends ArrayAdapter<Album> {

    private String albumName, albumTime;
    private ArrayList<Album> albumList, backupList;
    private Album album;
    private Context context;

    public AlbumAdapter(Context context, ArrayList<Album> albumList){
        super(context, 0, albumList);
        this.context = context;
        this.albumList = albumList;
        this.backupList = albumList;
    }

    class Myholder{
        TextView textView;
        TextView textView2;
        TextView textView3;
        ImageView imageView;
        Myholder(View view) {
            textView = (TextView) view.findViewById(R.id.songName);
            textView2 = (TextView) view.findViewById(R.id.songArtist);
            textView3 = (TextView) view.findViewById(R.id.songDuration);
            imageView = (ImageView) view.findViewById(R.id.image_view);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        Myholder myholder = null;
        album = getItem(position);
        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
            myholder = new Myholder(view);
            view.setTag(myholder);
        }else {
            myholder = (Myholder) view.getTag();
        }



        myholder.textView.setText(album.getAlbumName());
        if (album.getNumberOfSongs() == 1) {
            myholder.textView2.setText(album.getNumberOfSongs() + " Song");
        }else {
            myholder.textView2.setText(album.getNumberOfSongs() + " Songs");
        }
        if (album.getYear() != 0) {
            myholder.textView3.setText("Year: " + album.getYear());
        }

//        myholder.imageView.setImageURI(song.getImage());
//        Glide.with(context)
//                .load(song.getImage())
//                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher))
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .into(myholder.imageView);

        Picasso.with(context)
                .load(album.getImage())
                .placeholder(R.drawable.ic_drawer_album)
                .into(myholder.imageView);


        ViewAnimator
                .animate(view)
                .slideRight()
                .decelerate()
                .duration(300)
                .start();

        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return myFilter;
    }

    Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint.length() == 0){
                results.values = backupList;
                results.count = backupList.size();
            }
            else if (constraint != null && constraint.toString().length() > 0){
                ArrayList<Album> tempList = new ArrayList<Album>();
                for (int i = 0, l = backupList.size(); i < l; i++){
                    Album tempAlbum = backupList.get(i);
                    if (tempAlbum.getAlbumName().toLowerCase().contains(constraint.toString().toLowerCase())){
                        tempList.add(tempAlbum);
                    }
                }
                Log.i("albumlist", "" + albumList.size());
                results.count = tempList.size();
                results.values = tempList;
            }else {
                synchronized (this){
                    results.values = albumList;
                    results.count = albumList.size();
                }
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            albumList = (ArrayList<Album>) results.values;
            if (albumList != null) {
                if (albumList.size() > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    };

    @Nullable
    @Override
    public Album getItem(int position) {
        return albumList.get(position);
    }

    @Override
    public int getCount() {
        if (albumList != null) {
            return albumList.size();
        }else {
            return 0;
        }
    }
}
