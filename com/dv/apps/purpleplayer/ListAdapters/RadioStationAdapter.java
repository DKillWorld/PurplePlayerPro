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

import com.dv.apps.purpleplayer.Models.RadioStation;
import com.dv.apps.purpleplayer.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Dhaval on 31-12-2017.
 */

public class RadioStationAdapter extends ArrayAdapter<RadioStation> {

    private ArrayList<RadioStation> radioStations, backupList;
    private RadioStation radioStation;
    private Context context;

    public RadioStationAdapter(@NonNull Context context, ArrayList<RadioStation> radioStations) {
        super(context,0, radioStations);
        this.radioStations = radioStations;
        this.backupList = radioStations;
        this.context = context;
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
        RadioStation radioStation = getItem(position);
        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
            myholder = new Myholder(view);
            view.setTag(myholder);
        }else {
            myholder = (Myholder) view.getTag();
        }


        myholder.textView.setText(radioStation.getTitle());
        myholder.textView2.setText(radioStation.getArtist() + "");
        myholder.textView3.setText(radioStation.getDuration() + "");
//        myholder.imageView.setImageURI(song.getImage());
//        Glide.with(context)
//                .load(song.getImage())
//                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher))
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .into(myholder.imageView);

        Picasso.with(context)
                .load(R.mipmap.ic_launcher)
                .into(myholder.imageView);


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
                ArrayList<RadioStation> tempList = new ArrayList<RadioStation>();
                for (int i = 0, l = backupList.size(); i < l; i++){
                    RadioStation radioStation = backupList.get(i);
                    if (radioStation.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())
                            || radioStation.getArtist().toLowerCase().contains(constraint.toString().toLowerCase())){
                        tempList.add(radioStation);
                    }
                }
                Log.i("songlist", "" + radioStations.size());
                results.count = tempList.size();
                results.values = tempList;
            }else {
                synchronized (this){
                    results.values = radioStations;
                    results.count = radioStations.size();
                }
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            radioStations = (ArrayList<RadioStation>) results.values;
            if (radioStations != null) {
                if (radioStations.size() > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    };

    @Nullable
    @Override
    public RadioStation getItem(int position) {
        return radioStations.get(position);
    }

    @Override
    public int getCount() {
        if (radioStations != null) {
            return radioStations.size();
        }else {
            return 0;
        }
    }
}
