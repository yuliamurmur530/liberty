package net.crinoidea.liberty.settings;

import android.app.LocaleManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public final class LocaleController {
    public static final String RUSSIAN = "ru";
    public static final String ENGLISH = "en";

    private LocaleController() {
    }

    public static Context wrap(Context base) {
        String languageTag = getSelectedLanguage(base);
        if (languageTag == null) {
            return base;
        }

        Locale locale = Locale.forLanguageTag(languageTag);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration(
                base.getResources().getConfiguration()
        );
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return base.createConfigurationContext(configuration);
    }

    public static boolean hasLanguageSelection(Context context) {
        return getSelectedLanguage(context) != null;
    }

    public static String getSelectedLanguage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            LocaleManager manager = context.getSystemService(LocaleManager.class);
            if (manager != null && !manager.getApplicationLocales().isEmpty()) {
                return normalize(manager.getApplicationLocales().get(0).getLanguage());
            }
        }
        return normalize(AppPreferences.getStoredLanguage(context));
    }

    public static void selectLanguage(Context context, String languageTag) {
        String normalized = normalize(languageTag);
        if (normalized == null) {
            throw new IllegalArgumentException("Unsupported Liberty language");
        }

        AppPreferences.setLanguage(context, normalized);
        Locale.setDefault(Locale.forLanguageTag(normalized));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            LocaleManager manager = context.getSystemService(LocaleManager.class);
            if (manager != null) {
                manager.setApplicationLocales(LocaleList.forLanguageTags(normalized));
            }
        }
    }

    private static String normalize(String languageTag) {
        if (languageTag == null) {
            return null;
        }
        String language = Locale.forLanguageTag(languageTag).getLanguage();
        if (RUSSIAN.equals(language)) {
            return RUSSIAN;
        }
        if (ENGLISH.equals(language)) {
            return ENGLISH;
        }
        return null;
    }
}
