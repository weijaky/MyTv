package com.huawei.demo.mytv.cloud.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by jaky on 2018/2/11 0011.
 */

public class FastJsonConverterFactory extends Converter.Factory {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(final Type type, Annotation[] annotations, Retrofit retrofit) {
        return new Converter<ResponseBody, String>() {
            @Override
            public String convert(ResponseBody value) throws IOException {
                return JSON.parseObject(value.string(), type, ParserConfig.getGlobalInstance(), JSON.DEFAULT_PARSER_FEATURE, new Feature[0]);
            }
        };
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return new Converter<String, RequestBody>() {
            @Override
            public RequestBody convert(String value) throws IOException {
                return RequestBody.create(MEDIA_TYPE, value);
            }
        };
    }
}
