package com.josenaves.android.pb.restful.api;

import android.content.Context;

import java.util.concurrent.TimeoutException;

public interface WebSocketAPI {
    void onTimeout(TimeoutException e);
    void onResponse(byte[] response);
    Context getContext();
}
