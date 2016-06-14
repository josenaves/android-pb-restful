package com.josenaves.android.pb.restful.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;

public final class StorageUtils {

    private static final String TAG = StorageUtils.class.getSimpleName();

    private static final String PREFIX = "Android/data";

    public static byte[] readFileFromExternalStorage(String filePath) {

        File fileToBeRead = new File(filePath);
        byte[] data = new byte[(int)fileToBeRead .length()];

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            fis.read(data);
            fis.close();
            return data;
        }
        catch(FileNotFoundException fnf) {
            Log.e(TAG, "Could not find path: " +  fnf.getMessage());
        }
        catch(IOException io){
            Log.e(TAG, io.getMessage());
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException io){
                    Log.e(TAG, "Error closing file: " + io.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Create a storage path folders based on datetime on the device
     * @return if success, the complete path for the saved file; otherwise, null
     */
    public static String saveFileInExternalStorage(String packageName, String filename, byte[] data) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = 1 + cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        int millisecond = cal.get(Calendar.MILLISECOND);

        String absolutePath = String.format("%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/",
                Environment.getExternalStorageDirectory().getAbsolutePath(),
                PREFIX,
                packageName,
                year, month, day, hour, minute, second, millisecond);

        Log.d(TAG, absolutePath);

        // create the directory structure
        File folders = new File(absolutePath);
        if (folders.mkdirs()){
            Log.d(TAG, "Folders created at " + absolutePath);
        } else {
            Log.e(TAG, "Error creating dirs");
        }

        // get a reference for the file to be created
        File fileToBeSaved = new File(folders, filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileToBeSaved);
            fos.write(data);
            fos.close();
            return fileToBeSaved.getAbsolutePath();
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
        return null;
    }

    public static void localFile(Context context) {
        try {
            // Use Activity method to create a file in the writable directory
            FileOutputStream fos = context.openFileOutput("filename", Context.MODE_WORLD_WRITEABLE);

            // Create buffered writer
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write("Hi, I'm writing stuff");
            writer.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
