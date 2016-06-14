package com.josenaves.android.pb.restful.utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteUtils {
    public static byte[] convertStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[10240];
        int i = Integer.MAX_VALUE;
        while ((i = is.read(buff, 0, buff.length)) > 0) {
            baos.write(buff, 0, i);
        }
        return baos.toByteArray(); // be sure to close InputStream in calling function
    }
}
