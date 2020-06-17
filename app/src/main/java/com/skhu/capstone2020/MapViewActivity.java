package com.skhu.capstone2020;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.skhu.capstone2020.Adapter.ViewHolder.SelectMemberViewHolder;
import com.skhu.capstone2020.Custom.CustomCallOutBalloonAdapter;
import com.skhu.capstone2020.Interface.RecyclerItemClickListener;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.PlaceResponse.Place;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.Model.UserOptions;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;

public class MapViewActivity extends AppCompatActivity {
    private RecyclerView select_member_recycler;
    private TextView select_member_address;
    private ImageView btn_map_view_circle, btn_map_view_my_location, btn_map_view_destination;
    private LinearLayout range_view;

    private GroupInfo groupInfo;
    private User currentUser;
    private Place destinationInfo;

    private RecyclerView.Adapter<SelectMemberViewHolder> adapter;

    private CollectionReference locationReference = FirebaseFirestore.getInstance().collection("Locations");
    private CollectionReference userOptionReference = FirebaseFirestore.getInstance().collection("UserOptions");
    private CollectionReference userReference = FirebaseFirestore.getInstance().collection("Users");
    private TMapData tMapData = new TMapData();
    private TMapPoint destination_point;

    private String totalTime;

    private MapView mapView;
    private MapPOIItem myLocationMarker;

    private List<Member> memberList;
    private Member member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        groupInfo = (GroupInfo) getIntent().getSerializableExtra("groupInfo");  // 그룹 정보 객체 가져오기
        currentUser = (User) getIntent().getSerializableExtra("currentUser");   // 현재 유저 정보 객체 가져오기
        destinationInfo = (Place) getIntent().getSerializableExtra("destinationInfo");  // 목적지 정보 객체 가져오기

        range_view = findViewById(R.id.range_view);     // 목적지를 중심으로 하는 원의 반경을 표시하는 레이아웃

        btn_map_view_circle = findViewById(R.id.btn_map_view_circle);   // 목적지에 원을 추가/제거 하는 버튼
        btn_map_view_my_location = findViewById(R.id.btn_map_view_my_location);     // 지도의 중심점을 내 위치로 이동시키는 버튼
        btn_map_view_destination = findViewById(R.id.btn_map_view_destination);     // 지도의 중심점을 목적지로 이동시키는 버튼

