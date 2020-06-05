package com.skhu.capstone2020.Adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.PlaceResponse.Place;
import com.skhu.capstone2020.Model.UserOptions;
import com.skhu.capstone2020.R;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapTapi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;

public class MemberTrackingAdapter extends RecyclerView.Adapter<MemberTrackingAdapter.ViewHolder> {
    private Context context;
    private Activity activity;
    private GroupInfo groupInfo;
    private List<Member> memberList;

    private CollectionReference userOptionReference = FirebaseFirestore.getInstance().collection("UserOptions");
    private CollectionReference locationReference = FirebaseFirestore.getInstance().collection("Locations");

    private TMapData tMapData = new TMapData();
    private TMapPoint destination_point;

    public MemberTrackingAdapter(Context context, Activity activity, GroupInfo groupInfo) {     // 목적지 설정이 되어있지 않을 때의 생성자
        this.context = context;
        this.activity = activity;
        this.groupInfo = groupInfo;
        this.memberList = groupInfo.getMemberList();

        TMapTapi tMapTapi = new TMapTapi(context);       // TMap api key 인증
        tMapTapi.setSKTMapAuthentication("l7xxb40611a0458e46c9b3d18b565b1e701c");
    }

    public MemberTrackingAdapter(Context context, Activity activity, GroupInfo groupInfo, Place destination) {  // 목적지 설정이 되어있을 때의 생성자
        this.context = context;
        this.activity = activity;
        this.groupInfo = groupInfo;
        this.memberList = groupInfo.getMemberList();
        this.destination_point = new TMapPoint(Double.parseDouble(destination.getY()), Double.parseDouble(destination.getX()));

        TMapTapi tMapTapi = new TMapTapi(context);      // TMap api key 인증
        tMapTapi.setSKTMapAuthentication("l7xxb40611a0458e46c9b3d18b565b1e701c");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_tracking_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (memberList.size() != 0) {
            Member member = memberList.get(position);
            holder.member_tracking_name.setText(member.getName());

            if (!member.getImageUrl().equals("default")) {
                holder.member_tracking_photo.setPadding(0, 0, 0, 0);
                Glide.with(context)
                        .load(member.getImageUrl())
                        .into(holder.member_tracking_photo);
            }

            userOptionReference.document(member.getId())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                UserOptions options = documentSnapshot.toObject(UserOptions.class);

                                if (options != null)
                                    if (options.isAllowShareLocation()) {   // 위치 공유 설정 확인
                                        holder.member_tracking_no_tracking.setVisibility(View.INVISIBLE);
                                        holder.member_tracking_distance.setVisibility(View.VISIBLE);
                                        holder.member_tracking_time.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.member_tracking_distance.setVisibility(View.INVISIBLE);
                                        holder.member_tracking_no_tracking.setVisibility(View.VISIBLE);
                                        holder.member_tracking_time.setVisibility(View.INVISIBLE);
                                    }
                            }
                        }
                    });

            locationReference.document(member.getId())      // 현재 유저의 위치 정보 가져오기
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                GeoPoint geoPoint = documentSnapshot.getGeoPoint("l");
                                if (geoPoint != null) {
                                    TMapPoint tMapPoint = new TMapPoint(geoPoint.getLatitude(), geoPoint.getLongitude());

                                    tMapData.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPoint, destination_point, new TMapData.FindPathDataAllListenerCallback() {  // 거리 계산
                                        @Override
                                        public void onFindPathDataAll(Document document) {
                                            Element root = document.getDocumentElement();
                                            NodeList Dis = root.getElementsByTagName("tmap:totalDistance");

                                            if (Integer.parseInt(Dis.item(0).getChildNodes().item(0).getNodeValue()) > 2000) {  // 총 거리가 2km가 넘으면 대중교통으로 거리/시간 계산
                                                tMapData.findPathDataAllType(TMapData.TMapPathType.CAR_PATH, tMapPoint, destination_point, new TMapData.FindPathDataAllListenerCallback() {
                                                    @Override
                                                    public void onFindPathDataAll(Document document) {
                                                        Log.d("Test", "CAR_PATH");
                                                        Element root = document.getDocumentElement();

                                                        NodeList Dis = root.getElementsByTagName("tmap:totalDistance");
                                                        String totalDistance = Dis.item(0).getChildNodes().item(0).getNodeValue() + "m";  // 총 거리 정보 (m)

                                                        NodeList time = root.getElementsByTagName("tmap:totalTime");
                                                        String totalTime = time.item(0).getChildNodes().item(0).getNodeValue();    // 총 소요시간 정보 (second)

                                                        activity.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Log.d("Test", "runOnUiThread in CAR_PATH");
                                                                holder.member_tracking_distance.setText(totalDistance);
                                                                holder.member_tracking_time.setText(totalTime);
                                                            }
                                                        });
                                                    }
                                                });
                                            } else {        // 총 거리가 2km 이하면 도보로 거리/시간 계산
                                                tMapData.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPoint, destination_point, new TMapData.FindPathDataAllListenerCallback() {
                                                    @Override
                                                    public void onFindPathDataAll(Document document) {
                                                        Log.d("Test", "PEDESTRIAN_PATH");
                                                        Element root = document.getDocumentElement();

                                                        NodeList Dis = root.getElementsByTagName("tmap:totalDistance");
                                                        String totalDistance = Dis.item(0).getChildNodes().item(0).getNodeValue() + "m";  // 총 거리 정보 (m)

                                                        NodeList time = root.getElementsByTagName("tmap:totalTime");
                                                        String totalTime = time.item(0).getChildNodes().item(0).getNodeValue();     // 총 소요시간 정보 (second)

                                                        activity.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Log.d("Test", "runOnUiThread in PEDESTRIAN_PATH");
                                                                holder.member_tracking_distance.setText(totalDistance);
                                                                holder.member_tracking_time.setText(totalTime);
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
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView member_tracking_photo, member_tracking_no_tracking;
        TextView member_tracking_name, member_tracking_time, member_tracking_distance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            member_tracking_no_tracking = itemView.findViewById(R.id.member_tracking_no_tracking);
            member_tracking_distance = itemView.findViewById(R.id.member_tracking_distance);
            member_tracking_name = itemView.findViewById(R.id.member_tracking_name);
            member_tracking_time = itemView.findViewById(R.id.member_tracking_time);
            member_tracking_photo = itemView.findViewById(R.id.member_tracking_photo);
            member_tracking_photo.setClipToOutline(true);
        }
    }
}
