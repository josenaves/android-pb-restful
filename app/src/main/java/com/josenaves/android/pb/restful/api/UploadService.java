package com.josenaves.android.pb.restful.api;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UploadService {
    @POST("upload")
    Call<ResponseBody> upload(@Body RequestBody binaryData);
}
