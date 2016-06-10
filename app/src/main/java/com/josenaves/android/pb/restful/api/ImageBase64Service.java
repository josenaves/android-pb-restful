package com.josenaves.android.pb.restful.api;

import com.josenaves.android.pb.restful.ImageBase64;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ImageBase64Service {
    @GET("base64/florianopolis")
    Call<ImageBase64> getImageFlorianopolisBase64();

    @GET("base64/tree")
    Call<ImageBase64> getImageTreeBase64();
}
