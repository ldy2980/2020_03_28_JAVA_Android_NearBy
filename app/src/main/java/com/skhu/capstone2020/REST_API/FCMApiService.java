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
                    "Authorization: key=AAAAUHJLUjI:APA91bGmdhNz-JMZWbwB9-iSFBrUujz8i61w9l2YGxQUyURUsLer6DweAVMiYrtA2G3PhsND-Hh9bzyO6Hzr6a7ve9b1ftGq9x7U3cUv7cBDW72mEComIkh3IxSbS3A2R7mEi1y2TSd-"
            }
    )

    @POST("/fcm/send")
    Call<Response> sendRequestNotification(@Body Sender body);
}
