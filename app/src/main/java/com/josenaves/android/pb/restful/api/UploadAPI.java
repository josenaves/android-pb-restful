package com.josenaves.android.pb.restful.api;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.josenaves.android.pb.restful.Image;
import com.josenaves.android.pb.restful.utils.PreferencesUtils;
import com.josenaves.android.pb.restful.utils.StorageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    private Context context;

    public UploadAPI(Context context) {
        this.context = context;
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
        Log.d(TAG, String.format("Size of encoded data to be transferred: %s bytes", encodedData.length));

        // just to get a file with binary data encoded with Protocol Buffers --> will be used in load tests
        //saveBinaryPost(encodedData);

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

    /**
     * Save a copy of POST request that is sent in POST request in the filesystem.
     * It will be used just one time to get a file with binary data encoded with
     * Protocol Buffers --> will be used in load tests.
     */
    private void saveBinaryPost(byte[] protocolBuffersData) {

        String packageName= context.getApplicationContext().getPackageName();

        String absolutePath = String.format("/%s/%s/%s/",
                //Environment.getExternalStorageDirectory().getAbsolutePath(),
                "sdcard",
                "Android/data",
                packageName);

        Log.d(TAG, String.format("Path where the post.bin will be saved: %s" ,absolutePath));

        // create the directory structure
        File folder = new File(absolutePath);
        if (folder.mkdirs()){
            Log.d(TAG, "Folders created at " + absolutePath);
        } else {
            Log.e(TAG, "Error creating dirs");
        }

        // get a reference for the file to be created
        File fileToBeSaved = new File(folder, "post.bin");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileToBeSaved);
            fos.write(protocolBuffersData);
            fos.close();
            Log.d(TAG, fileToBeSaved.getAbsolutePath());
        }
        catch(FileNotFoundException fnf) {
            Log.e(TAG, "Could not find path: " +  fnf.getMessage());
        }
        catch(IOException io){
            Log.e(TAG, io.getMessage());
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException io){
                    Log.e(TAG, "Error closing file: " + io.getMessage());
                }
            }
        }
    }
}
