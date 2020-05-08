package com.skhu.capstone2020.REST_API;

import com.skhu.capstone2020.Model.Sender;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FCMApiService {
    @Headers(
            {
                    "Authorization: key=AAAAUHJLUjI:APA91bGlabfHxfrb7WpN4P8a_cy8275s4f6zzzovRJHmkY0dvvRADxSkZKL_wIvwyIzOz4U4eWikxSpHK_PgvEg7Y_jWz-bpZNDXi3cVu-zJ_Vs6TrY5TZAZxYJFch6Z7ZpqJKkBGQLy",
                    "Content_Type:application/json"
            }
    )

    @POST("/fcm/send")
    Call<ResponseBody> sendRequestNotification(@Body Sender body);
}
