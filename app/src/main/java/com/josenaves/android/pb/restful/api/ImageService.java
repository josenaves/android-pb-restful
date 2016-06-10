package com.josenaves.android.pb.restful.api;

import com.josenaves.android.pb.restful.Image;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ImageService {
    @GET("random")
    Call<Image> getRandomImage();

    @GET("florianopolis")
    Call<Image> getImageFlorianopolis();

    @GET("tree")
    Call<Image> getImageTree();
}
