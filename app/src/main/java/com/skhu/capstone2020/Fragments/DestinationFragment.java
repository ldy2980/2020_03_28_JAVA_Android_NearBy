package com.skhu.capstone2020.Fragments;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.PlaceResponse.Place;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.PlaceDetailActivity;
import com.skhu.capstone2020.R;
import com.skhu.capstone2020.SearchPlaceActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Objects;

public class DestinationFragment extends Fragment {
    private User currentUser;
    private GroupInfo groupInfo;

    private RelativeLayout layout_no_destination, layout_set_destination;

    private SpinKitView destination_spinKitView;
    private Button btn_search_destination;
    private ImageView destination_image, destination_category;
    private TextView destination_place_name;
    private TextView destination_place_address;
    private Button destination_detail_btn;
    private WebView webView;
    private WebSettings mWebSettings;
    private String imageSource;
    private String parsedUrl;

    public DestinationFragment(User currentUser, GroupInfo groupInfo) {
        this.currentUser = currentUser;
        this.groupInfo = groupInfo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("Test", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_destination, container, false);

        destination_spinKitView = view.findViewById(R.id.destination_spinKitView);
        destination_place_name = view.findViewById(R.id.destination_place_name);
        destination_place_address = view.findViewById(R.id.destination_place_address);
        destination_category = view.findViewById(R.id.destination_category);
        destination_image = view.findViewById(R.id.destination_image);
        destination_image.setClipToOutline(true);

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

        destination_detail_btn = view.findViewById(R.id.destination_detail_btn);

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

        layout_set_destination = view.findViewById(R.id.set_destination);
        layout_no_destination = view.findViewById(R.id.no_destination);
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
                            destination_image.setPadding(0, 0, 0, 0);
                            destination_image.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {          // 목적지가 설정되어있는 경우
                                Log.d("Test", "Destination is available");
                                Place place = snapshot.toObject(Place.class);
                                if (place != null && place.getUrl() != null) {
                                    destination_place_name.setText(place.getPlaceName());
                                    destination_place_address.setText(place.getAddress());
                                    setCategoryIcon(place);
                                    destination_detail_btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(getActivity(), PlaceDetailActivity.class);
                                            intent.putExtra("placeName", place.getPlaceName());
                                            intent.putExtra("url", place.getUrl());
                                            Objects.requireNonNull(getActivity()).startActivity(intent);
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

/*    @Override
    public void onResume() {
        super.onResume();
        Log.d("Test", "onResume");
        //layout_no_destination.setVisibility(View.INVISIBLE);
        //destination_spinKitView.setVisibility(View.VISIBLE);
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
                            destination_image.setPadding(0, 0, 0, 0);
                            destination_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {          // 목적지가 설정되어있는 경우
                                Log.d("Test", "Destination is available");
                                Place place = snapshot.toObject(Place.class);
                                if (place != null && place.getUrl() != null)
                                    webView.loadUrl(place.getUrl());
                            }
                        }
                    }
                });
    }*/

    private void setCategoryIcon(Place place) {
        switch (place.getCategoryCode()) {
            case "FD6":
                destination_category.setImageResource(R.drawable.ic_category_restaurant);
                destination_category.setBackground(ActivityCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.category_restaurant_background));
                break;
            case "CS2":
            case "MT1":
                destination_category.setImageResource(R.drawable.ic_category_market);
                destination_category.setBackground(ActivityCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.category_market_background));
                break;
            case "CE7":
                destination_category.setImageResource(R.drawable.ic_category_cafe);
                destination_category.setBackground(ActivityCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.category_cafe_background));
                break;
            case "PM9":
                destination_category.setImageResource(R.drawable.ic_category_pharmacy);
                destination_category.setBackground(ActivityCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.category_pharmacy_background));
                break;
            case "BK9":
                destination_category.setImageResource(R.drawable.ic_category_bank);
                destination_category.setBackground(ActivityCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.category_bank_background));
                break;
            case "SW8":
                destination_category.setImageResource(R.drawable.ic_category_subway_station);
                destination_category.setBackground(ActivityCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.category_subway_station));
                break;
            case "HP8":
                destination_category.setImageResource(R.drawable.ic_category_hospital);
                destination_category.setBackground(ActivityCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.category_hospital_background));
                break;
            case "CT1":
                destination_category.setImageResource(R.drawable.ic_category_culture);
                destination_category.setBackground(ActivityCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.category_culture_background));
                break;
            case "AD5":
                destination_category.setImageResource(R.drawable.ic_category_accommodation);
                destination_category.setBackground(ActivityCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.category_accommodation_background));
                break;
            default:
                //destination_category.setVisibility(View.INVISIBLE);
                break;
        }
    }

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
                    try {
                        while (true) {
                            Thread.sleep(1000);
                            Log.d("Test", "In while");
                            if (parsedUrl != null) {
                                Glide.with(Objects.requireNonNull(getActivity()))
                                        .load(parsedUrl)
                                        .into(destination_image);
                                destination_spinKitView.setVisibility(View.INVISIBLE);
                                layout_no_destination.setVisibility(View.INVISIBLE);
                                layout_set_destination.setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
