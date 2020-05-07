package com.skhu.capstone2020;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class PlaceDetailActivity extends AppCompatActivity {
    ImageView placeDetail_back;
    TextView placeDetail_name;
    WebView placeDetail_view;
    WebSettings mWebSettings;

    String url, placeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        url = getIntent().getStringExtra("url");
        placeName = getIntent().getStringExtra("placeName");

        Toolbar toolbar = findViewById(R.id.placeDetail_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);

        placeDetail_back = findViewById(R.id.placeDetail_back);
        placeDetail_back.setOnClickListener(new View.OnClickListener() {     // 뒤로 가기
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        placeDetail_name = findViewById(R.id.placeDetail_name);
        placeDetail_name.setText(placeName);

        placeDetail_view = findViewById(R.id.placeDetail_view);
        placeDetail_view.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        mWebSettings = placeDetail_view.getSettings(); //세부 세팅
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

        placeDetail_view.loadUrl(url);  // 웹뷰 시작
    }

    @Override
    public void onBackPressed() {
        if (placeDetail_view.getOriginalUrl().equalsIgnoreCase(url)) {
            super.onBackPressed();
        } else if (placeDetail_view.canGoBack()) {
            placeDetail_view.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
