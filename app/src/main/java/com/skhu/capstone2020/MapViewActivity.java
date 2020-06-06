package com.skhu.capstone2020;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.skhu.capstone2020.Adapter.ViewHolder.SelectMemberViewHolder;
import com.skhu.capstone2020.Custom.CustomCallOutBalloonAdapter;
import com.skhu.capstone2020.Interface.RecyclerItemClickListener;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.PlaceResponse.Place;
import com.skhu.capstone2020.Model.User;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;

public class MapViewActivity extends AppCompatActivity {
    private RecyclerView select_member_recycler;
    private TextView select_member_address;

    private GroupInfo groupInfo;
    private User currentUser;
    private Place destinationInfo;

    private RecyclerView.Adapter<SelectMemberViewHolder> adapter;

    private CollectionReference locationReference = FirebaseFirestore.getInstance().collection("Locations");
    private TMapData tMapData = new TMapData();
    private TMapPoint destination_point;

    private String totalTime;

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        groupInfo = (GroupInfo) getIntent().getSerializableExtra("groupInfo");  // 그룹 정보 객체 가져오기
        currentUser = (User) getIntent().getSerializableExtra("currentUser");   // 현재 유저 정보 객체 가져오기
        destinationInfo = (Place) getIntent().getSerializableExtra("destinationInfo");  // 목적지 정보 객체 가져오기

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

        List<Member> memberList = groupInfo.getMemberList();    // 현재 그룹의 멤버 정보
        select_member_address = findViewById(R.id.select_member_address);   // 선택된 멤버의 현재 주소를 표시할 텍스트뷰
        select_member_recycler = findViewById(R.id.select_member_recycler); // 리사이클러뷰
        select_member_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));  // 가로형 리사이클러뷰 설정
        loadMembers(memberList, destination_point);     // 멤버 목록 로드
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_top);
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
                        Glide.with(MapViewActivity.this)
                                .load(member.getImageUrl())
                                .into(holder.select_member_image);
                    }

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
                            Log.d("Test", "selectMemberItem clicked");
                        }
                    });
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
        destinationMarker.setCustomImageResourceId(R.drawable.marker_destination_sky_blue);   // 커스텀 마커 이미지
        destinationMarker.setCustomImageAnchor(0.5f, 1.0f);     // 마커의 기준 위치 지정
        destinationMarker.setCustomImageAutoscale(false);   // 지도 라이브러리의 스케일 기능 Off
        destinationMarker.setShowDisclosureButtonOnCalloutBalloon(false);   // 말풍선 끝부분에 꺽쇠 안보이게
        destinationMarker.setUserObject(destinationInfo);   // 마커에 목적지 정보 객체 저장
        mapView.addPOIItem(destinationMarker);      // 지도에 마커 추가
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(destinationInfo.getY()), Double.parseDouble(destinationInfo.getX())), true);  // 지도의 중심점을 목적지 위치로 이동
        mapView.setZoomLevel(2, true);     // 지도 줌레벨 변경
    }
}
