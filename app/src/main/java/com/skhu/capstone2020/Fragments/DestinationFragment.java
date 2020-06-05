package com.skhu.capstone2020.Fragments;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.skhu.capstone2020.Adapter.MemberTrackingAdapter;
import com.skhu.capstone2020.Custom.CustomCancelDestinationDialog;
import com.skhu.capstone2020.MapViewActivity;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.PlaceResponse.Place;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.PlaceDetailActivity;
import com.skhu.capstone2020.R;
import com.skhu.capstone2020.SearchPlaceActivity;
import com.skt.Tmap.TMapTapi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DestinationFragment extends Fragment {
    private User currentUser;
    private GroupInfo groupInfo;

    private RelativeLayout layout_no_destination, layout_set_destination, layout_already_meeting;
    private SpinKitView destination_spinKitView;
    private Button btn_search_destination;
    private ImageView destination_image;
    private ImageButton btn_cancel_destination, btn_gps_tracking;
    private TextView destination_place_name, destination_place_address, destination_group_category, destination_category;
    private CardView destination_cardView;
    private WebView webView;
    private WebSettings mWebSettings;
    private String imageSource;
    private String parsedUrl;
    private String destinationId;

    private List<String> groupIdList = new ArrayList<>();
    private CollectionReference reference = FirebaseFirestore.getInstance()
            .collection("Users");

    private CustomCancelDestinationDialog dialog;

    private RecyclerView destination_member_recycler;
    private MemberTrackingAdapter adapter;

    public static boolean isJustSetDestination = false;

    public DestinationFragment(User currentUser, GroupInfo groupInfo) {
        this.currentUser = currentUser;
        this.groupInfo = groupInfo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("Test", "onCreateView in DestinationFragment");
        View view = inflater.inflate(R.layout.fragment_destination, container, false);

        TMapTapi tMapTapi = new TMapTapi(getContext());                           // TMap api key 인증
        tMapTapi.setSKTMapAuthentication("l7xxb40611a0458e46c9b3d18b565b1e701c");

        layout_set_destination = view.findViewById(R.id.set_destination);
        layout_no_destination = view.findViewById(R.id.no_destination);

        destination_cardView = view.findViewById(R.id.destination_cardView);
        destination_spinKitView = view.findViewById(R.id.destination_spinKitView);
        destination_place_name = view.findViewById(R.id.destination_place_name);
        destination_place_address = view.findViewById(R.id.destination_place_address);
        destination_group_category = view.findViewById(R.id.destination_group_category);
        destination_category = view.findViewById(R.id.destination_category);
        destination_image = view.findViewById(R.id.destination_image);
        destination_image.setClipToOutline(true);

        dialog = new CustomCancelDestinationDialog(Objects.requireNonNull(getContext()), okListener, cancelListener);   // 목적지 취소 다이얼로그 설정
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btn_cancel_destination = view.findViewById(R.id.btn_cancel_destination);        // 설정된 목적지 취소 버튼

        if (currentUser.getId().equals(groupInfo.getMasterId())) {
            btn_cancel_destination.setVisibility(View.VISIBLE);     // 현재 유저가 그룹의 마스터유저일 경우 삭제 버튼 보이기
        } else {
            btn_cancel_destination.setVisibility(View.INVISIBLE);       // 마스터유저가 아닐 경우 삭제 버튼 숨기기
        }

        btn_cancel_destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {            // 목적지 취소 버튼 클릭 시
                Log.d("Test", "onClick in btn_cancel_destination");
                dialog.show();
            }
        });

        btn_gps_tracking = view.findViewById(R.id.btn_gps_tracking);    // 멤버 위치 확인 버튼
        btn_gps_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MapViewActivity.class);
                intent.putExtra("groupInfo", groupInfo);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);                                      // 멤버 위치 확인 액티비티로 이동
                Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_scale_out);
            }
        });

        webView = view.findViewById(R.id.destination_web_view);
        mWebSettings = webView.getSettings(); //세부 세팅 등록
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스크립트 허용 여부
        mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부
        webView.addJavascriptInterface(new MyJavascriptInterface(), "Android");
        WebView.setWebContentsDebuggingEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("Test", "onPageFinished");
                // 자바스크립트 인터페이스로 연결되어 있는 getHTML를 실행
                // 자바스크립트 기본 메소드로 html 소스를 통째로 지정해서 인자로 넘김
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('body')[0].innerHTML);");
                    }
                }, 1000);
            }
        });

        btn_search_destination = view.findViewById(R.id.btn_search_destination);
        btn_search_destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SearchPlaceActivity.class);                // 목적지 설정 화면으로 이동
                intent.putExtra("groupInfo", groupInfo);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_scale_out);
            }
        });

        destination_member_recycler = view.findViewById(R.id.destination_member_recycler);  // 멤버 리사이클러뷰
        destination_member_recycler.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL));
        destination_member_recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MemberTrackingAdapter(getContext(), groupInfo);   //
        destination_member_recycler.setAdapter(adapter);                // 어댑터 생성 후 리사이클러뷰에 연결

        FirebaseFirestore.getInstance()
                .collection("Groups")
                .document(groupInfo.getGroupId())
                .collection("Destination")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {                                     // 목적지가 설정되어있지 않을 경우
                            destination_spinKitView.setVisibility(View.INVISIBLE);
                            layout_no_destination.setVisibility(View.VISIBLE);

                            if (currentUser.getId().equals(groupInfo.getMasterId()))
                                btn_search_destination.setVisibility(View.VISIBLE);         // 마스터일 경우 장소 검색 버튼 보이기
                            else
                                btn_search_destination.setVisibility(View.GONE);            // 마스터가 아닐 경우 버튼 숨기기

                        } else {
/*                            destination_image.setPadding(0, 0, 0, 0);
                            destination_image.setScaleType(ImageView.ScaleType.CENTER_CROP);*/

                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {          // 목적지가 설정되어있는 경우
                                Log.d("Test", "Destination is available");
                                Place place = snapshot.toObject(Place.class);
                                if (place != null && place.getUrl() != null) {
                                    destination_place_name.setText(place.getPlaceName());
                                    destination_place_address.setText(place.getAddress());
                                    destinationId = place.getPlaceId();

                                    if (!place.getGroupCategory().equals(""))
                                        destination_group_category.setText(place.getGroupCategory());
                                    else
                                        destination_group_category.setVisibility(View.GONE);

                                    destination_category.setText(place.getCategory());
                                    destination_cardView.setOnClickListener(new View.OnClickListener() {  // 카드뷰 터치 시 상세정보 화면으로 이동
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(getContext(), PlaceDetailActivity.class);
                                            intent.putExtra("placeName", place.getPlaceName());
                                            intent.putExtra("url", place.getUrl());
                                            startActivity(intent);
                                        }
                                    });
                                    webView.loadUrl(place.getUrl());
                                }
                            }
                        }
                    }
                });

        return view;
    }

    @Override
    public void onResume() {
        Log.d("Test", "onResume in DestinationFragment");
        super.onResume();
        if (isJustSetDestination) {
            Log.d("Test", "static value is true");
            assert getFragmentManager() != null;
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(DestinationFragment.this).attach(DestinationFragment.this).commit();
            isJustSetDestination = false;
        }
    }

    private View.OnClickListener okListener = new View.OnClickListener() {          // 설정된 목적지 삭제시 동작
        @Override
        public void onClick(View view) {
            FirebaseFirestore.getInstance()
                    .collection("Groups")
                    .document(groupInfo.getGroupId())
                    .collection("Destination")
                    .document(destinationId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            isJustSetDestination = false;
                            assert getFragmentManager() != null;
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.detach(DestinationFragment.this).attach(DestinationFragment.this).commit();
                            dialog.dismiss();
                        }
                    });

        }
    };

    private View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.dismiss();
        }
    };

    public class MyJavascriptInterface {
        @JavascriptInterface
        public void getHtml(String html) {
            //위 자바스크립트가 호출되면 여기로 html이 반환됨
            imageSource = html;
            Document document = Jsoup.parse(html);
            Elements elements = document.select(".details_present").select("a");            // 여기에서 이미지 URL 파싱
            Log.d("Test", "getHtml");
            Log.d("Test", "Count: " + elements.size() + elements.toString());

            if (elements.size() != 0) {
                for (Element element : elements) {
                    Log.d("Test", "Url is parsed");
                    String sourceUrl = element.attr("style");
                    parsedUrl = "https:" + sourceUrl.substring(23, sourceUrl.length() - 19);
                }
            }
            Log.d("Test", "parsedUrl: " + parsedUrl);

            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Test", "Run");
                    while (true) {
                        Log.d("Test", "In while");
                        if (parsedUrl != null) {
                            destination_image.setPadding(0, 0, 0, 0);
                            destination_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            Glide.with(Objects.requireNonNull(getActivity()))
                                    .load(parsedUrl)
                                    .into(destination_image);
                            destination_spinKitView.setVisibility(View.INVISIBLE);
                            layout_no_destination.setVisibility(View.INVISIBLE);
                            layout_set_destination.setVisibility(View.VISIBLE);
                            break;
                        } else {
                            destination_spinKitView.setVisibility(View.INVISIBLE);
                            layout_no_destination.setVisibility(View.INVISIBLE);
                            layout_set_destination.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            });
        }
    }
}
