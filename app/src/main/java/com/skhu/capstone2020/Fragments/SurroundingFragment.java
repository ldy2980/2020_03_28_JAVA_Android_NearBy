package com.skhu.capstone2020.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.skhu.capstone2020.Adapter.PlacesListAdapter;
import com.skhu.capstone2020.Model.Place;
import com.skhu.capstone2020.Model.PlaceResponse;
import com.skhu.capstone2020.R;
import com.skhu.capstone2020.REST_API.KakaoLocalApi;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SurroundingFragment extends Fragment {
    private RecyclerView surrounding_place_recycler;
    private SpinKitView surrounding_spinKitView;

    private PlaceResponse placeResponse;
    private PlacesListAdapter adapter;
    private List<Place> placeList = new ArrayList<>();
    private String[] categories = {"FD6", "CS2", "MT1", "CE7", "PM9", "BK9", "SW8", "HP8", "CT1"};

    private Retrofit retrofit;
    private KakaoLocalApi api;
    private LocationManager lm;

    private RelativeLayout fragment_container;

    public SurroundingFragment() {
    }

    public SurroundingFragment(RelativeLayout container) {
        this.fragment_container = container;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_surrounding, container, false);
        surrounding_place_recycler = view.findViewById(R.id.surrounding_place_recycler);
        surrounding_place_recycler.setHasFixedSize(true);
        surrounding_place_recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PlacesListAdapter(placeList, getContext());
        surrounding_place_recycler.setAdapter(adapter);

        surrounding_spinKitView = view.findViewById(R.id.surrounding_spinKitView);

        setList();

        return view;
    }

    private void setList() {
        Log.d("Test", "setList");
        lm = (LocationManager) Objects.requireNonNull(getContext()).getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            TedPermission.with(getContext())                                                        // 앱 접근 권한 설정
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("앱 사용을 위해 접근 권한이 필요합니다.\n\n[설정]에서 권한을 허용해주세요.")
                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    .check();
        } else {
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                Log.d("Test", "GPS location");
                for (String category : categories) {
                    getPlaces(location, category);
                }
            } else {
                Log.d("Test", "Network location");
                Location networkLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (networkLocation != null)
                    for (String category : categories) {
                        getPlaces(networkLocation, category);
                    }
            }
        }
        setVisible();
    }

    private void getPlaces(Location location, String category) {
        Log.d("Test", "getPlaces");
        retrofit = new Retrofit.Builder()
                .baseUrl(KakaoLocalApi.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KakaoLocalApi.class);
        api.getPlaces(KakaoLocalApi.key, Double.toString(location.getLongitude()), Double.toString(location.getLatitude()), category, 400, "accuracy")
                .enqueue(new Callback<PlaceResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<PlaceResponse> call, @NotNull Response<PlaceResponse> response) {
                        if (!(response.isSuccessful())) {
                            Toast.makeText(getContext(), Integer.toString(response.code()), Toast.LENGTH_LONG).show();
                            return;
                        }

                        placeResponse = response.body();
                        if (placeResponse != null) {
                            placeList.addAll(placeResponse.getPlaceList());
                            Collections.sort(placeList, new Comparator<Place>() {                                       // 거리순 정렬
                                @Override
                                public int compare(Place p1, Place p2) {
                                    return p1.getDistance().compareTo(p2.getDistance());
                                }
                            });
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<PlaceResponse> call, @NotNull Throwable t) {
                        Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setVisible() {                                                              // 리사이클러뷰 Visibility 설정
        Log.d("Test", "setRecyclerAdapter");
        surrounding_place_recycler.setVisibility(View.VISIBLE);
        surrounding_spinKitView.setVisibility(View.INVISIBLE);
    }

    private PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(getContext(), "위치 권한 허용", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
        }
    };
}
