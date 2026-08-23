package net.crinoidea.liberty.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import net.crinoidea.liberty.R;
import net.crinoidea.liberty.settings.AppPreferences;
import net.crinoidea.liberty.settings.LocaleController;

public final class PrivacyActivity extends BaseActivity {
    private static final String POLICY_URL = "https://liberty.crinoidea.net/privacy";
    private boolean acceptanceRequired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!enforceReleaseSignature()) {
            return;
        }
        setContentView(R.layout.activity_privacy);
        configureWindow(findViewById(R.id.root));

        acceptanceRequired = !AppPreferences.hasAcceptedCurrentPrivacyPolicy(this);
        Button actionButton = findViewById(R.id.privacyActionButton);
        actionButton.setText(
                acceptanceRequired ? R.string.privacy_continue : R.string.close
        );
        actionButton.setOnClickListener(view -> complete());
        findViewById(R.id.openPolicyWebsiteButton).setOnClickListener(
                view -> openWebsite()
        );
    }

    private void complete() {
        if (!acceptanceRequired) {
            finish();
            return;
        }

        AppPreferences.acceptCurrentPrivacyPolicy(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openWebsite() {
        String language = LocaleController.getSelectedLanguage(this);
        Uri uri = Uri.parse(POLICY_URL + ("en".equals(language) ? "#en" : "#ru"));
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.browser_not_found, Toast.LENGTH_LONG).show();
        }
    }
}
