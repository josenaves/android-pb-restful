package com.josenaves.android.pb.restful.api;

import java.util.concurrent.TimeoutException;

public interface SocketListener {
    void onTimeout(TimeoutException e);
    void onException(Exception e);
    void onResponse(byte[] response);
}
