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

    // Existing keys
    private static final String KEY_DOC_LIST = "document_list";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_PARTNER_ID = "partner_id";

    // ✅ Newly added keys
    private static final String KEY_PROFILE = "profile";
    private static final String KEY_DOCS_UPLOADED = "docs_uploaded";

    // ----------- 📄 Documents List Save / Get -------------
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

    // ----------- 🔑 Token Save / Get / Clear -------------
    public static void saveToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, null);
    }

    public static void clearToken(Context context) {
        getPrefs(context).edit().remove(KEY_TOKEN).apply();
    }

    // ----------- 🧍 Partner ID Save / Get / Clear -------------
    public static void savePartnerId(Context context, String partnerId) {
        getPrefs(context).edit().putString(KEY_PARTNER_ID, partnerId).apply();
    }

    public static String getPartnerId(Context context) {
        return getPrefs(context).getString(KEY_PARTNER_ID, null);
    }

    public static void clearPartnerId(Context context) {
        getPrefs(context).edit().remove(KEY_PARTNER_ID).apply();
    }

    // ----------- 🧾 Profile Save / Get (New) -------------
    public static void saveProfile(Context context, String profileJson) {
        getPrefs(context).edit().putString(KEY_PROFILE, profileJson).apply();
    }

    public static String getProfile(Context context) {
        return getPrefs(context).getString(KEY_PROFILE, null);
    }

    public static void clearProfile(Context context) {
        getPrefs(context).edit().remove(KEY_PROFILE).apply();
    }

    // ----------- ✅ Docs Upload Status -------------
    public static void setDocsUploaded(Context context, boolean uploaded) {
        getPrefs(context).edit().putBoolean(KEY_DOCS_UPLOADED, uploaded).apply();
    }

    public static boolean getDocsUploaded(Context context) {
        return getPrefs(context).getBoolean(KEY_DOCS_UPLOADED, false);
    }

    // ----------- 🔄 Clear All -------------
    public static void clearAll(Context context) {
        getPrefs(context).edit().clear().apply();
    }

    // ----------- Internal Helper -------------
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
