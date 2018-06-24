package com.huawei.demo.mytv.cloud.task;


import com.huawei.demo.mytv.cloud.api.ServiceContext;
import com.huawei.demo.mytv.cloud.api.TvService;
import com.huawei.demo.mytv.data.Movie;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Jack on 2018/6/23.
 */

public class GetMoviesObservable {

    public Observable getMovies() {
        return Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                List<Movie> movies = null;
                Response<List<Movie>> response = getTvService(ServiceContext.BASEURL)
                        .getMovies("token", "").execute();
                if (response != null) {
                    movies = response.body();
                }
                e.onNext(movies);
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Retrofit getRetrofit(String url) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    public TvService getTvService(String url) {
        return getRetrofit(url).create(TvService.class);
    }
}
