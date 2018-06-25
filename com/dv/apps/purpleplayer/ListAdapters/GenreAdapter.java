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

import com.dv.apps.purpleplayer.Models.Genre;
import com.dv.apps.purpleplayer.R;
import com.github.florent37.viewanimator.ViewAnimator;

import java.util.ArrayList;

/**
 * Created by Dhaval on 24-10-2017.
 */

public class GenreAdapter extends ArrayAdapter<Genre>{

    Context context;
    private ArrayList<Genre> genreList, backupList;
    private String genreName;


    public GenreAdapter(Context context, ArrayList<Genre> genreList){
        super(context, 0, genreList);
        this.context = context;
        this.genreList = genreList;
        this.backupList = genreList;
    }

    class Myholder{
        TextView textView;
        ImageView imageView;
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
        genreName = getItem(position).getGenreName();
        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
            myholder = new Myholder(view);
            view.setTag(myholder);
        }else {
            myholder = (Myholder) view.getTag();
        }



        myholder.textView.setText(genreName);


//        myholder.imageView.setImageURI(song.getImage());
//        Glide.with(context)
//                .load(song.getImage())
//                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher))
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .into(myholder.imageView);

        myholder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_drawer_genre));
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
                    genreName = backupList.get(i).getGenreName();
                    if (genreName.toLowerCase().contains(constraint.toString().toLowerCase())){
                        tempList.add(genreName);
                    }
                }
                Log.i("albumlist", "" + genreList.size());
                results.count = tempList.size();
                results.values = tempList;
            }else {
                synchronized (this){
                    results.values = genreList;
                    results.count = genreList.size();
                }
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            genreList = (ArrayList<Genre>) results.values;
            if (genreList != null) {
                if (genreList.size() > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    };

    @Nullable
    @Override
    public Genre getItem(int position) {
        return genreList.get(position);
    }

    @Override
    public int getCount() {
        if (genreList != null) {
            return genreList.size();
        }else {
            return 0;
        }
    }

}
