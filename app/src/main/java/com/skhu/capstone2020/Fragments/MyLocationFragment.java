package com.skhu.capstone2020.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin;
import com.skhu.capstone2020.Model.Place;
import com.skhu.capstone2020.Model.PlaceResponse;
import com.skhu.capstone2020.PlaceDetailActivity;
import com.skhu.capstone2020.R;
import com.skhu.capstone2020.REST_API.KakaoLocalApi;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

import static android.os.Looper.getMainLooper;

public class MyLocationFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {
    private RelativeLayout fragment_container;
    private Location currentLocation;

    private LocationManager locationManager;
    private Retrofit retrofit;
    private KakaoLocalApi api;

    private CameraPosition cameraPosition;
    private MapView mapView;
    private MapboxMap fragmentMapBoxMap;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);

    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 5000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    private String[] categories = {"FD6", "CS2", "MT1", "CE7", "BK9", "SW8", "AD5", "CT1"};
    //private String[] categories = {"FD6", "MT1", "CE7", "SW8", "CT1", "AT4"};
    private PlaceResponse placeResponse;
    private List<Place> placeList = new ArrayList<>();

    public MyLocationFragment() {
    }

    public MyLocationFragment(RelativeLayout fragment_container) {
        this.fragment_container = fragment_container;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(Objects.requireNonNull(getContext()), getString(R.string.mapbox_access_token));
        View view = inflater.inflate(R.layout.fragment_mylocation, container, false);

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permissions not granted", Toast.LENGTH_LONG).show();
        } else {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation == null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                fragmentMapBoxMap = mapboxMap;
                if (currentLocation != null) {
                    for (String code : categories) {
                        setNearByPlaceMarkers(mapboxMap, currentLocation, code);
                    }
                    cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                            .zoom(16)
                            .build();
                }
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1300);
                mapboxMap.setStyle(new Style.Builder().fromUri("asset://local_style.json"), new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        LocalizationPlugin localizationPlugin = new LocalizationPlugin(mapView, mapboxMap, style);
                        try {
                            localizationPlugin.matchMapLanguageWithDeviceDefault();
                        } catch (RuntimeException exception) {
                            Timber.d(exception.toString());
                        }
                        UiSettings uiSettings = mapboxMap.getUiSettings();
                        uiSettings.setCompassMargins(50, 350, 50, 50);
                        enableLocationComponent(style);
                    }
                });

                mapboxMap.setOnInfoWindowClickListener(new MapboxMap.OnInfoWindowClickListener() {
                    @Override
                    public boolean onInfoWindowClick(@NonNull Marker marker) {
                        Log.d("Test", "onInfoWindowClick, " + marker.getTitle());
                        if (placeList.size() != 0)
                            for (Place place : placeList) {
                                Log.d("Test", "placeResponse.getPlaceList is not null");
                                if (place.getPlaceName().trim().equals(marker.getTitle().trim()) ||
                                        place.getAddress().trim().equals(marker.getSnippet().trim())) {
                                    Log.d("Test", "matched");
                                    Intent intent = new Intent(getContext(), PlaceDetailActivity.class);
                                    intent.putExtra("url", place.getUrl());
                                    intent.putExtra("placeName", place.getPlaceName());
                                    startActivity(intent);
                                }
                            }
                        return true;
                    }
                });
            }
        });
        return view;
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(Objects.requireNonNull(getContext()))) {
            LocationComponent locationComponent = fragmentMapBoxMap.getLocationComponent();          // Get an instance of the component
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(getContext(), loadedMapStyle)        // Set the LocationComponent activation options
                            .useDefaultLocationEngine(false)
                            .build();

            locationComponent.activateLocationComponent(locationComponentActivationOptions);        // Activate with the LocationComponentActivationOptions object
            locationComponent.setLocationComponentEnabled(true);                                    // Enable to make component visible
            locationComponent.setCameraMode(CameraMode.TRACKING);                                   // Set the component's camera mode
            locationComponent.setRenderMode(RenderMode.COMPASS);                                    // Set the component's render mode

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(MyLocationFragment.this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(Objects.requireNonNull(getContext()));

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            fragmentMapBoxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(getContext(), "Permissions not granted", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
    }

    private static class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {
        private final WeakReference<MyLocationFragment> activityWeakReference;

        LocationChangeListeningActivityLocationCallback(MyLocationFragment locationFragment) {
            this.activityWeakReference = new WeakReference<>(locationFragment);
        }

        //The LocationEngineCallback interface's method which fires when the device's location has changed.
        //@param result the LocationEngineResult object which has the last known location within it.
        @Override
        public void onSuccess(LocationEngineResult result) {
            Log.d("Test", "onSuccess in MyLocationFragment");
            MyLocationFragment locationFragment = activityWeakReference.get();
            if (locationFragment != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                if (locationFragment.fragmentMapBoxMap != null && result.getLastLocation() != null) {    // Pass the new location to the Maps SDK's LocationComponent
                    locationFragment.fragmentMapBoxMap.getLocationComponent()
                            .zoomWhileTracking(16, 1300);
                    locationFragment.fragmentMapBoxMap.getLocationComponent()
                            .forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        // The LocationEngineCallback interface's method which fires when the device's location can't be captured
        // @param exception the exception message
        @Override
        public void onFailure(@NonNull Exception exception) {
            MyLocationFragment locationFragment = activityWeakReference.get();
            if (locationFragment != null) {
                Toast.makeText(locationFragment.getContext(), exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setNearByPlaceMarkers(MapboxMap mapboxMap, Location location, String code) {
        Log.d("Test", "setNearByPlaceMarkers");
        IconFactory iconFactory = IconFactory.getInstance(Objects.requireNonNull(getContext()));
        Icon markerRestaurant = iconFactory.fromResource(R.drawable.marker_restaurant_small);
        Icon markerMarket = iconFactory.fromResource(R.drawable.marker_market_small);
        Icon markerBank = iconFactory.fromResource(R.drawable.marker_bank_small);
        Icon markerCafe = iconFactory.fromResource(R.drawable.marker_cafe_small);
        Icon markerHospital = iconFactory.fromResource(R.drawable.marker_hospital_small);
        Icon markerPharmacy = iconFactory.fromResource(R.drawable.marker_pharmacy_small);
        Icon markerSubWay = iconFactory.fromResource(R.drawable.marker_subway_small);
        Icon markerCulture = iconFactory.fromResource(R.drawable.marker_culture_small);
        Icon markerAccommodation = iconFactory.fromResource(R.drawable.marker_accommodation_small);
        retrofit = new Retrofit.Builder()
                .baseUrl(KakaoLocalApi.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KakaoLocalApi.class);
        api.getPlaces(KakaoLocalApi.key, Double.toString(location.getLongitude()), Double.toString(location.getLatitude()), code, KakaoLocalApi.radius, "accuracy")
                .enqueue(new Callback<PlaceResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<PlaceResponse> call, @NotNull Response<PlaceResponse> response) {
                        Log.d("Test", "onResponse");
                        if (!(response.isSuccessful())) {
                            Toast.makeText(getContext(), Integer.toString(response.code()), Toast.LENGTH_LONG).show();
                            return;
                        }
                        placeResponse = response.body();
                        if (placeResponse != null) {
                            placeList.addAll(placeResponse.getPlaceList());
                            for (Place place : placeList) {
                                switch (place.getCategoryCode()) {
                                    case "FD6":
                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(Double.parseDouble(place.getY()), Double.parseDouble(place.getX())))
                                                .icon(markerRestaurant)
                                                .setTitle(place.getPlaceName().trim()))
                                                .setSnippet(place.getAddress());
                                        continue;
                                    case "CS2":
                                    case "MT1":
                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(Double.parseDouble(place.getY()), Double.parseDouble(place.getX())))
                                                .icon(markerMarket)
                                                .setTitle(place.getPlaceName().trim()))
                                                .setSnippet(place.getAddress());
                                        continue;
                                    case "BK9":
                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(Double.parseDouble(place.getY()), Double.parseDouble(place.getX())))
                                                .icon(markerBank)
                                                .setTitle(place.getPlaceName().trim()))
                                                .setSnippet(place.getAddress());
                                        continue;
                                    case "CE7":
                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(Double.parseDouble(place.getY()), Double.parseDouble(place.getX())))
                                                .icon(markerCafe)
                                                .setTitle(place.getPlaceName().trim()))
                                                .setSnippet(place.getAddress());
                                        continue;
                                    case "HP8":
                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(Double.parseDouble(place.getY()), Double.parseDouble(place.getX())))
                                                .icon(markerHospital)
                                                .setTitle(place.getPlaceName().trim()))
                                                .setSnippet(place.getAddress());
                                        continue;
                                    case "PM9":
                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(Double.parseDouble(place.getY()), Double.parseDouble(place.getX())))
                                                .icon(markerPharmacy)
                                                .setTitle(place.getPlaceName().trim()))
                                                .setSnippet(place.getAddress());
                                        continue;
                                    case "SW8":
                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(Double.parseDouble(place.getY()), Double.parseDouble(place.getX())))
                                                .icon(markerSubWay)
                                                .setTitle(place.getPlaceName().trim()))
                                                .setSnippet(place.getAddress());
                                        continue;
                                    case "AT4":
                                    case "CT1":
                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(Double.parseDouble(place.getY()), Double.parseDouble(place.getX())))
                                                .icon(markerCulture)
                                                .setTitle(place.getPlaceName().trim()))
                                                .setSnippet(place.getAddress());
                                        break;
                                    case "AD5":
                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(Double.parseDouble(place.getY()), Double.parseDouble(place.getX())))
                                                .icon(markerAccommodation)
                                                .setTitle(place.getPlaceName().trim()))
                                                .setSnippet(place.getAddress());
                                        break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<PlaceResponse> call, @NotNull Throwable t) {
                        Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
