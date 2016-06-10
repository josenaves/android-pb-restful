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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public final class WebSocketService {

    private static final String TAG = WebSocketService.class.getSimpleName();

    private List<SocketListener> listeners = new ArrayList<>();

    private String wsURI;

    public WebSocketService(Context context) {
        this.wsURI = String.format("ws://%s:%s",
                PreferencesUtils.getHost(context),
                PreferencesUtils.getWebsocketPort(context));
    }

    public void request() {
        final AsyncHttpClient connection = AsyncHttpClient.getDefaultInstance();
        connection.websocket(wsURI, null, new WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket ws) {
                if (ex != null) {
                    Log.e(TAG, ":( Exception: " + ex.getMessage());
                    if (ex instanceof TimeoutException) {
                        Log.e(TAG, ":( TimeoutException: " + ex.getMessage());
                        notifyTimeout((TimeoutException)ex);
                    }
                    else {
                        notifyException(ex);
                    }
                    return;
                }

                Log.d(TAG, "Connected.");

                ws.setDataCallback(new DataCallback() {
                    @Override
                    public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                        Log.d(TAG, "Got some bytes!");
                        notifyData(bb.getAllByteArray());
                        emitter.close();
                        bb.recycle();
                    }
                });
            }
        });
    }

    public void registerListener(SocketListener socketListener) {
        listeners.add(socketListener);
    }

    public void unregisterListener(SocketListener socketListener) {
        listeners.remove(socketListener);
    }

    private void notifyTimeout(TimeoutException e) {
        for (SocketListener l: listeners) {
            l.onTimeout(e);
        }
    }

    private void notifyException(Exception e) {
        for (SocketListener l: listeners) {
            l.onException(e);
        }
    }

    private void notifyData(byte[] data) {
        for (SocketListener l: listeners) {
            l.onResponse(data);
        }
    }


}
