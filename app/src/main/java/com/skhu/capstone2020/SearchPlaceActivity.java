package com.skhu.capstone2020;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.skhu.capstone2020.Model.AddressResponse;
import com.skhu.capstone2020.Model.RoadAddress;
import com.skhu.capstone2020.REST_API.KakaoLocalApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchPlaceActivity extends AppCompatActivity {
    private GridLayout grid_container;
    private RelativeLayout place_search_my_location;
    private FrameLayout place_search_input_container;
    private ImageView place_search_toggle_btn;
    private TextView place_search_show_my_location;

    private LocationManager locationManager;
    private Location currentLocation;

    private Retrofit retrofit;
    private KakaoLocalApi api;
    private AddressResponse addressResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_place);

        place_search_show_my_location = findViewById(R.id.place_search_show_my_location);
        place_search_input_container = findViewById(R.id.place_search_input_container);
        place_search_my_location = findViewById(R.id.place_search_my_location);
        grid_container = findViewById(R.id.grid_container);
        place_search_toggle_btn = findViewById(R.id.place_search_toggle_btn);
        place_search_toggle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Test", "onClick");
                if (grid_container.getVisibility() == View.VISIBLE) {
                    grid_container.setVisibility(View.GONE);
                    place_search_my_location.setVisibility(View.GONE);
                    place_search_toggle_btn.setImageResource(R.drawable.ic_arrow_down);
                } else {
                    grid_container.setVisibility(View.VISIBLE);
                    place_search_my_location.setVisibility(View.VISIBLE);
                    place_search_toggle_btn.setImageResource(R.drawable.ic_arrow_up);
                }
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        } else {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation == null) {
                Log.d("Test", "Network location");
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (currentLocation != null)
                    getAddress(currentLocation);
            } else {
                Log.d("Test", "GPS location");
                getAddress(currentLocation);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_top);
    }

    private void getAddress(Location currentLocation) {
        Log.d("Test", "getAddress");
        retrofit = new Retrofit.Builder()
                .baseUrl(KakaoLocalApi.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KakaoLocalApi.class);
        api.getAddress(KakaoLocalApi.key, Double.toString(currentLocation.getLongitude()), Double.toString(currentLocation.getLatitude()))
                .enqueue(new Callback<AddressResponse>() {
                    @Override
                    public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {
                        Log.d("Test", "onResponse in SearchPlaceActivity");
                        if (!(response.isSuccessful())) {
                            Toast.makeText(SearchPlaceActivity.this, response.code() + ", " + response.message(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        addressResponse = response.body();
                        if (addressResponse != null) {
                            RoadAddress roadAddress = addressResponse.getAddressDocuments().get(0).getRoadAddress();
                            place_search_show_my_location.setText(roadAddress.getAddress_name());
                        }
                    }

                    @Override
                    public void onFailure(Call<AddressResponse> call, Throwable t) {
                        Log.d("Test", "onFailure in SearchPlaceActivity");
                        Toast.makeText(SearchPlaceActivity.this, t.getMessage(), Toast.LENGTH_LONG);
                    }
                });
    }
}
