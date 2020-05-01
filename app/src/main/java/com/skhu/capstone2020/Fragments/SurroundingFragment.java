package com.skhu.capstone2020.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.skhu.capstone2020.Adapter.PlacesListAdapter;
import com.skhu.capstone2020.Model.PlaceResponse;
import com.skhu.capstone2020.R;
import com.skhu.capstone2020.REST_API.Client;
import com.skhu.capstone2020.REST_API.KakaoLocalApi;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurroundingFragment extends Fragment {
    private RecyclerView surrounding_place_recycler;
    private PlaceResponse placeResponse;
    private KakaoLocalApi api;
    private LocationManager lm;
    private PlacesListAdapter adapter;

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

        getPlaces();

        return view;
    }

    private void getPlaces() {
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
                api = Client.getClient(KakaoLocalApi.base).create(KakaoLocalApi.class);
                api.getPlaces(KakaoLocalApi.key, Double.toString(location.getLongitude()), Double.toString(location.getLatitude()), "FD6", 200, "accuracy")
                        .enqueue(new Callback<PlaceResponse>() {
                            @Override
                            public void onResponse(@NotNull Call<PlaceResponse> call, @NotNull Response<PlaceResponse> response) {
                                if (!(response.isSuccessful())) {
                                    Toast.makeText(getContext(), Integer.toString(response.code()), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                placeResponse = response.body();
                                if (placeResponse != null) {
                                    adapter = new PlacesListAdapter(placeResponse.getPlaceList(), getContext());
                                    surrounding_place_recycler.setAdapter(adapter);
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call<PlaceResponse> call, @NotNull Throwable t) {
                                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }
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
