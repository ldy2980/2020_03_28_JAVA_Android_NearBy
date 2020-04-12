package com.skhu.capstone2020.REST_API;

import com.skhu.capstone2020.Model.Response;
import com.skhu.capstone2020.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FCMApiService {
    @Headers(
            {
                    "Content_Type:application/json",
                    "Authorization:key=AAAAUHJLUjI:APA91bEIJDGjnqhPGi3vP5e6DWM7uXul5YZn205mkVcUzSr9__7mfHKR1_CyD9Q_jOGQ89-vDCcTfyFSh-1VM4qsfa8ZVRRwFPoVzaELih4HEd-E0f3M5N8ZNm4Z58v1_VTDt-TfXNQy"
            }
    )

    @POST("fcm/send")
    Call<Response> sendRequestNotification(@Body Sender body);
}
