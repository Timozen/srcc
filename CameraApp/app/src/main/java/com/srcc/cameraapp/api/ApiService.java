package com.srcc.cameraapp.api;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * This api will handle the returned api requests.
 * We just have to use the functions.
 */
public interface ApiService {

    @POST("/api/Home")
    Single<Home> getHome();

    @Multipart
    @POST("/api/SendImage")
    Single<ResponseBody> sendImage(
            @Part("backend") int backend,
            @Part("tiling") int tiling,
            @Part("tile_size") int tiling_size,
            @Part("stitch_type") int stitching_size,
            @Part("initialization") int initialization,
            @Part MultipartBody.Part file);



    @Multipart
    @POST("/api/SendImage")
    Single<ResponseBody> sendImage(
            @Part("debug") int debug,
            @Part("backend") int backend,
            @Part("tiling") int tiling,
            @Part("tile_size") int tiling_size,
            @Part("stitch_type") int stitching_size,
            @Part("initialization") int initialization,
            @Part MultipartBody.Part file);
}
