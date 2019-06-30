package com.srcc.cameraapp.api;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("todos/{todo_id}")
    Single<Todo> getTodo(@Path("todo_id") int todoId);
}
