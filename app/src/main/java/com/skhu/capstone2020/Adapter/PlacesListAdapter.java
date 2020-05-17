package com.skhu.capstone2020.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.skhu.capstone2020.Model.PlaceResponse.Place;
import com.skhu.capstone2020.PlaceDetailActivity;
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
            holder.place_digit.setText((place.getPhone()));
            holder.place_address.setText(place.getAddress());

            if (place.getDistance().trim().length() == 0) {
                holder.place_distance.setVisibility(View.INVISIBLE);
            } else {
                String distance = place.getDistance() + "m";
                holder.place_distance.setText(distance);
            }

            switch (place.getCategoryCode()) {
                case "FD6":
                    holder.place_category_icon.setImageResource(R.drawable.ic_category_restaurant);
                    holder.place_category_icon.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_restaurant_background));
                    break;
                case "CS2":
                case "MT1":
                    holder.place_category_icon.setImageResource(R.drawable.ic_category_market);
                    holder.place_category_icon.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_market_background));
                    break;
                case "CE7":
                    holder.place_category_icon.setImageResource(R.drawable.ic_category_cafe);
                    holder.place_category_icon.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_cafe_background));
                    break;
                case "PM9":
                    holder.place_category_icon.setImageResource(R.drawable.ic_category_pharmacy);
                    holder.place_category_icon.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_pharmacy_background));
                    break;
                case "BK9":
                    holder.place_category_icon.setImageResource(R.drawable.ic_category_bank);
                    holder.place_category_icon.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_bank_background));
                    break;
                case "SW8":
                    holder.place_category_icon.setImageResource(R.drawable.ic_category_subway_station);
                    holder.place_category_icon.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_subway_station));
                    break;
                case "HP8":
                    holder.place_category_icon.setImageResource(R.drawable.ic_category_hospital);
                    holder.place_category_icon.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_hospital_background));
                    break;
                case "CT1":
                    holder.place_category_icon.setImageResource(R.drawable.ic_category_culture);
                    holder.place_category_icon.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_culture_background));
                    break;
                case "AD5":
                    holder.place_category_icon.setImageResource(R.drawable.ic_category_accommodation);
                    holder.place_category_icon.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_accommodation_background));
                    break;
                default:
                    holder.place_category_icon.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (placeList.size() != 0)
            return placeList.size();
        else
            return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView place_name, place_category, place_group_category, place_digit, place_address, place_distance;
        ImageView place_category_icon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            place_name = itemView.findViewById(R.id.place_name);
            place_category = itemView.findViewById(R.id.place_category);
            place_group_category = itemView.findViewById(R.id.place_group_category);
            place_address = itemView.findViewById(R.id.place_address);
            place_distance = itemView.findViewById(R.id.place_distance);
            place_digit = itemView.findViewById(R.id.place_digit);
            place_category_icon = itemView.findViewById(R.id.place_category_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {                                                            // 상세 정보 페이지 화면으로 이동
            Place place = placeList.get(getAdapterPosition());
            Intent intent = new Intent(view.getContext(), PlaceDetailActivity.class);
            intent.putExtra("url", place.getUrl());
            intent.putExtra("placeName", place.getPlaceName());
            view.getContext().startActivity(intent);
        }
    }
}
