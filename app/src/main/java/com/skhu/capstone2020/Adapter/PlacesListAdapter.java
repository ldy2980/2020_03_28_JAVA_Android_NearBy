package com.skhu.capstone2020.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
            holder.place_name.setText(place.getPlaceName());
            holder.place_category.setText(place.getCategory());
            holder.place_group_category.setText(place.getGroupCategory());
            holder.place_address.setText(place.getAddress());

            holder.place_main_image.setPadding(0, 0, 0, 0);
            holder.place_main_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(context)
                    .load("https://img1.daumcdn.net/thumb/T800x0.q70/?fname=http%3A%2F%2Ft1.daumcdn.net%2Fplace%2F0DE4A16B703244D0A9D62143E18E6A48")
                    .error(R.drawable.ic_default_place_image)
                    .into(holder.place_main_image);

            if (!place.getPhone().isEmpty()) {
                holder.place_digit.setVisibility(View.VISIBLE);
                holder.place_digit.setText(place.getPhone());
            } /*else {
                holder.place_digit.setVisibility(View.INVISIBLE);
            }*/

            String distance = place.getDistance() + "m";
            holder.place_distance.setText(distance);
        }
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView place_name, place_category, place_group_category, place_digit, place_address, place_distance;
        ImageView place_main_image;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            place_name = itemView.findViewById(R.id.place_name);
            place_category = itemView.findViewById(R.id.place_category);
            place_group_category = itemView.findViewById(R.id.place_group_category);
            place_address = itemView.findViewById(R.id.place_address);
            place_distance = itemView.findViewById(R.id.place_distance);
            place_digit = itemView.findViewById(R.id.place_digit);

            place_main_image = itemView.findViewById(R.id.place_main_image);
            place_main_image.setClipToOutline(true);
        }
    }
}