        ImageView map_view_back = findViewById(R.id.map_view_back);
        map_view_back.setOnClickListener(new View.OnClickListener() {                       // 이전 화면으로 이동
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_top);
            }
        });

        if (destinationInfo != null)
            destination_point = new TMapPoint(Double.parseDouble(destinationInfo.getY()), Double.parseDouble(destinationInfo.getX()));      // 목적지의 위경도 값

        mapView = new MapView(this);
        mapView.setCalloutBalloonAdapter(new CustomCallOutBalloonAdapter(MapViewActivity.this));    // 커스텀 인포윈도우 설정
        Log.d("Test", "Set custom balloon");
        ViewGroup mapViewContainer = findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        setDestinationMarker(destinationInfo);    // 목적지 정보를 지도에 표시하기
        setMyLocationMarker();  // 현재 유저 위치를 지도에 표시하기

        btn_map_view_circle.setOnClickListener(new View.OnClickListener() {   // 목적지 위치에 원 범위 추가/제거
            @Override
            public void onClick(View view) {
                if (mapView.getCircles().length != 0) {     // 이미 원이 추가되어있다면 삭제
                    mapView.removeAllCircles();
                    btn_map_view_circle.setImageResource(R.drawable.ic_circle);
                    btn_map_view_circle.setBackground(ContextCompat.getDrawable(MapViewActivity.this, R.drawable.btn_map_view_background));
                    range_view.setVisibility(View.INVISIBLE);
                } else {
                    addCircles();       // 원이 없다면 추가
                    btn_map_view_circle.setImageResource(R.drawable.ic_circle_cancel);
                    btn_map_view_circle.setBackground(ContextCompat.getDrawable(MapViewActivity.this, R.drawable.btn_map_view_background_dark_blue));
                    range_view.setVisibility(View.VISIBLE);
                }
            }
        });

        btn_map_view_destination.setOnClickListener(new View.OnClickListener() {    // 지도의 중심점을 목적지 위치로 이동
            @Override
            public void onClick(View view) {
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(    // 중심점 설정
                        Double.parseDouble(destinationInfo.getY()),
                        Double.parseDouble(destinationInfo.getX())
                        ),
                        true);

                MapReverseGeoCoder geoCoder = new MapReverseGeoCoder("bd97169bbcb001a23368a7ed262a107c",    // 좌표 -> 주소 변환 설정
                        MapPoint.mapPointWithGeoCoord(
                                Double.parseDouble(destinationInfo.getY()),
                                Double.parseDouble(destinationInfo.getX())
                        ),
                        new MapReverseGeoCoder.ReverseGeoCodingResultListener() {
                            @Override
                            public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
                                select_member_address.setText(s);   // 상단에 목적지 주소 표시
                            }

                            @Override
                            public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
                                Log.d("Test", "목적지 주소 변환 실패");
                            }
                        }, MapViewActivity.this);

                geoCoder.startFindingAddress();         // 현재 목적지의 좌표를 주소로 변환
            }
        });

        btn_map_view_my_location.setOnClickListener(new View.OnClickListener() {        // 지도의 중심점을 현재 유저의 위치로 이동
            @Override
            public void onClick(View view) {
                locationReference
                        .document(currentUser.getId())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {     // 현재 유저의 위치정보를 DB에서 가져오기
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot != null) {
                                    GeoPoint geoPoint = documentSnapshot.getGeoPoint("l");
                                    if (geoPoint != null) {
                                        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(    // 유저의 현재 위치로 이동
                                                geoPoint.getLatitude(),
                                                geoPoint.getLongitude()),
                                                true);

                                        MapReverseGeoCoder geoCoder = new MapReverseGeoCoder("bd97169bbcb001a23368a7ed262a107c",    // 좌표 -> 주소 변환 설정
                                                MapPoint.mapPointWithGeoCoord(geoPoint.getLatitude(), geoPoint.getLongitude()),
                                                new MapReverseGeoCoder.ReverseGeoCodingResultListener() {
                                                    @Override
                                                    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
                                                        select_member_address.setText(s);   // 상단에 목적지 주소 표시
                                                    }

                                                    @Override
                                                    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
                                                        Log.d("Test", "목적지 주소 변환 실패");
                                                    }
                                                }, MapViewActivity.this);
                                        geoCoder.startFindingAddress();     // 현재 목적지의 좌표를 주소로 변환
                                    }
                                }
                            }
                        });
            }
        });

        memberList = groupInfo.getMemberList();    // 현재 그룹의 멤버 정보
        select_member_address = findViewById(R.id.select_member_address);   // 주소를 표시할 텍스트뷰
        select_member_recycler = findViewById(R.id.select_member_recycler); // 리사이클러뷰
        select_member_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));  // 가로형 리사이클러뷰 설정
        loadMembers(memberList, destination_point);     // 멤버 목록 로드

        for (int i = 0; i < memberList.size(); ++i)
            if (!memberList.get(i).getId().equals(currentUser.getId()))
                setMemberMarker(memberList.get(i).getId());     // 현재 유저를 제외한 각 멤버들의 위치를 지도 위에 마커로 추가
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_top);
    }

    private void setMyLocationMarker() {        // 현재 유저의 위치를 지도 위에 마커로 표시
        locationReference
                .document(currentUser.getId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) { // 현재 유저의 위치정보를 DB에서 가져오고, 위치 변경시 실시간으로 갱신
                        //Log.d("Test", "내 위치 변경됨");
                        if (documentSnapshot != null) {
                            GeoPoint geoPoint = documentSnapshot.getGeoPoint("l");
                            if (geoPoint != null) {

                                if (mapView.findPOIItemByName(currentUser.getName()) != null)   // 이전 위치에 마커가 있으면 삭제
                                    mapView.removePOIItem(myLocationMarker);

                                myLocationMarker = new MapPOIItem();

                                @SuppressLint("InflateParams")
                                View customMarkerBalloon = LayoutInflater.from(MapViewActivity.this).inflate(R.layout.custom_marker_balloon, null); // 커스텀 말풍선 뷰 인플레이션
                                TextView currentUserName = customMarkerBalloon.findViewById(R.id.custom_marker_balloon_userName);
                                currentUserName.setText(currentUser.getName());

                                myLocationMarker.setCustomCalloutBalloon(customMarkerBalloon);      //
                                myLocationMarker.setCustomPressedCalloutBalloon(customMarkerBalloon);   // 마커에 커스텀 말풍선 설정

                                myLocationMarker.setItemName(currentUser.getName());    // 마커의 이름을 현재 유저의 이름으로 설정
                                myLocationMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(geoPoint.getLatitude(), geoPoint.getLongitude()));   // 유저의 현재 위치로 설정
                                myLocationMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);  // 커스텀 이미지 사용
                                myLocationMarker.setShowDisclosureButtonOnCalloutBalloon(false);   // 말풍선 끝부분에 꺽쇠 안보이게

                                Glide.with(getApplicationContext())        // 유저의 이미지를 비트맵으로 변환
                                        .asBitmap()
                                        .load(currentUser.getImageUrl())
                                        .apply(new RequestOptions().circleCrop())
                                        .into(new CustomTarget<Bitmap>(100, 100) {  // 이미지의 크기를 100X100으로 가져오기
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                myLocationMarker.setCustomImageBitmap(resource);    // 변환된 이미지를 마커에 적용
                                                mapView.addPOIItem(myLocationMarker);   // 지도에 마커 추가
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void setMemberMarker(String memberId) {    // 멤버들의 위치를 지도 위에 마커로 표시
        Log.d("Test", "setMemberMarker 호출");
        locationReference
                .document(memberId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {  // 멤버의 위치정보를 DB에서 가져오고, 위치 변경시 실시간으로 갱신
                        Log.d("Test", "멤버 위치 조회");
                        if (documentSnapshot != null) {
                            Log.d("Test", "멤버 위치정보 존재");
                            GeoPoint geoPoint = documentSnapshot.getGeoPoint("l");

                            userOptionReference
                                    .document(memberId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot != null) {
                                                UserOptions userOptions = documentSnapshot.toObject(UserOptions.class);
                                                if (userOptions != null && userOptions.isAllowShareLocation()) {        // 멤버가 위치 공유를 허용했는지 확인
                                                    userReference
                                                            .whereEqualTo("id", documentSnapshot.getId())
                                                            .get()
                                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {  // 매개변수로 받은 id로 DB에서 멤버 정보 가져오기
                                                                @Override
                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                    if (queryDocumentSnapshots != null)
                                                                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                                            User user = snapshot.toObject(User.class);
                                                                            if (user != null && geoPoint != null) {
                                                                                Log.d("Test", "멤버 정보 객체 생성");
                                                                                member = new Member(user.getId(), user.getName(), user.getImageUrl());  // 멤버 정보 객체 생성

                                                                                if (mapView.findPOIItemByName(member.getName()) != null)
                                                                                    mapView.removePOIItem(mapView.findPOIItemByName(member.getName())[0]);  // 이전 위치에 마커가 있으면 삭제

                                                                                MapPOIItem memberMarker = new MapPOIItem();     // 멤버 마커 객체 생성

                                                                                @SuppressLint("InflateParams")
                                                                                View customMarkerBalloon = LayoutInflater.from(MapViewActivity.this).inflate(R.layout.custom_marker_balloon, null); // 커스텀 말풍선 뷰 인플레이션
                                                                                TextView currentUserName = customMarkerBalloon.findViewById(R.id.custom_marker_balloon_userName);
                                                                                currentUserName.setText(member.getName());

                                                                                memberMarker.setCustomCalloutBalloon(customMarkerBalloon);          //
                                                                                memberMarker.setCustomPressedCalloutBalloon(customMarkerBalloon);   // 마커에 커스텀 말풍선 설정

                                                                                memberMarker.setItemName(member.getName());    // 마커의 이름을 현재 멤버의 이름으로 설정
                                                                                memberMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(geoPoint.getLatitude(), geoPoint.getLongitude()));   // 멤버의 현재 위치로 설정
                                                                                memberMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);  // 커스텀 이미지 사용
                                                                                memberMarker.setShowDisclosureButtonOnCalloutBalloon(false);   // 말풍선 끝부분에 꺽쇠 안보이게

                                                                                Glide.with(getApplicationContext())        // 멤버의 이미지를 비트맵으로 변환
                                                                                        .asBitmap()
                                                                                        .load(member.getImageUrl())
                                                                                        .apply(new RequestOptions().circleCrop())
                                                                                        .into(new CustomTarget<Bitmap>(100, 100) {  // 이미지의 크기를 100X100으로 가져오기
                                                                                            @Override
                                                                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                                                                Log.d("Test", "멤버 이미지 비트맵 변환 성공");
                                                                                                memberMarker.setCustomImageBitmap(resource);    // 변환된 이미지를 마커에 적용
                                                                                                mapView.addPOIItem(memberMarker);   // 지도에 마커 추가
                                                                                            }

                                                                                            @Override
                                                                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                }
                                                            });
                                                }

                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void addCircles() {     // 지도상의 목적지 위치에 원 추가
/*        MapCircle circle1000 = new MapCircle(MapPoint.mapPointWithGeoCoord(Double.parseDouble(destinationInfo.getY()), Double.parseDouble(destinationInfo.getX())),
                1000,
                Color.argb(160, 110, 140, 160),
                Color.argb(160, 110, 140, 160));     // 반경 1km의 원*/

        MapCircle circle500 = new MapCircle(MapPoint.mapPointWithGeoCoord(Double.parseDouble(destinationInfo.getY()), Double.parseDouble(destinationInfo.getX())),
                500,
                Color.argb(160, 200, 200, 200),
                Color.argb(160, 200, 200, 200));        // 반경 500m의 원

        MapCircle circle300 = new MapCircle(MapPoint.mapPointWithGeoCoord(Double.parseDouble(destinationInfo.getY()), Double.parseDouble(destinationInfo.getX())),
                300,
                Color.argb(144, 107, 165, 242),
                Color.argb(144, 107, 165, 242));        // 반경 300m의 원

        MapCircle circle100 = new MapCircle(MapPoint.mapPointWithGeoCoord(Double.parseDouble(destinationInfo.getY()), Double.parseDouble(destinationInfo.getX())),
                100,
                Color.argb(80, 255, 215, 0),
                Color.argb(80, 255, 215, 0));           // 반경 100m의 원

        //mapView.addCircle(circle1000);
        mapView.addCircle(circle300);   //
        mapView.addCircle(circle500);   //
        mapView.addCircle(circle100);   // 지도에 원 추가
    }

    private void loadMembers(List<Member> memberList, TMapPoint destination_point) {     // 멤버 목록 로드
        adapter = new RecyclerView.Adapter<SelectMemberViewHolder>() {  // 어댑터 생성
            @NonNull
            @Override
            public SelectMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(MapViewActivity.this).inflate(R.layout.select_member_item, parent, false);
                return new SelectMemberViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull SelectMemberViewHolder holder, int position) {
                if (memberList.size() != 0) {
                    Member member = memberList.get(position);
                    holder.select_member_name.setText(member.getName());

                    if (!member.getImageUrl().equals("default")) {      // 멤버 프로필 이미지 로드
                        holder.select_member_image.setPadding(0, 0, 0, 0);
                        Glide.with(getApplicationContext())
                                .load(member.getImageUrl())
                                .into(holder.select_member_image);
                    }

                    userOptionReference
                            .document(member.getId())
                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    if (documentSnapshot != null) {
                                        UserOptions options = documentSnapshot.toObject(UserOptions.class);
                                        if (options != null && !options.isAllowShareLocation()) {
                                            holder.select_member_time.setVisibility(View.INVISIBLE);
                                            holder.select_member_distance.setVisibility(View.INVISIBLE);
                                            holder.setRecyclerItemClickListener(new RecyclerItemClickListener() {
                                                @Override
                                                public void onItemClickListener(View view, int position) {
                                                }
                                            });

                                        } else {

                                            locationReference.document(member.getId())      // 현재 유저의 위치 정보 가져오기
                                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                            if (documentSnapshot != null) {
                                                                GeoPoint geoPoint = documentSnapshot.getGeoPoint("l");
                                                                if (geoPoint != null) {
                                                                    TMapPoint tMapPoint = new TMapPoint(geoPoint.getLatitude(), geoPoint.getLongitude());   // 해당 멤버의 현재 위치 정보 객체
                                                                    tMapData.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPoint, destination_point, new TMapData.FindPathDataAllListenerCallback() {  // 거리 계산
                                                                        @Override
                                                                        public void onFindPathDataAll(Document document) {
                                                                            Element root = document.getDocumentElement();
                                                                            NodeList Dis = root.getElementsByTagName("tmap:totalDistance");

                                                                            if (Integer.parseInt(Dis.item(0).getChildNodes().item(0).getNodeValue()) > 2000) {  // 총 거리가 2km가 넘으면 대중교통으로 거리/시간 계산
                                                                                tMapData.findPathDataAllType(TMapData.TMapPathType.CAR_PATH, tMapPoint, destination_point, new TMapData.FindPathDataAllListenerCallback() {
                                                                                    @Override
                                                                                    public void onFindPathDataAll(Document document) {
                                                                                        Log.d("Test", "CAR_PATH in MapViewActivity");
                                                                                        Element root = document.getDocumentElement();

                                                                                        NodeList Dis = root.getElementsByTagName("tmap:totalDistance");
                                                                                        int iTotalDistance = Integer.parseInt(Dis.item(0).getChildNodes().item(0).getNodeValue());  // 총 거리 정보 (m)
                                                                                        String totalDistance;
                                                                                        if (iTotalDistance > 1000) {
                                                                                            int km;
                                                                                            km = iTotalDistance / 1000;
                                                                                            totalDistance = km + "km";
                                                                                        } else {
                                                                                            totalDistance = iTotalDistance + "m";
                                                                                        }

                                                                                        NodeList time = root.getElementsByTagName("tmap:totalTime");
                                                                                        int iTotalTime = Integer.parseInt(time.item(0).getChildNodes().item(0).getNodeValue());  // 총 소요시간 정보 (second)
                                                                                        int h = iTotalTime / 3600;
                                                                                        int m = iTotalTime % 3600 / 60;

                                                                                        if (h <= 0)
                                                                                            totalTime = m + "분";
                                                                                        else
                                                                                            totalTime = h + "시간 " + m + "분";

                                                                                        runOnUiThread(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                Log.d("Test", "MapViewActivity runOnUiThread in CAR_PATH");
                                                                                                holder.select_member_distance.setText(totalDistance);
                                                                                                holder.select_member_time.setText(totalTime);
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });
                                                                            } else {        // 총 거리가 2km 이하면 도보로 거리/시간 계산
                                                                                tMapData.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPoint, destination_point, new TMapData.FindPathDataAllListenerCallback() {
                                                                                    @Override
                                                                                    public void onFindPathDataAll(Document document) {
                                                                                        Log.d("Test", "PEDESTRIAN_PATH in MapViewActivity");
                                                                                        Element root = document.getDocumentElement();

                                                                                        NodeList Dis = root.getElementsByTagName("tmap:totalDistance");
                                                                                        int iTotalDistance = Integer.parseInt(Dis.item(0).getChildNodes().item(0).getNodeValue());  // 총 거리 정보 (m)
                                                                                        String totalDistance;
                                                                                        if (iTotalDistance > 1000) {
                                                                                            int km;
                                                                                            km = iTotalDistance / 1000;
                                                                                            totalDistance = km + "km";
                                                                                        } else {
                                                                                            totalDistance = iTotalDistance + "m";
                                                                                        }

                                                                                        NodeList time = root.getElementsByTagName("tmap:totalTime");
                                                                                        int iTotalTime = Integer.parseInt(time.item(0).getChildNodes().item(0).getNodeValue());  // 총 소요시간 정보 (second)
                                                                                        int h = iTotalTime / 3600;
                                                                                        int m = iTotalTime % 3600 / 60;

                                                                                        if (h <= 0)
                                                                                            totalTime = m + "분";
                                                                                        else
                                                                                            totalTime = h + "시간 " + m + "분";

                                                                                        runOnUiThread(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                Log.d("Test", "MapViewActivity runOnUiThread in PEDESTRIAN_PATH");
                                                                                                holder.select_member_distance.setText(totalDistance);
                                                                                                holder.select_member_time.setText(totalTime);
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });

                                                                }
                                                            }
                                                        }
                                                    });

                                            holder.setRecyclerItemClickListener(new RecyclerItemClickListener() {       // 클릭리스너 설정
                                                @Override
                                                public void onItemClickListener(View view, int position) {      // 멤버 클릭 시 지도의 중심점을 해당 멤버 위치로 이동
                                                    Log.d("Test", "멤버 클릭됨");
                                                    locationReference
                                                            .document(member.getId())
                                                            .get()
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                    if (documentSnapshot != null) {
                                                                        GeoPoint geoPoint = documentSnapshot.getGeoPoint("l");

                                                                        if (geoPoint != null) {
                                                                            mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(    // 지도의 중심점을 멤버의 현재 위치로 이동
                                                                                    geoPoint.getLatitude(),
                                                                                    geoPoint.getLongitude()),
                                                                                    true);

                                                                            MapReverseGeoCoder geoCoder = new MapReverseGeoCoder("bd97169bbcb001a23368a7ed262a107c",    // 좌표 -> 주소 변환 설정
                                                                                    MapPoint.mapPointWithGeoCoord(geoPoint.getLatitude(), geoPoint.getLongitude()),
                                                                                    new MapReverseGeoCoder.ReverseGeoCodingResultListener() {
                                                                                        @Override
                                                                                        public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
                                                                                            select_member_address.setText(s);   // 상단에 목적지 주소 표시
                                                                                        }

                                                                                        @Override
                                                                                        public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
                                                                                            Log.d("Test", "목적지 주소 변환 실패");
                                                                                        }
                                                                                    }, MapViewActivity.this);
                                                                            geoCoder.startFindingAddress();     // 선택된 멤버의 위치 좌표를 주소로 변환
                                                                        }

                                                                    }
                                                                }
                                                            });
                                                }
                                            });

                                        }
                                    }
                                }
                            });

/*                    holder.setRecyclerItemClickListener(new RecyclerItemClickListener() {       // 클릭리스너 설정
                        @Override
                        public void onItemClickListener(View view, int position) {      // 멤버 클릭 시 지도의 중심점을 해당 멤버 위치로 이동
                            Log.d("Test", "멤버 클릭됨");
                            locationReference
                                    .document(member.getId())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot != null) {
                                                GeoPoint geoPoint = documentSnapshot.getGeoPoint("l");

                                                if (geoPoint != null) {
                                                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(    // 지도의 중심점을 멤버의 현재 위치로 이동
                                                            geoPoint.getLatitude(),
                                                            geoPoint.getLongitude()),
                                                            true);

                                                    MapReverseGeoCoder geoCoder = new MapReverseGeoCoder("bd97169bbcb001a23368a7ed262a107c",    // 좌표 -> 주소 변환 설정
                                                            MapPoint.mapPointWithGeoCoord(geoPoint.getLatitude(), geoPoint.getLongitude()),
                                                            new MapReverseGeoCoder.ReverseGeoCodingResultListener() {
                                                                @Override
                                                                public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
                                                                    select_member_address.setText(s);   // 상단에 목적지 주소 표시
                                                                }

                                                                @Override
                                                                public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
                                                                    Log.d("Test", "목적지 주소 변환 실패");
                                                                }
                                                            }, MapViewActivity.this);
                                                    geoCoder.startFindingAddress();     // 선택된 멤버의 위치 좌표를 주소로 변환
                                                }

                                            }
                                        }
                                    });
                        }
                    });*/
                }
            }

            @Override
            public int getItemCount() {
                return memberList.size();
            }
        };

        select_member_recycler.setAdapter(adapter);     // 리사이클러뷰와 어댑터 연결
    }

    private void setDestinationMarker(Place destinationInfo) {      // 목적지를 지도에 표시하기
        Log.d("Test", "setDestinationMarker in MapViewActivity");
        MapPOIItem destinationMarker = new MapPOIItem();
        destinationMarker.setItemName(destinationInfo.getPlaceName());  // 목적지 이름
        destinationMarker.setTag(Integer.parseInt(destinationInfo.getPlaceId()));   // 목적지의 태그 설정
        destinationMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(destinationInfo.getY()), Double.parseDouble(destinationInfo.getX())));   // 목적지의 위치(위도, 경도)
        destinationMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);     // 마커 타입 설정
        destinationMarker.setCustomImageResourceId(R.drawable.marker_destination_azure_radience);   // 커스텀 마커 이미지
        destinationMarker.setCustomImageAnchor(0.5f, 1.0f);     // 마커의 기준 위치 지정
        destinationMarker.setCustomImageAutoscale(false);   // 지도 라이브러리의 스케일 기능 Off
        destinationMarker.setShowDisclosureButtonOnCalloutBalloon(false);   // 말풍선 끝부분에 꺽쇠 안보이게
        destinationMarker.setUserObject(destinationInfo);   // 마커에 목적지 정보 객체 저장
        mapView.addPOIItem(destinationMarker);      // 지도에 마커 추가
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(destinationInfo.getY()), Double.parseDouble(destinationInfo.getX())), true);  // 지도의 중심점을 목적지 위치로 이동
        mapView.setZoomLevel(2, true);     // 지도 줌레벨 변경

        MapReverseGeoCoder geoCoder = new MapReverseGeoCoder("bd97169bbcb001a23368a7ed262a107c",
                destinationMarker.getMapPoint(),
                new MapReverseGeoCoder.ReverseGeoCodingResultListener() {
                    @Override
                    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
                        select_member_address.setText(s);   // 상단에 목적지 주소 표시
                    }

                    @Override
                    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
                        Log.d("Test", "목적지 주소 변환 실패");
                    }
                }, MapViewActivity.this);
        geoCoder.startFindingAddress();     // 현재 목적지의 좌표를 주소로 변환
    }
}
