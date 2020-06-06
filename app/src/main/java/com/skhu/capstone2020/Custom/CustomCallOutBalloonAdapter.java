package com.skhu.capstone2020.Custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.skhu.capstone2020.Model.PlaceResponse.Place;
import com.skhu.capstone2020.R;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;

public class CustomCallOutBalloonAdapter implements CalloutBalloonAdapter {
    private Context context;

    private View callOutBalloon;
    private ImageView callOut_balloon_category;
    private TextView callOut_balloon_placeName, callOut_balloon_address;

    @SuppressLint("InflateParams")
    public CustomCallOutBalloonAdapter(Context context) {
        Log.d("Test", "CustomCallOutBalloonAdapter constructor");
        this.context = context;
        callOutBalloon = LayoutInflater.from(context).inflate(R.layout.custom_callout_balloon, null);
        callOut_balloon_placeName = callOutBalloon.findViewById(R.id.callOut_balloon_placeName);
        callOut_balloon_address = callOutBalloon.findViewById(R.id.callOut_balloon_address);
        callOut_balloon_category = callOutBalloon.findViewById(R.id.callOut_balloon_category);
        callOut_balloon_category.setClipToOutline(true);
    }

    @Override
    public View getCalloutBalloon(MapPOIItem mapPOIItem) {
        Log.d("Test", "getCalloutBalloon");
        Place place = (Place) mapPOIItem.getUserObject();
        callOut_balloon_placeName.setText(place.getPlaceName());
        callOut_balloon_address.setText(place.getAddress());

        switch (place.getCategoryCode()) {
            case "FD6":
                callOut_balloon_category.setImageResource(R.drawable.ic_category_restaurant);
                callOut_balloon_category.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_restaurant_background));
                break;
            case "CS2":
            case "MT1":
                callOut_balloon_category.setImageResource(R.drawable.ic_category_market);
                callOut_balloon_category.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_market_background));
                break;
            case "CE7":
                callOut_balloon_category.setImageResource(R.drawable.ic_category_cafe);
                callOut_balloon_category.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_cafe_background));
                break;
            case "PM9":
                callOut_balloon_category.setImageResource(R.drawable.ic_category_pharmacy);
                callOut_balloon_category.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_pharmacy_background));
                break;
            case "BK9":
                callOut_balloon_category.setImageResource(R.drawable.ic_category_bank);
                callOut_balloon_category.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_bank_background));
                break;
            case "SW8":
                callOut_balloon_category.setImageResource(R.drawable.ic_category_subway_station);
                callOut_balloon_category.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_subway_station));
                break;
            case "HP8":
                callOut_balloon_category.setImageResource(R.drawable.ic_category_hospital);
                callOut_balloon_category.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_hospital_background));
                break;
            case "CT1":
                callOut_balloon_category.setImageResource(R.drawable.ic_category_culture);
                callOut_balloon_category.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_culture_background));
                break;
            case "AD5":
                callOut_balloon_category.setImageResource(R.drawable.ic_category_accommodation);
                callOut_balloon_category.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_accommodation_background));
                break;
            default:
                callOut_balloon_category.setImageResource(R.drawable.ic_category_default);
                callOut_balloon_category.setBackground(ActivityCompat.getDrawable(context, R.drawable.category_default_image_background));
                break;
        }

        return callOutBalloon;
    }

    @Override
    public View getPressedCalloutBalloon(MapPOIItem mapPOIItem) {
        Log.d("Test", "getPressedCalloutBalloon");
        return callOutBalloon;
    }
}
