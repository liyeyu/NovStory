package com.liyeyu.novstory.api;

import com.liyeyu.novstory.entry.LrcRes;
import com.liyeyu.rxhttp.ApiService;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Liyeyu on 2016/8/22.
 */
public interface BaseApi extends ApiService {
    @GET
    Call<LrcRes> get(@Url String url);
}
