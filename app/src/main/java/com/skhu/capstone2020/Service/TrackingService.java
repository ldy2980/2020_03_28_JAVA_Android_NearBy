package com.skhu.capstone2020.Service;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.skhu.capstone2020.GroupActivity;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.PlaceResponse.Place;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.Model.UserOptions;
import com.skhu.capstone2020.Notification.OreoNotification;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TrackingService extends Service implements LocationListener {
    CollectionReference userReference = FirebaseFirestore.getInstance().collection("Users");
    CollectionReference locationReference = FirebaseFirestore.getInstance().collection("Locations");
    CollectionReference groupReference = FirebaseFirestore.getInstance().collection("Groups");
    CollectionReference userOptionReference = FirebaseFirestore.getInstance().collection("UserOptions");
    GeoFirestore geoFirestore = new GeoFirestore(locationReference);

    private UserOptions currentUserOption;

    public static GeoQuery query500;    // 반경 500m 범위 설정 지오쿼리

    private double range500 = 0.5;  //
    private double range300 = 0.3;  //
    private double range100 = 0.1;  // km 단위

    private Place destinationInfo;
    private Location destinationLocation = new Location("destinationLocation");
    private GroupInfo currentGroupInfo;
    public static List<Member> members;

    LocationManager locationManager;
    Location currentLocation;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    User user;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Test", "서비스 시작");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {    // 권한 체크
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
        } else {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation != null && currentUser != null) {
                Log.d("Test", "GPS_PROVIDER in TrackingService");
                geoFirestore.setLocation(currentUser.getUid(), new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
            } else {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);       // GPS 정보가 없다면 NETWORK 정보 할당
                if (currentLocation != null && currentUser != null) {
                    Log.d("Test", "NETWORK_PROVIDER in TrackingService");
                    geoFirestore.setLocation(currentUser.getUid(), new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {    // 권한 체크
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8, 10, this);

        userReference
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null && documentSnapshot.exists())
                            user = documentSnapshot.toObject(User.class);       // 현재 유저 정보 객체 가져오기
                    }
                });

        userOptionReference
                .document(currentUser.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null && documentSnapshot.exists())
                            currentUserOption = documentSnapshot.toObject(UserOptions.class);       // 현재 유저의 앱 설정 정보 객체 가져오기
                    }
                });

        groupReference
                .whereEqualTo("setDestination", true)   // 현재 존재하는 그룹들 중 목적지가 설정된 그룹이 있는지 확인
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.d("Test", "목적지 설정된 그룹 존재");
                        if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() != 0)
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                GroupInfo groupInfo = snapshot.toObject(GroupInfo.class);
                                if (groupInfo != null) {
                                    List<Member> memberList = groupInfo.getMemberList();
                                    for (int i = 0; i < memberList.size(); ++i)
                                        if (currentUser.getUid().equals(memberList.get(i).getId())) {   // 목적지가 설정된 그룹이 현재 유저가 속해있는 그룹이라면 지오쿼리 설정
                                            Log.d("Test", "현재 유저가 속한 그룹");
                                            members = groupInfo.getMemberList();
                                            currentGroupInfo = groupInfo;
                                            getDestinationInfo(groupInfo.getGroupId());     // 목적지 정보 객체 가져오기
                                        }
                                }
                            }
                    }
                });

        return START_REDELIVER_INTENT;
    }

    public void getDestinationInfo(String groupId) {            // 목적지 정보 객체 획득
        Log.d("Test", "getDestinationInfo 호출됨");
        groupReference
                .document(groupId)
                .collection("Destination")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() != 0) {
                            Place place = queryDocumentSnapshots.toObjects(Place.class).get(0);
                            destinationInfo = place;
                            destinationLocation.setLatitude(Double.parseDouble(destinationInfo.getY()));
                            destinationLocation.setLongitude(Double.parseDouble(destinationInfo.getX()));
                            GeoPoint geoPoint = new GeoPoint(Double.parseDouble(place.getY()), Double.parseDouble(place.getX()));
                            setGeoQuery500(geoPoint, range500); //  반경 500m 범위 설정
                        }
                    }
                });
    }

    public void setGeoQuery500(GeoPoint geoPoint, double radius) {
        Log.d("Test", "setGeoQuery500 호출됨: " + range500);
        query500 = geoFirestore.queryAtLocation(geoPoint, radius);      // 반경 500m 범위의 지오쿼리 설정
        query500.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(@NotNull String documentId, @NotNull GeoPoint geoPoint) {      // 유저가 해당 범위 안에 들어오면 호출
                Log.d("Test", "range500 onKeyEntered 호출");
                if (!documentId.equals(currentUser.getUid()))                   //
                    if (members != null) {                                      //
                        for (int i = 0; i < members.size(); ++i)                //
                            if (members.get(i).getId().equals(documentId)) {    // 범위 안에 들어온 유저가 현재 자신이 아니고 그룹에 속해있는 멤버일 경우
                                Log.d("Test", "멤버리스트와 일치하는 유저");
                                Log.d("Test", documentId + ", " + "(" + range500 + ")" + "범위 내 진입: " + "(" + geoPoint.getLatitude() + ", " + geoPoint.getLongitude() + ")");

                                if (currentUserOption != null && currentUserOption.isAllowNotification())
                                    sendPushNotification(members.get(i), range500);     // 푸시 알림 전송
                            }
                    } else {
                        Log.d("Test", "멤버리스트 null");
                    }
            }

            @Override
            public void onKeyExited(@NotNull String s) {
                Log.d("Test", s + ", " + range500 + " 범위 벗어남");
            }

            @Override
            public void onKeyMoved(@NotNull String documentId, @NotNull GeoPoint geoPoint) {    // 유저가 해당 범위 안에서 이동할 때 호출
                Log.d("Test", "range500 onKeyMoved 호출");
                if (!documentId.equals(currentUser.getUid()))                   //
                    if (members != null) {                                      //
                        for (int i = 0; i < members.size(); ++i)                //
                            if (members.get(i).getId().equals(documentId)) {    // 범위 안에 들어온 유저가 현재 자신이 아니고 그룹에 속해있는 멤버일 경우
                                Location currentLocation = new Location("currentLocation");
                                currentLocation.setLatitude(geoPoint.getLatitude());
                                currentLocation.setLongitude(geoPoint.getLongitude());

                                double distance = currentLocation.distanceTo(destinationLocation);      // 현재 위치와 목적지까지의 거리 계산

                                if (distance >= 100 && distance <= 300 && currentUserOption != null && currentUserOption.isAllowNotification())     // 남은 거리에 따라 각각 푸시알림 전송
                                    sendPushNotification(members.get(i), range300);
                                else if (distance <= 100 && currentUserOption != null && currentUserOption.isAllowNotification())           //
                                    sendPushNotification(members.get(i), range100);

                                Log.d("Test", "range500 범위 내 위치 이동됨: " + "(" + geoPoint.getLatitude() + ", " + geoPoint.getLongitude() + ")");
                                Log.d("Test", documentId + ", 목적지까지 남은 거리: " + distance + "m");
                            }
                    } else {
                        Log.d("Test", "멤버리스트 null");
                    }
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("Test", "500 지오쿼리 준비");
            }

            @Override
            public void onGeoQueryError(@NotNull Exception e) {
            }
        });
    }

    public static void removeAllGeoQueries() {      // 현재 설정된 모든 지오쿼리 삭제
        if (query500 != null) {
            Log.d("Test", "removeAllGeoQueries 호출됨");
            query500.removeAllListeners();
        }
    }

    public void sendPushNotification(Member member, double range) {
        Log.d("Test", "sendPushNotification 호출");
        String distance = "";
        if (range == 0.5)       // 현재 범위 정보에 따라 distance의 값 설정
            distance = "500m";
        else if (range == 0.3)
            distance = "300m";
        else if (range == 0.1)
            distance = "100m";
        else
            Log.d("Test", "범위 정보 null");

        String groupName = "";
        if (currentGroupInfo != null)
            groupName = currentGroupInfo.getGroupName();    // 그룹 이름 설정
        else
            Log.d("Test", "그룹정보 객체 null");

        String messageBody = member.getName() + " (이)가 목적지까지 약 " + distance + "남았습니다.";
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(this, GroupActivity.class);
        if (currentGroupInfo != null)
            intent.putExtra("groupInfo", currentGroupInfo);     // 그룹 정보 전달
        else
            Log.d("Test", "그룹 정보 null");

        if (user != null)
            intent.putExtra("currentUser", user);               // 현재 유저 정보 전달
        else
            Log.d("Test", "현재 유저정보 null");

        if (destinationInfo != null)
            intent.putExtra("destinationInfo", destinationInfo);    // 목적지 정보 전달
        else
            Log.d("Test", "목적지 정보 null");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getRequestNotification(groupName, messageBody, pendingIntent, defaultSound);
        oreoNotification.getManager().notify(0, builder.build());
    }

    @Override
    public void onDestroy() {
        Log.d("Test", "서비스 종료");
        super.onDestroy();
        //locationManager.removeUpdates(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {      // 현재 유저의 위치가 변경되었을 때 호출
        Log.d("Test", "onLocationChanged 내 위치 변경됨");
        if (location != null && currentUser != null)
            geoFirestore.setLocation(currentUser.getUid(), new GeoPoint(location.getLatitude(), location.getLongitude()));  // 위치 정보를 DB에 저장
        else
            Log.d("Test", "위치 정보 찾을 수 없음. location is null");
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
