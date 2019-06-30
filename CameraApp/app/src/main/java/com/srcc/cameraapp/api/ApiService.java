package com.srcc.cameraapp.api;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @POST("/api/Home")
    Single<Home> getHome();

    @Multipart
    @POST("/api/SendImage")
    Single<ResponseBody> sendImage(@Part("description")RequestBody description, @Part MultipartBody.Part file);
}
