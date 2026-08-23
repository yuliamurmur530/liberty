package net.crinoidea.liberty.ui;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import net.crinoidea.liberty.admin.AdminComponents;
import net.crinoidea.liberty.admin.SecurityPolicies;
import net.crinoidea.liberty.settings.LocaleController;

public final class PolicyComplianceActivity extends BaseActivity {
    public static final String EXTRA_INITIAL_LANGUAGE =
            "net.crinoidea.liberty.extra.INITIAL_LANGUAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!enforceOfficialSignature()) {
            setResult(RESULT_CANCELED);
            return;
        }
        String action = getIntent().getAction();

        if (DevicePolicyManager.ACTION_GET_PROVISIONING_MODE.equals(action)) {
            Intent result = new Intent();
            result.putExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_MODE,
                    DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE
            );
            setResult(RESULT_OK, result);
            finish();
            return;
        }

        DevicePolicyManager policyManager = getSystemService(DevicePolicyManager.class);
        if (policyManager != null && policyManager.isProfileOwnerApp(getPackageName())) {
            applyInitialLanguage();
            SecurityPolicies.apply(this);
            policyManager.setProfileEnabled(AdminComponents.receiver(this));
        }

        setResult(RESULT_OK);
        if (DevicePolicyManager.ACTION_PROVISIONING_SUCCESSFUL.equals(action)) {
            startActivity(new Intent(this, VaultActivity.class));
        }
        finish();
    }

    @SuppressWarnings("deprecation")
    private void applyInitialLanguage() {
        PersistableBundle adminExtras = getIntent().getParcelableExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE
        );
        if (adminExtras == null) {
            return;
        }
        String language = adminExtras.getString(EXTRA_INITIAL_LANGUAGE);
        if (language != null) {
            LocaleController.selectLanguage(this, language);
        }
    }
}
