package com.josenaves.android.pb.restful;

public class ImageBase64 {
    public String id;
    public String name;
    public String datetime;
    public String image_data;

    @Override
    public String toString() {
        return "ImageBase64{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", date='" + datetime + '\'' +
                ", imageData='" + image_data + '\'' +
                '}';
    }
}
