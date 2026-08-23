package net.crinoidea.liberty.settings;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPreferences {
    private static final String FILE_NAME = "liberty_local_settings";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_PRIVACY_VERSION = "privacy_version";
    private static final String KEY_PENDING_FULL_UNINSTALL = "pending_full_uninstall";
    private static final int CURRENT_PRIVACY_VERSION = 6;

    private AppPreferences() {
    }

    public static boolean hasLanguageSelection(Context context) {
        return preferences(context).contains(KEY_LANGUAGE);
    }

    public static String getStoredLanguage(Context context) {
        return preferences(context).getString(KEY_LANGUAGE, null);
    }

    public static void setLanguage(Context context, String languageTag) {
        preferences(context)
                .edit()
                .putString(KEY_LANGUAGE, languageTag)
                .apply();
    }

    public static boolean hasAcceptedCurrentPrivacyPolicy(Context context) {
        return preferences(context).getInt(KEY_PRIVACY_VERSION, 0)
                >= CURRENT_PRIVACY_VERSION;
    }

    public static void acceptCurrentPrivacyPolicy(Context context) {
        preferences(context)
                .edit()
                .putInt(KEY_PRIVACY_VERSION, CURRENT_PRIVACY_VERSION)
                .apply();
    }

    public static boolean isFullUninstallPending(Context context) {
        return preferences(context).getBoolean(KEY_PENDING_FULL_UNINSTALL, false);
    }

    public static void setFullUninstallPending(Context context, boolean pending) {
        preferences(context)
                .edit()
                .putBoolean(KEY_PENDING_FULL_UNINSTALL, pending)
                .apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE
        );
    }
}
