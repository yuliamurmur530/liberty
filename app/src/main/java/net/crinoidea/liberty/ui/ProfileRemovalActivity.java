package net.crinoidea.liberty.ui;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import net.crinoidea.liberty.R;
public final class ProfileRemovalActivity extends BaseActivity {
    private DevicePolicyManager policyManager;
    private Button removeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!enforceReleaseSignature()) {
            return;
        }

        policyManager = getSystemService(DevicePolicyManager.class);
        if (!canRemoveProfile()) {
            Toast.makeText(this, R.string.remove_profile_not_available, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_profile_removal);
        configureWindow(findViewById(R.id.root));
        removeButton = findViewById(R.id.confirmRemoveProfileButton);

        findViewById(R.id.cancelRemoveProfileButton).setOnClickListener(view -> finish());
        removeButton.setOnClickListener(view -> showFinalConfirmation());
    }

    private boolean canRemoveProfile() {
        if (policyManager == null) {
            return false;
        }
        try {
            return policyManager.isProfileOwnerApp(getPackageName());
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private void showFinalConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_profile_final_title)
                .setMessage(R.string.remove_profile_final_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.remove_profile_confirm, (dialog, which) -> removeProfile())
                .show();
    }

    private void removeProfile() {
        removeButton.setEnabled(false);
        try {
            policyManager.wipeData(0, getString(R.string.remove_profile_reason));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException exception) {
            removeButton.setEnabled(true);
            Toast.makeText(this, R.string.remove_profile_failed, Toast.LENGTH_LONG).show();
        }
    }
}
