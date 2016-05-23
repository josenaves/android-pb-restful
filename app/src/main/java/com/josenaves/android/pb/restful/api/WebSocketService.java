package com.josenaves.android.pb.restful.api;

import android.content.Context;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback;
import com.koushikdutta.async.http.WebSocket;
import android.util.Log;

import com.josenaves.android.pb.restful.PreferencesUtils;

import java.util.concurrent.TimeoutException;

public final class WebSocketService {

    private static final String TAG = WebSocketService.class.getSimpleName();

    private final AsyncHttpClient connection = AsyncHttpClient.getDefaultInstance();

    private WebSocketAPI wsClient;

    private Context context;
    private String wsURI;

    public WebSocketService(WebSocketAPI wsClient) {
        this.wsClient = wsClient;
        this.context = wsClient.getContext();
        this.wsURI = String.format("ws://%s:%s",
                PreferencesUtils.getHost(context),
                PreferencesUtils.getWebsocketPort(context));
    }

    public void request() {
        connection.websocket(wsURI, null, new WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket ws) {
                if (ex != null) {
                    if (ex instanceof TimeoutException) {
                        Log.e(TAG, "Timeout - server must be down :(");
                        wsClient.onTimeout((TimeoutException)ex);
                    }
                    return;
                }

                Log.d(TAG, "Connected.");

                ws.setDataCallback(new DataCallback() {
                    @Override
                    public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                        Log.d(TAG, "Got some bytes!");
                        wsClient.onResponse(bb.getAllByteArray());
                        bb.recycle();
                    }
                });
            }
        });
    }
}
