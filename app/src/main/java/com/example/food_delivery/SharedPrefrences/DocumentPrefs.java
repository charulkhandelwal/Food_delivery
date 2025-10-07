package com.example.food_delivery.SharedPrefrences;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.food_delivery.Model.DocumentModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class DocumentPrefs {

    private static final String PREF_NAME = "DocsPrefs";
    private static final String KEY_DOC_LIST = "document_list";
    private static final String KEY_TOKEN = "token"; // 🔥 Token key

    // ----------- Documents List Save / Get -------------
    public static void saveDocumentList(Context context, ArrayList<DocumentModel> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(list);
        editor.putString(KEY_DOC_LIST, json);
        editor.apply();
    }

    public static ArrayList<DocumentModel> getDocumentList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_DOC_LIST, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<DocumentModel>>(){}.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }


    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_TOKEN, null);
    }

    public static void clearToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}
