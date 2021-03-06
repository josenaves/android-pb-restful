package com.josenaves.android.pb.restful.api;

import android.content.Context;
import android.util.Log;
import com.josenaves.android.pb.restful.Image;
import com.josenaves.android.pb.restful.utils.PreferencesUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.wire.WireConverterFactory;

public class ImageAPI {

    private static final String TAG = ImageAPI.class.getSimpleName();

    // Trailing slash is needed
    //private static final String BASE_URL = "http://192.168.0.16:9090/";
    //private static final String BASE_URL = PreferencesUtils.getServerHost();

    private Retrofit retrofit;

    public ImageAPI(Context context) {
        String host = String.format("http://%s:%s/",
                PreferencesUtils.getHost(context), PreferencesUtils.getHttpPort(context));

        retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(WireConverterFactory.create())
                .build();
    }

    public Image getRandomImage() {
        ImageService service = retrofit.create(ImageService.class);
        Call<Image> image = service.getRandomImage();
        try {
            Response<Image> response = image.execute();
            return response.body();
        }
        catch (IOException io) {
            Log.e(TAG, io.getMessage());
        }
        return null;
    }

    public Image getFlorianoplisImage() throws Exception {
        ImageService service = retrofit.create(ImageService.class);
        Call<Image> image = service.getImageFlorianopolis();
        try {
            Response<Image> response = image.execute();
            return response.body();
        }
        catch (Throwable throwable) {
            Log.e(TAG, throwable.getMessage());

            if (throwable instanceof SocketTimeoutException) {
                throw new Exception("Connection timeout");
            }
        }
        return null;
    }

    public Image getTreeImage() throws Exception {
        ImageService service = retrofit.create(ImageService.class);
        Call<Image> image = service.getImageTree();
        try {
            Response<Image> response = image.execute();
            return response.body();
        }
        catch (Throwable throwable) {
            Log.e(TAG, throwable.getMessage());

            if (throwable instanceof SocketTimeoutException) {
                throw new Exception("Connection timeout");
            }
        }
        return null;
    }
}