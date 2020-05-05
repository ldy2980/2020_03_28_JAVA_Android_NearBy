package com.skhu.capstone2020.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

    private WebView webView;
    private WebSettings mWebSettings;
    private String source;

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

        webView = view.findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
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

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);
                // 자바스크립트 인터페이스로 연결되어 있는 getHTML를 실행
                // 자바스크립트 기본 메소드로 html 소스를 통째로 지정해서 인자로 넘김
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('body')[0].innerHTML);");
                    }
                }, 100);
            }
        });

        webView.loadUrl("https://place.map.kakao.com/14931853"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작

        setList();

        return view;
    }

    private void setList() {
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
                getPlaces(location);
            }
        }
    }

    private void getPlaces(Location location) {
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

    private void getImageUrls(String sourceUrl) {                                                                   // 이미지 URL 크롤링

    }

    public class MyJavascriptInterface {
        @JavascriptInterface
        public void getHtml(String html) {
            //위 자바스크립트가 호출되면 여기로 html이 반환됨
            source = html;
            Document document = Jsoup.parse(html);
            Elements elements = document.select(".details_present").select("a");            // 이미지 URL이 있는 태그 선택
/*            Log.d("WebView Test", "getHtml: " + source);
            Log.d("WebView Test", "Count: " + elements.size() + elements.toString());*/

            if (elements.size() != 0)
                for (Element element : elements) {
                    String url = element.attr("style");
                    String parsedUrl = "https:" + url.substring(23, url.length() - 19);
                    Log.d("WebView Test", "URL: " + parsedUrl);
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
