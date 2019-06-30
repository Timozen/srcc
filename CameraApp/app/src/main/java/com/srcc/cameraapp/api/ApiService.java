package com.srcc.cameraapp.api;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("/api/Home")
    Single<Home> getHome();
}
