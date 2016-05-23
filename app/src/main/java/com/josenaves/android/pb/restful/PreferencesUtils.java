package com.josenaves.android.pb.restful;

import android.content.Context;
import android.content.SharedPreferences;

public final class PreferencesUtils {

    public final static String PREF_HOST = "pref_server_host";
    public final static String PREF_HTTP_PORT = "pref_http_port";
    public final static String PREF_WEBSOCKET_PORT = "pref_ws_port";

    public static String getHost(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_HOST, 0);
        return prefs.getString(PREF_HOST, context.getString(R.string.default_host));
    }

    public static String getHttpPort(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_HTTP_PORT, 0);
        return prefs.getString(PREF_HTTP_PORT, context.getString(R.string.default_http_port));
    }

    public static String getWebsocketPort(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_WEBSOCKET_PORT, 0);
        return prefs.getString(PREF_WEBSOCKET_PORT, context.getString(R.string.default_ws_port));
    }

}
