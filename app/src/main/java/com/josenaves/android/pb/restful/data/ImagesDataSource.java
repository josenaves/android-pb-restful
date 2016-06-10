package com.josenaves.android.pb.restful.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import com.josenaves.android.pb.restful.Image;
import com.josenaves.android.pb.restful.ImageBase64;
import com.josenaves.android.pb.restful.StorageUtils;

import java.util.ArrayList;
import java.util.List;

import okio.ByteString;

/**
 * Image Data Access Object.
 */
public final class ImagesDataSource {

    private static final String TAG = ImagesDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private ImagesDatabaseHelper dbHelper;

    private String[] columns = { ImagesDatabaseHelper.COLUMN_ID, ImagesDatabaseHelper.COLUMN_NAME,
            ImagesDatabaseHelper.COLUMN_DATE, ImagesDatabaseHelper.COLUMN_IMAGE };

    public ImagesDataSource(Context context) {
        dbHelper = ImagesDatabaseHelper.getInstance(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createImage(Image image) {
        Log.d(TAG, "createImage...");
        saveImage(image.id, image.name, image.date, image.image_data.toByteArray());
    }

    public void createImageBase64(ImageBase64 image) {
        Log.d(TAG, "createImageBase64...");
        byte[] imageData = Base64.decode(image.image_data, Base64.DEFAULT);
        saveImage(image.id, image.name, image.datetime, imageData);
    }

    private void saveImage(String uuid, String name, String datetime, byte[] imageData) {
        String packageName= dbHelper.getContext().getApplicationContext().getPackageName();
        String imageLocation = StorageUtils.saveFileInExternalStorage(
                packageName,
                name,
                imageData);

        if (imageLocation != null) {
            //database.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(ImagesDatabaseHelper.COLUMN_ID, uuid);
            values.put(ImagesDatabaseHelper.COLUMN_NAME, name);
            values.put(ImagesDatabaseHelper.COLUMN_DATE, datetime);
            values.put(ImagesDatabaseHelper.COLUMN_IMAGE, imageLocation);

            long id = database.insert(ImagesDatabaseHelper.TABLE_IMAGES, null, values);
            Log.d(TAG, "Image saved in the database - rowid = " + id);
            //database.endTransaction();
        }
        else {
            Log.e(TAG, "Could not save image on device storage :(");
        }
    }

    public List<Image> getAllImages(){
        Log.d(TAG, "getAllImages...");

        List<Image> images = new ArrayList<>();
        Cursor cursor = database.query(ImagesDatabaseHelper.TABLE_IMAGES, columns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            images.add(imageFromCursor(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return images;
    }

    private Image imageFromCursor(Cursor cursor) {
        String location = cursor.getString(cursor.getColumnIndex(ImagesDatabaseHelper.COLUMN_IMAGE));
        return new Image.Builder()
                .id(cursor.getString(cursor.getColumnIndex(ImagesDatabaseHelper.COLUMN_ID)))
                .name(cursor.getString(cursor.getColumnIndex(ImagesDatabaseHelper.COLUMN_NAME)))
                .date(cursor.getString(cursor.getColumnIndex(ImagesDatabaseHelper.COLUMN_DATE)))
                .image_data(ByteString.of(StorageUtils.readFileFromExternalStorage(location)))
                .build();

    }

}
