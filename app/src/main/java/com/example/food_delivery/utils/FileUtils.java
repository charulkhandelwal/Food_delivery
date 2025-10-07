package com.example.food_delivery.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class FileUtils {

    // Convert content Uri -> Absolute file path (String)
    public static String getPath(Context context, Uri uri) {
        String result = null;
        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            if (cursor.moveToFirst()) {
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        return result;
    }
}

