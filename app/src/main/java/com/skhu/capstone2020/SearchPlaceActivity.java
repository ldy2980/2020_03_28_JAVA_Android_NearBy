package com.skhu.capstone2020;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skhu.capstone2020.Adapter.PlacesListAdapter;
import com.skhu.capstone2020.Model.Address;
import com.skhu.capstone2020.Model.AddressResponse;
import com.skhu.capstone2020.Model.Place;
import com.skhu.capstone2020.Model.PlaceResponse;
import com.skhu.capstone2020.REST_API.KakaoLocalApi;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchPlaceActivity extends AppCompatActivity {
    private GridLayout grid_container;
    private RelativeLayout place_search_my_location;
    private FrameLayout place_search_input_container;
    private ImageView place_search_toggle_btn, btn_place_search, place_search_no_result;
    private TextView place_search_show_my_location, place_search_no_result_text;
    private TextView place_category_all, place_category_cafe, place_category_restaurant_pub, place_category_culture, place_category_market, place_category_accommodation;
    private CheckBox place_search_check_my_location;
    private EditText place_search_input;

    private int[] categoryIds = {R.id.place_category_all, R.id.place_category_cafe, R.id.place_category_restaurant_pub,
            R.id.place_category_culture, R.id.place_category_market, R.id.place_category_accommodation};
    private List<String> categoryCodeList = new ArrayList<>();
    private String keyword;

    private RecyclerView place_search_recycler;
    private PlacesListAdapter adapter;
    private PlaceResponse placeResponse;
    private List<Place> placeList = new ArrayList<>();

    private LocationManager locationManager;
    private Location currentLocation;

    private Retrofit retrofit;
    private KakaoLocalApi api;
    private AddressResponse addressResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_place);

        place_search_no_result = findViewById(R.id.place_search_no_result);
        place_search_no_result_text = findViewById(R.id.place_search_no_result_text);

        place_category_all = findViewById(R.id.place_category_all);
        place_category_cafe = findViewById(R.id.place_category_cafe);
        place_category_restaurant_pub = findViewById(R.id.place_category_restaurant_pub);
        place_category_culture = findViewById(R.id.place_category_culture);
        place_category_market = findViewById(R.id.place_category_market);
        place_category_accommodation = findViewById(R.id.place_category_accommodation);

        place_category_all.setOnClickListener(listener);
        place_category_cafe.setOnClickListener(listener);
        place_category_restaurant_pub.setOnClickListener(listener);
        place_category_culture.setOnClickListener(listener);
        place_category_market.setOnClickListener(listener);
        place_category_accommodation.setOnClickListener(listener);

        place_search_check_my_location = findViewById(R.id.place_search_check_my_location);
        place_search_check_my_location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d("Test", "onCheckedChanged: " + place_search_check_my_location.isChecked());
            }
        });

        btn_place_search = findViewById(R.id.btn_place_search);
        btn_place_search.setOnClickListener(new View.OnClickListener() {                            // 검색 버튼 동작
            @Override
            public void onClick(View view) {
                Log.d("Test", "onClick on Search button");
                if (place_search_input.getText().toString().trim().length() > 0) {
                    InputMethodManager immHide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (immHide != null)
                        immHide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    placeList.clear();
                    keyword = place_search_input.getText().toString();
                    place_search_input.setText("");
                    place_search_input.clearFocus();

                    if (categoryCodeList.size() > 0 && !(place_search_check_my_location.isChecked())) {
                        for (String categoryCode : categoryCodeList) {
                            getPlaceListByKeywordAndCategory(keyword, categoryCode);                // 카테고리로 필터링했을 경우
                        }
                    } else if (categoryCodeList.size() == 0 && place_search_check_my_location.isChecked()) {
                        getPlaceListByKeywordAndLocation(keyword, currentLocation);                 // 현재 위치에서 검색할 경우
                    } else {
                        getPlaceListByKeyword(keyword);                                             // 키워드만 입력했을 경우
                    }
                }
            }
        });

        place_search_input = findViewById(R.id.place_search_input);
        place_search_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {           // 입력값이 있을 때만 검색 버튼 활성화
                if (place_search_input.getText().toString().trim().length() > 0) {
                    btn_place_search.setImageResource(R.drawable.ic_search_eastbay);
                    btn_place_search.setClickable(true);
                } else {
                    btn_place_search.setImageResource(R.drawable.ic_search_ice);
                    btn_place_search.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        place_search_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {        // 키보드 엔터키 재정의
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Log.d("Test", "onEditorAction");
                if (i == EditorInfo.IME_ACTION_DONE && place_search_input.getText().toString().trim().length() > 0) {
                    InputMethodManager immHide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (immHide != null)
                        immHide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    placeList.clear();
                    keyword = place_search_input.getText().toString();
                    place_search_input.setText("");
                    place_search_input.clearFocus();

                    if (categoryCodeList.size() > 0 && !(place_search_check_my_location.isChecked())) {
                        for (String categoryCode : categoryCodeList)
                            getPlaceListByKeywordAndCategory(keyword, categoryCode);                // 카테고리로 필터링했을 경우
                    } else if (categoryCodeList.size() == 0 && place_search_check_my_location.isChecked()) {
                        getPlaceListByKeywordAndLocation(keyword, currentLocation);                 // 현재 위치에서 검색할 경우
                    } else {
                        getPlaceListByKeyword(keyword);                                             // 키워드만 입력했을 경우
                    }
                }
                return true;
            }
        });

        place_search_show_my_location = findViewById(R.id.place_search_show_my_location);
        place_search_input_container = findViewById(R.id.place_search_input_container);
        place_search_my_location = findViewById(R.id.place_search_my_location);
        grid_container = findViewById(R.id.grid_container);
        place_search_toggle_btn = findViewById(R.id.place_search_toggle_btn);
        place_search_toggle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                                        // 카테고리 선택영역 Visible/Invisible
                Log.d("Test", "onClick on category");
                if (grid_container.getVisibility() == View.VISIBLE) {
                    grid_container.setVisibility(View.GONE);
                    place_search_my_location.setVisibility(View.GONE);
                    place_search_toggle_btn.setImageResource(R.drawable.ic_arrow_down);
                } else {
                    grid_container.setVisibility(View.VISIBLE);
                    place_search_my_location.setVisibility(View.VISIBLE);
                    place_search_toggle_btn.setImageResource(R.drawable.ic_arrow_up);
                }
            }
        });

        place_search_recycler = findViewById(R.id.place_search_recycler);
        place_search_recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlacesListAdapter(placeList, SearchPlaceActivity.this);
        place_search_recycler.setAdapter(adapter);                                                  // 리사이클러뷰와 어댑터 연결

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        } else {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);   // 현재 위치 정보 받아오기
            if (currentLocation == null) {
                Log.d("Test", "Network location");
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (currentLocation != null) {
                    Log.d("Test", "location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                    getAddress(currentLocation);
                }
            } else {
                Log.d("Test", "GPS location");
                Log.d("Test", "location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                getAddress(currentLocation);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_top);
    }

    View.OnClickListener listener = new View.OnClickListener() {                                    // 카테고리별 결과 필터링 기능
        @Override
        public void onClick(View view) {
            setButtonBackgroundColor(view.getId());
            Log.d("Test", "Category codes: " + categoryCodeList.toString());
        }
    };

    private void setButtonBackgroundColor(int id) {
        switch (id) {
            case R.id.place_category_all:
                categoryCodeList.clear();
                setDefaultBackgroundColor();
                place_category_all.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.eastBay));
                break;
            case R.id.place_category_cafe:
                categoryCodeList.clear();
                setDefaultBackgroundColor();
                place_category_cafe.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.eastBay));
                categoryCodeList.add("CE7");
                break;
            case R.id.place_category_restaurant_pub:
                categoryCodeList.clear();
                setDefaultBackgroundColor();
                place_category_restaurant_pub.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.eastBay));
                categoryCodeList.add("FD6");
                break;
            case R.id.place_category_culture:
                categoryCodeList.clear();
                setDefaultBackgroundColor();
                place_category_culture.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.eastBay));
                categoryCodeList.add("CT1");
                break;
            case R.id.place_category_market:
                categoryCodeList.clear();
                setDefaultBackgroundColor();
                place_category_market.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.eastBay));
                categoryCodeList.add("MT1");
                categoryCodeList.add("CS2");
                break;
            case R.id.place_category_accommodation:
                categoryCodeList.clear();
                setDefaultBackgroundColor();
                place_category_accommodation.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.eastBay));
                categoryCodeList.add("AD5");
                break;
        }
    }

    private void setDefaultBackgroundColor() {
        place_category_all.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.dodgerBlue));
        place_category_cafe.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.royalBlue));
        place_category_restaurant_pub.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.slateBlue));
        place_category_culture.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.azureRadiance));
        place_category_market.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.vividBlue));
        place_category_accommodation.setBackgroundColor(ContextCompat.getColor(SearchPlaceActivity.this, R.color.lightBlue));
    }

    private void getPlaceListByKeyword(String keyword) {                                            // 장소 목록 가져오기(키워드)
        retrofit = new Retrofit.Builder()
                .baseUrl(KakaoLocalApi.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KakaoLocalApi.class);
        api.getPlacesByKeyword(KakaoLocalApi.key, keyword)
                .enqueue(new Callback<PlaceResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<PlaceResponse> call, @NotNull Response<PlaceResponse> response) {
                        Log.d("Test", "onResponse in getPlaceListByKeyword");
                        if (!(response.isSuccessful())) {
                            Toast.makeText(SearchPlaceActivity.this, response.code() + ", " + response.message(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        placeResponse = response.body();
                        if (placeResponse != null) {
                            Log.d("Test", "placeResponse is not null" + ", " + placeResponse.getPlaceList().size());
                            if (placeResponse.getPlaceList().size() != 0) {                         // 검색 결과가 있을 때만 리사이클러뷰에 표시
                                placeList.addAll(placeResponse.getPlaceList());
                                adapter.notifyDataSetChanged();
                                place_search_no_result.setVisibility(View.INVISIBLE);
                                place_search_no_result_text.setVisibility(View.INVISIBLE);
                                place_search_recycler.setVisibility(View.VISIBLE);
                            } else {
                                place_search_no_result.setVisibility(View.VISIBLE);                 // 검색 결과가 없을 경우 "결과 없음" 표시
                                place_search_no_result_text.setVisibility(View.VISIBLE);
                                place_search_recycler.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<PlaceResponse> call, @NotNull Throwable t) {
                        Toast.makeText(SearchPlaceActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getPlaceListByKeywordAndCategory(String keyword, String categoryCode) {            // 장소 목록 가져오기(키워드, 카테고리)
        Log.d("Test", "getPlaceListKeywordAndCategory");
        retrofit = new Retrofit.Builder()
                .baseUrl(KakaoLocalApi.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KakaoLocalApi.class);
        api.getPlacesByKeywordAndCategory(KakaoLocalApi.key, keyword, categoryCode)
                .enqueue(new Callback<PlaceResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<PlaceResponse> call, @NotNull Response<PlaceResponse> response) {
                        Log.d("Test", "onResponse in getPlaceListKeywordAndCategory");
                        if (!(response.isSuccessful())) {
                            Toast.makeText(SearchPlaceActivity.this, response.code() + ", " + response.message(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        placeResponse = response.body();
                        if (placeResponse != null) {
                            if (placeResponse.getPlaceList().size() != 0) {                         // 검색 결과가 있을 때만 리사이클러뷰에 표시
                                placeList.addAll(placeResponse.getPlaceList());
                                adapter.notifyDataSetChanged();
                                place_search_no_result.setVisibility(View.INVISIBLE);
                                place_search_no_result_text.setVisibility(View.INVISIBLE);
                                place_search_recycler.setVisibility(View.VISIBLE);
                            } else {
                                place_search_no_result.setVisibility(View.VISIBLE);                 // 검색 결과가 없을 경우 "결과 없음" 표시
                                place_search_no_result_text.setVisibility(View.VISIBLE);
                                place_search_recycler.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<PlaceResponse> call, @NotNull Throwable t) {
                        Log.d("Test", "onFailure in getPlaceListByKeywordAndCategory");
                        Toast.makeText(SearchPlaceActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getPlaceListByKeywordAndLocation(String keyword, Location currentLocation) {       // 장소 목록 가져오기(키워드, 현재 위치)
        Log.d("Test", "getPlaceListByKeywordAndLocation");
        retrofit = new Retrofit.Builder()
                .baseUrl(KakaoLocalApi.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KakaoLocalApi.class);
        api.getPlacesByKeywordAndLocation(KakaoLocalApi.key, keyword, currentLocation.getLongitude(), currentLocation.getLatitude(), KakaoLocalApi.radius)
                .enqueue(new Callback<PlaceResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<PlaceResponse> call, @NotNull Response<PlaceResponse> response) {
                        Log.d("Test", "onResponse in getPlaceListByKeywordAndLocation" + ", " + response.message());
                        if (!(response.isSuccessful())) {
                            Toast.makeText(SearchPlaceActivity.this, response.code() + ", " + response.message(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        placeResponse = response.body();
                        if (placeResponse != null) {
                            if (placeResponse.getPlaceList().size() != 0) {                         // 검색 결과가 있을 때만 리사이클러뷰에 표시
                                placeList.addAll(placeResponse.getPlaceList());
                                adapter.notifyDataSetChanged();
                                place_search_no_result.setVisibility(View.INVISIBLE);
                                place_search_no_result_text.setVisibility(View.INVISIBLE);
                                place_search_recycler.setVisibility(View.VISIBLE);
                            } else {
                                place_search_no_result.setVisibility(View.VISIBLE);                 // 검색 결과가 없을 경우 "결과 없음" 표시
                                place_search_no_result_text.setVisibility(View.VISIBLE);
                                place_search_recycler.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<PlaceResponse> call, @NotNull Throwable t) {
                        Log.d("Test", "onFailure in getPlaceByKeywordAndLocation");
                        Toast.makeText(SearchPlaceActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getAddress(Location currentLocation) {                                             // 현재 위치의 주소 정보 받아오기
        Log.d("Test", "getAddress");
        Log.d("Test", "location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
        retrofit = new Retrofit.Builder()
                .baseUrl(KakaoLocalApi.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KakaoLocalApi.class);
        api.getAddress(KakaoLocalApi.key, currentLocation.getLongitude(), currentLocation.getLatitude())
                .enqueue(new Callback<AddressResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<AddressResponse> call, @NotNull Response<AddressResponse> response) {
                        Log.d("Test", "onResponse in getAddress" + ", " + response.message());
                        if (!(response.isSuccessful())) {
                            Toast.makeText(SearchPlaceActivity.this, response.code() + ", " + response.message(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        addressResponse = response.body();
                        if (addressResponse != null) {
                            Log.d("Test", "addressResponse is not null" + ", " + addressResponse.getAddressDocuments().get(0).getAddress().getAddress_name());
                            Log.d("Test", "addressResponse is not null" + ", " + addressResponse.getAddressDocuments().get(0).getRoadAddress());
                            Address address = addressResponse.getAddressDocuments().get(0).getAddress();
                            if (address != null)
                                place_search_show_my_location.setText(address.getAddress_name());
                            else
                                place_search_show_my_location.setText("위치를 찾을 수 없음");
                        }

                    }

                    @Override
                    public void onFailure(@NotNull Call<AddressResponse> call, @NotNull Throwable t) {
                        Log.d("Test", "onFailure in getAddress");
                        Toast.makeText(SearchPlaceActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
