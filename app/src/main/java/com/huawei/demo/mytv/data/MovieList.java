/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.huawei.demo.mytv.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class MovieList {
    private static MovieList movies;
    private final String serverUrl = LocalDataManager.getConfig().getServer();
    private final String movieUrl = serverUrl + LocalDataManager.getConfig().getMovieDir();
    private final String imageUrl = movieUrl + LocalDataManager.getConfig().getImageDir();
    private final String fileName = "test";
    private final String movieExt = ".mp4";
    private final String imageExt = ".png";
    private static List<Movie> list;
    private static long count = 0;

    private MovieList() {
        list = new ArrayList<>();
        List<String> title = LocalDataManager.getConfig().getMovieTitle();
        List<String> studio = LocalDataManager.getConfig().getStudio();
        List<String> description = LocalDataManager.getConfig().getMovieDescription();
        String[] videoUrl = new String[5];
        String[] bgImageUrl = new String[5];
        String[] cardImageUrl = new String[5];


        for (int i = 0; i < videoUrl.length; i++) {
            videoUrl[i] = movieUrl + File.separator + fileName + (i + 1) + movieExt;
            bgImageUrl[i] = imageUrl + File.separator + fileName + (i + 1) + imageExt;
            cardImageUrl[i] = imageUrl + File.separator + fileName + (i + 1) + imageExt;
            list.add(buildMovieInfo("category",
                    title.get(i), description.get(i), studio.get(i), videoUrl[i], cardImageUrl[i], bgImageUrl[i]));
        }
    }

    public static MovieList init() {
        if (movies == null) {
            movies = new MovieList();
        }
        return movies;
    }

    public List<Movie> getList() {
        return list;
    }


    private Movie buildMovieInfo(String category, String title,
                                 String description, String studio, String videoUrl, String cardImageUrl,
                                 String backgroundImageUrl) {
        Movie movie = new Movie();
        movie.setId(count++);
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setStudio(studio);
        movie.setCategory(category);
        movie.setCardImageUrl(cardImageUrl);
        movie.setBackgroundImageUrl(backgroundImageUrl);
        movie.setVideoUrl(videoUrl);
        return movie;
    }
}