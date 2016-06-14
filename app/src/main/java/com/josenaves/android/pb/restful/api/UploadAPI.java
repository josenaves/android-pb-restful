package com.josenaves.android.pb.restful.api;

import android.content.Context;
import android.util.Log;

import com.josenaves.android.pb.restful.Image;
import com.josenaves.android.pb.restful.utils.PreferencesUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.wire.WireConverterFactory;

public class UploadAPI {

    private static final String TAG = UploadAPI.class.getSimpleName();

    private Retrofit retrofit;

    public UploadAPI(Context context) {
        String host = String.format("http://%s:%s/",
                PreferencesUtils.getHost(context), PreferencesUtils.getPrefWsCompressPort(context));

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .client(client)
                .addConverterFactory(WireConverterFactory.create())
                .build();
    }

    public void uploadImage(Image imageToUpload) throws Exception {
        UploadService service = retrofit.create(UploadService.class);

        byte[] encodedData = imageToUpload.encode();
        Log.d(TAG, String.format("Size of encoded data to be transfered: %s bytes", encodedData.length));

        RequestBody requestBody = RequestBody
                .create(MediaType.parse("application/x-protobuf"), encodedData);

        Call<ResponseBody> api = service.upload(requestBody);
        try {
            Response<ResponseBody> response = api.execute();
            Log.d(TAG, response.toString());
        }
        catch (Throwable throwable) {
            Log.e(TAG, throwable.getMessage());

            if (throwable instanceof SocketTimeoutException) {
                throw new Exception("Connection timeout");
            }
        }
    }

}

class LoggingInterceptor implements Interceptor {

    private static final String TAG = LoggingInterceptor.class.getSimpleName();

    @Override public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        Log.i(TAG, String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        okhttp3.Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        Log.i(TAG, String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        return response;
    }
}