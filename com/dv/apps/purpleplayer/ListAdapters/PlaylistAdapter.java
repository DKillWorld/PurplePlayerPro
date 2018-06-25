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

import com.dv.apps.purpleplayer.Models.Playlist;
import com.dv.apps.purpleplayer.R;
import com.github.florent37.viewanimator.ViewAnimator;

import java.util.ArrayList;

/**
 * Created by Dhaval on 24-10-2017.
 */

public class PlaylistAdapter extends ArrayAdapter<Playlist>{

    Context context;
    private ArrayList<Playlist> playlistList, backupList;
    private String playlistName;


    public PlaylistAdapter(Context context, ArrayList<Playlist> playlistList){
        super(context, 0, playlistList);
        this.context = context;
        this.playlistList = playlistList;
        this.backupList = playlistList;
    }

    class Myholder{
        ImageView imageView;
        TextView textView;
        Myholder(View view) {
            textView = (TextView) view.findViewById(R.id.songName);
            imageView = (ImageView) view.findViewById(R.id.image_view);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        Myholder myholder = null;
        playlistName = getItem(position).getPlaylistName();
        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
            myholder = new Myholder(view);
            view.setTag(myholder);
        }else {
            myholder = (Myholder) view.getTag();
        }



        myholder.textView.setText(playlistName);


//        myholder.imageView.setImageURI(song.getImage());
//        Glide.with(context)
//                .load(song.getImage())
//                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher))
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .into(myholder.imageView);

        myholder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_drawer_playlist));
//        Picasso.with(context)
//                .load(R.drawable.ic_drawer_genre)
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
                ArrayList<String> tempList = new ArrayList<String>();
                for (int i = 0, l = backupList.size(); i < l; i++){
                    playlistName = backupList.get(i).getPlaylistName();
                    if (playlistName.toLowerCase().contains(constraint.toString().toLowerCase())){
                        tempList.add(playlistName);
                    }
                }
                Log.i("albumlist", "" + playlistList.size());
                results.count = tempList.size();
                results.values = tempList;
            }else {
                synchronized (this){
                    results.values = playlistList;
                    results.count = playlistList.size();
                }
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            playlistList = (ArrayList<Playlist>) results.values;
            if (playlistList != null) {
                if (playlistList.size() > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    };

    @Nullable
    @Override
    public Playlist getItem(int position) {
        return playlistList.get(position);
    }

    @Override
    public int getCount() {
        if (playlistList != null) {
            return playlistList.size();
        }else {
            return 0;
        }
    }


}
