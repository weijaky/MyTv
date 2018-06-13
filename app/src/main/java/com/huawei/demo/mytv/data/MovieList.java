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

import java.util.ArrayList;
import java.util.List;

public final class MovieList {
    public static final String MOVIE_CATEGORY[] = {
            "资讯",
            "财经",
            "军事",
            "娱乐",
            "体育",
            "科技",
    };

    private static List<Movie> list;
    private static long count = 0;

    public static List<Movie> getList() {
        if (list == null) {
            list = setupMovies();
        }
        return list;
    }

    public static List<Movie> setupMovies() {
        list = new ArrayList<>();
        String title[] = {
                "BINGLE BANGLE",
                "成长之重量",
                "后来的我们",
                "一生所爱",
                "少年志"
        };

        String description = "一生所爱 电视剧《大话西游之爱你一万年》" +
                "插曲 720P高清MV迅雷下载。《大话西游之爱你一万年》由“大" +
                "话”经典IP改编,在对原作魔幻、爱情、喜剧元素充分保留的前提" +
                "下,对剧情进行了全新的改编,主演阵容也呈现全面的年轻化,堪称" +
                "“青春版大话”。原版“大话”中的片尾曲《一生所爱》更是早已成" +
                "为历久弥新的经典";
        String studio[] = {
                "播放室1", "播放室2", "播放室3", "播放室3", "播放室4"
        };
        String videoUrl[] = {
                Config.MAIN_SERVER + Config.MOVIE_DIR + "/test1.mp4",
                Config.MAIN_SERVER + Config.MOVIE_DIR + "/test2.mp4",
                Config.MAIN_SERVER + Config.MOVIE_DIR + "/test3.mp4",
                Config.MAIN_SERVER + Config.MOVIE_DIR + "/test4.mp4",
                Config.MAIN_SERVER + Config.MOVIE_DIR + "/test5.mp4"
        };
        String bgImageUrl[] = {
                Config.MAIN_SERVER + Config.MOVIE_DIR + Config.IMAGE_DIR + "/test1.png",
                Config.MAIN_SERVER + Config.MOVIE_DIR + Config.IMAGE_DIR + "/test2.png",
                Config.MAIN_SERVER + Config.MOVIE_DIR + Config.IMAGE_DIR + "/test3.png",
                Config.MAIN_SERVER + Config.MOVIE_DIR + Config.IMAGE_DIR + "/test4.png",
                Config.MAIN_SERVER + Config.MOVIE_DIR + Config.IMAGE_DIR + "/test5.png"
        };
        String cardImageUrl[] = {
                Config.MAIN_SERVER + Config.MOVIE_DIR + Config.IMAGE_DIR + "/test1.png",
                Config.MAIN_SERVER + Config.MOVIE_DIR + Config.IMAGE_DIR + "/test2.png",
                Config.MAIN_SERVER + Config.MOVIE_DIR + Config.IMAGE_DIR + "/test3.png",
                Config.MAIN_SERVER + Config.MOVIE_DIR + Config.IMAGE_DIR + "/test4.png",
                Config.MAIN_SERVER + Config.MOVIE_DIR + Config.IMAGE_DIR + "/test5.png"
        };

        for (int index = 0; index < title.length; ++index) {
            list.add(
                    buildMovieInfo(
                            "category",
                            title[index],
                            description,
                            studio[index],
                            videoUrl[index],
                            cardImageUrl[index],
                            bgImageUrl[index]));
        }

        return list;
    }

    private static Movie buildMovieInfo(String category, String title,
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