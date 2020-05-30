package com.skhu.capstone2020.Service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.imperiumlabs.geofirestore.GeoFirestore;

public class TrackingService extends Service implements LocationListener {
    CollectionReference locationReference = FirebaseFirestore.getInstance().collection("Locations");
    GeoFirestore geoFirestore = new GeoFirestore(locationReference);

    LocationManager locationManager;
    Location currentLocation;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Test", "onStartCommand in TrackingService");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
        } else {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation != null) {
                Log.d("Test", "GPS_PROVIDER in TrackingService");
                geoFirestore.setLocation(currentUser.getUid(), new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
            } else {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (currentLocation != null) {
                    Log.d("Test", "NETWORK_PROVIDER in TrackingService");
                    geoFirestore.setLocation(currentUser.getUid(), new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
                }
            }
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5, this);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("Test", "onDestroy in TrackingService");
        super.onDestroy();
        //locationManager.removeUpdates(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Test", "onLocationChanged in TrackingService");
        if (location != null)
            geoFirestore.setLocation(currentUser.getUid(), new GeoPoint(location.getLatitude(), location.getLongitude()));
        else
            Log.d("Test", "location is null");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }
}