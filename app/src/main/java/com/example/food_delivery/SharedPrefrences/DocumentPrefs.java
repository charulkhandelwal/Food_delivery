    package com.example.food_delivery.SharedPrefrences;

    import android.content.Context;
    import android.content.SharedPreferences;
    import android.util.Log;

    import com.example.food_delivery.Model.DocumentModel;
    import com.google.gson.Gson;
    import com.google.gson.reflect.TypeToken;

    import java.lang.reflect.Type;
    import java.util.ArrayList;

    public class DocumentPrefs {

        private static final String PREF_NAME = "DocsPrefs";
        private static final String KEY_DOC_LIST = "document_list";
        private static final String KEY_TOKEN = "token";
        private static final String KEY_PARTNER_ID = "partner_id";
        private static final String KEY_PROFILE = "profile";
        private static final String KEY_DOCS_UPLOADED = "docs_uploaded";

        private static final String TAG = "DOC_PREFS";

        // ✅ Save Document List
        public static void saveDocumentList(Context context, ArrayList<DocumentModel> list) {
            if (list == null) list = new ArrayList<>();

            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            String json = new Gson().toJson(list);
            editor.putString(KEY_DOC_LIST, json);
            editor.apply();

            Log.d(TAG, "✅ Saved list: " + json);

           /* // 👇 Add this line
            Exception e = new Exception();
            Log.e(TAG, "✅ Saved list (" + list.size() + " items). Stack trace:", e);*/
        }

        // ✅ Get Document List
        public static ArrayList<DocumentModel> getDocumentList(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String json = prefs.getString(KEY_DOC_LIST, null);

            if (json == null || json.isEmpty()) {
                Log.w(TAG, "⚠ No saved docs found, returning empty list");
                return new ArrayList<>();
            }

            try {
                Type type = new TypeToken<ArrayList<DocumentModel>>(){}.getType();
                ArrayList<DocumentModel> list = new Gson().fromJson(json, type);
                Log.d(TAG, "📂 Loaded list: " + json);
                return list != null ? list : new ArrayList<>();
            } catch (Exception e) {
                Log.e(TAG, "❌ Error reading saved docs: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        // ✅ Update or Add Single Document safely
        public static void updateSingleDocument(Context context, DocumentModel newDoc) {
            if (newDoc == null || newDoc.getDocName() == null) return; // safeguard

            ArrayList<DocumentModel> docs = getDocumentList(context); // always non-null
            boolean found = false;

            // Replace if doc with same name exists
            for (int i = 0; i < docs.size(); i++) {
                DocumentModel existing = docs.get(i);
                if (existing.getDocName() != null &&
                        existing.getDocName().equalsIgnoreCase(newDoc.getDocName())) {

                    // ✅ Merge: if newDoc.imageUri is null, keep old one
                    if (newDoc.getImageUri() == null || newDoc.getImageUri().isEmpty()) {
                        newDoc.setImageUri(existing.getImageUri());
                    }

                    docs.set(i, newDoc); // replace
                    found = true;
                    break;
                }
            }

            if (!found) docs.add(newDoc); // add new if not exists
            saveDocumentList(context, docs); // save full list
        }




        // ✅ Token and Partner Info (unchanged)
        public static void saveToken(Context context, String token) {
            getPrefs(context).edit().putString(KEY_TOKEN, token).apply();
        }

        public static String getToken(Context context) {
            return getPrefs(context).getString(KEY_TOKEN, null);
        }

        public static void clearToken(Context context) {
            getPrefs(context).edit().remove(KEY_TOKEN).apply();
        }

        public static void savePartnerId(Context context, String partnerId) {
            getPrefs(context).edit().putString(KEY_PARTNER_ID, partnerId).apply();
        }

        public static String getPartnerId(Context context) {
            return getPrefs(context).getString(KEY_PARTNER_ID, null);
        }

        public static void clearPartnerId(Context context) {
            getPrefs(context).edit().remove(KEY_PARTNER_ID).apply();
        }

        public static void saveProfile(Context context, String profileJson) {
            getPrefs(context).edit().putString(KEY_PROFILE, profileJson).apply();
        }

        public static String getProfile(Context context) {
            return getPrefs(context).getString(KEY_PROFILE, null);
        }

        public static void clearProfile(Context context) {
            getPrefs(context).edit().remove(KEY_PROFILE).apply();
        }

        public static void setDocsUploaded(Context context, boolean uploaded) {
            getPrefs(context).edit().putBoolean(KEY_DOCS_UPLOADED, uploaded).apply();
        }

        public static boolean getDocsUploaded(Context context) {
            return getPrefs(context).getBoolean(KEY_DOCS_UPLOADED, false);
        }

        public static void clearAll(Context context) {
            getPrefs(context).edit().clear().apply();
        }

        private static SharedPreferences getPrefs(Context context) {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }
