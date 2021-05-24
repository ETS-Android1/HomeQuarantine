package com.unitbv.quarantineapp;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Api {

    @Headers({
        "Authorization: key=AAAAFcWuNBA:APA91bFsszVft6ekKMNn5N12kpmq3BrtPSw9QXPOy3W-pJ9zeqoJA-mxCYxISW9m88diGukIzAB4Wn7tITNvZWF0VR9xS7UA6Wm_7HcqPzVNjVViBb4y0Q8MwKdnv2-r5cWN8OXi9oT2",
            "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendNotification(@Body RequestNotification requestNotification);
}
