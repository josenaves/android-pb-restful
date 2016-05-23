package com.josenaves.android.pb.restful.api;

import android.content.Context;
import android.util.Log;

import com.josenaves.android.pb.restful.ImageBase64;
import com.josenaves.android.pb.restful.PreferencesUtils;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageBase64API {

    private static final String TAG = ImageBase64API.class.getSimpleName();

    // Trailing slash is needed
    private Retrofit retrofitBase64;

    public ImageBase64API(Context context) {
        retrofitBase64 = new Retrofit.Builder()
                .baseUrl(PreferencesUtils.getServerHost(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public ImageBase64 getFlorianopolisBase64Image() {
        ImageBase64Service service = retrofitBase64.create(ImageBase64Service.class);
        Call<ImageBase64> image = service.getImageFlorianopolisBase64();
        try {
            Response<ImageBase64> response = image.execute();
            return response.body();
        }
        catch (IOException io) {
            Log.e(TAG, io.getMessage());
        }
        return null;
    }

}