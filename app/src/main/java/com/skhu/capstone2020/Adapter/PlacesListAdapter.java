package com.skhu.capstone2020.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skhu.capstone2020.Model.Place;
import com.skhu.capstone2020.R;

import java.util.List;

public class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.ViewHolder> {
    private List<Place> placeList;
    private Context context;

    public PlacesListAdapter(List<Place> placeList, Context context) {
        this.placeList = placeList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (placeList.size() != 0) {
            Place place = placeList.get(position);
            holder.place_id.setText(place.getPlaceId());
            holder.place_name.setText(place.getPlaceName());
            holder.place_category.setText(place.getCategory());
            holder.place_group_category.setText(place.getGroupCategory());
            holder.place_phone_number.setText(place.getPhone());
            holder.place_url.setText(place.getUrl());
            holder.place_address.setText(place.getAddress());
            holder.place_distance.setText(place.getDistance());
            holder.place_x.setText(place.getX());
            holder.place_y.setText(place.getY());
        }
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView place_id, place_name, place_category, place_group_category, place_phone_number, place_url, place_address, place_distance, place_x, place_y;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            place_id = itemView.findViewById(R.id.place_id);
            place_name = itemView.findViewById(R.id.place_name);
            place_category = itemView.findViewById(R.id.place_category);
            place_group_category = itemView.findViewById(R.id.place_group_category);
            place_phone_number = itemView.findViewById(R.id.place_phone_number);
            place_url = itemView.findViewById(R.id.place_url);
            place_address = itemView.findViewById(R.id.place_address);
            place_distance = itemView.findViewById(R.id.place_distance);
            place_x = itemView.findViewById(R.id.place_x);
            place_y = itemView.findViewById(R.id.place_y);
        }
    }
}
