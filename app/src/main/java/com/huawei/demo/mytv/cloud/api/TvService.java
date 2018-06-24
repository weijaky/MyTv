package com.huawei.demo.mytv.cloud.api;

import com.huawei.demo.mytv.data.Movie;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * Created by Jack on 2018/6/22.
 */

public interface TvService {


    @GET("/api/movies/{category}")
    Call<List<Movie>> getMovies(@Header(ServiceContext.SERVER_TOKEN) String token,
                                @Path(ServiceContext.CATEGORY) String category);

}
