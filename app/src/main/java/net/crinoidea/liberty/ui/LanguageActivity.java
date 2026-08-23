package net.crinoidea.liberty.ui;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;

import net.crinoidea.liberty.R;
import net.crinoidea.liberty.settings.AppPreferences;
import net.crinoidea.liberty.settings.LocaleController;

public final class LanguageActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!enforceReleaseSignature()) {
            return;
        }
        setContentView(R.layout.activity_language);
        configureWindow(findViewById(R.id.root));

        findViewById(R.id.russianButton).setOnClickListener(
                view -> selectLanguage(LocaleController.RUSSIAN)
        );
        findViewById(R.id.englishButton).setOnClickListener(
                view -> selectLanguage(LocaleController.ENGLISH)
        );
    }

    private void selectLanguage(String languageTag) {
        LocaleController.selectLanguage(this, languageTag);
        Class<?> destination;
        if (isManagedProfile()) {
            destination = VaultActivity.class;
        } else {
            destination = AppPreferences.hasAcceptedCurrentPrivacyPolicy(this)
                    ? MainActivity.class
                    : PrivacyActivity.class;
        }
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean isManagedProfile() {
        UserManager userManager = getSystemService(UserManager.class);
        if (userManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return userManager.isManagedProfile();
        }
        DevicePolicyManager policyManager = getSystemService(DevicePolicyManager.class);
        return policyManager != null && policyManager.isProfileOwnerApp(getPackageName());
    }
}
