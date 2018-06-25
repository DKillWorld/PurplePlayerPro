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

import com.dv.apps.purpleplayer.Models.Artist;
import com.dv.apps.purpleplayer.R;
import com.github.florent37.viewanimator.ViewAnimator;

import java.util.ArrayList;

/**
 * Created by Dhaval on 24-10-2017.
 */

public class ArtistAdapter extends ArrayAdapter<Artist> {

    private String artistName, albumTime;
    private ArrayList<Artist> artistList, backupList;
    private Artist artist;
    private Context context;

    public ArtistAdapter(Context context, ArrayList<Artist> artistList){
        super(context, 0, artistList);
        this.context = context;
        this.artistList = artistList;
        this.backupList = artistList;
    }

    class Myholder{
        TextView textView;
        TextView textView2;
        TextView textView3;
        ImageView imageView;
        Myholder(View view) {
            textView = (TextView) view.findViewById(R.id.songName);
            textView2 = (TextView) view.findViewById(R.id.songArtist);
//            textView3 = (TextView) view.findViewById(R.id.songDuration);
            imageView = (ImageView) view.findViewById(R.id.image_view);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        Myholder myholder = null;
        artist = getItem(position);
        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
            myholder = new Myholder(view);
            view.setTag(myholder);
        }else {
            myholder = (Myholder) view.getTag();
        }



        myholder.textView.setText(artist.getArtistName());
        if (artist.getNumberOfSongs() == 1) {
            myholder.textView2.setText(artist.getNumberOfSongs() + " Song");
        }else {
            myholder.textView2.setText(artist.getNumberOfSongs() + " Songs");
        }


//        myholder.imageView.setImageURI(song.getImage());
//        Glide.with(context)
//                .load(song.getImage())
//                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher))
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .into(myholder.imageView);

        myholder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_drawer_artist));
//        Picasso.with(context)
//                .load(R.drawable.ic_drawer_artist)
//                .into(myholder.imageView);



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
                ArrayList<Artist> tempList = new ArrayList<Artist>();
                for (int i = 0, l = backupList.size(); i < l; i++){
                    Artist tempArtist = backupList.get(i);
                    if (tempArtist.getArtistName().toLowerCase().contains(constraint.toString().toLowerCase())){
                        tempList.add(tempArtist);
                    }
                }
                Log.i("albumlist", "" + artistList.size());
                results.count = tempList.size();
                results.values = tempList;
            }else {
                synchronized (this){
                    results.values = artistList;
                    results.count = artistList.size();
                }
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            artistList = (ArrayList<Artist>) results.values;
            if (artistList != null) {
                if (artistList.size() > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    };

    @Nullable
    @Override
    public Artist getItem(int position) {
        return artistList.get(position);
    }

    @Override
    public int getCount() {
        if (artistList != null) {
            return artistList.size();
        }else {
            return 0;
        }
    }
}
