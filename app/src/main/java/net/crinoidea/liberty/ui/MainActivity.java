package net.crinoidea.liberty.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.CrossProfileApps;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.crinoidea.liberty.R;
import net.crinoidea.liberty.admin.AdminComponents;
import net.crinoidea.liberty.settings.AppPreferences;
import net.crinoidea.liberty.settings.LocaleController;

import java.util.Collections;
import java.util.List;

public final class MainActivity extends BaseActivity {
    private static final int REQUEST_PROVISION_PROFILE = 1002;
    private static final int REQUEST_CHOOSE_ACCOUNT = 1003;
    private static final String EXTRA_OPEN_PROTECTED_SPACE = "open_protected_space";

    private DevicePolicyManager policyManager;
    private CrossProfileApps crossProfileApps;
    private TextView heroTitle;
    private TextView heroBody;
    private LinearLayout setupSteps;
    private LinearLayout statusCard;
    private TextView statusTitle;
    private TextView statusBody;
    private Button createSpaceButton;
    private Button chooseAccountButton;
    private Button openProtectedSpaceButton;
    private Button removeLibertyButton;
    private TextView dataNote;
    private boolean fullUninstallDialogVisible;
    private Account selectedMigrationAccount;
    private boolean waitingForProtectedSpace;
    private final Runnable openProtectedSpaceTimeout = () -> {
        if (!waitingForProtectedSpace || isFinishing()) {
            return;
        }
        resetOpenProtectedSpaceButton();
        showOpenProtectedSpaceHelp();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!enforceOfficialSignature()) {
            return;
        }
        policyManager = getSystemService(DevicePolicyManager.class);
        crossProfileApps = getSystemService(CrossProfileApps.class);

        if (isManagedLibertyProfile()) {
            startActivity(new Intent(this, VaultActivity.class));
            finish();
            return;
        }

        if (!LocaleController.hasLanguageSelection(this)) {
            startActivity(new Intent(this, LanguageActivity.class));
            finish();
            return;
        }
        if (!AppPreferences.hasAcceptedCurrentPrivacyPolicy(this)) {
            startActivity(new Intent(this, PrivacyActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        configureWindow(findViewById(R.id.root));
        bindViews();

        chooseAccountButton.setOnClickListener(view -> openAccountPicker());
        createSpaceButton.setOnClickListener(view -> createManagedProfile());
        openProtectedSpaceButton.setOnClickListener(view -> openProtectedSpace());
        removeLibertyButton.setOnClickListener(view -> confirmFullRemoval());
        findViewById(R.id.changeLanguageButton).setOnClickListener(
                view -> startActivity(new Intent(this, LanguageActivity.class))
        );
        findViewById(R.id.openPrivacyButton).setOnClickListener(
                view -> startActivity(new Intent(this, PrivacyActivity.class))
        );
        handleShortcutIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleShortcutIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (setupSteps != null) {
            updateAvailability();
            continuePendingFullUninstall();
        }
    }

    @Override
    protected void onPause() {
        resetOpenProtectedSpaceButton();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        resetOpenProtectedSpaceButton();
        super.onDestroy();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_ACCOUNT) {
            if (resultCode == RESULT_OK && data != null) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                String accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                if (accountName != null
                        && !accountName.trim().isEmpty()
                        && accountType != null
                        && !accountType.trim().isEmpty()) {
                    selectedMigrationAccount = new Account(accountName, accountType);
                    chooseAccountButton.setText(R.string.signin_account_selected);
                }
            }
            return;
        }

        if (requestCode == REQUEST_PROVISION_PROFILE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.provisioning_started, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.provisioning_cancelled, Toast.LENGTH_LONG).show();
            }
            return;
        }

    }

    private void bindViews() {
        heroTitle = findViewById(R.id.heroTitle);
        heroBody = findViewById(R.id.heroBody);
        setupSteps = findViewById(R.id.setupSteps);
        statusCard = findViewById(R.id.statusCard);
        statusTitle = findViewById(R.id.statusTitle);
        statusBody = findViewById(R.id.statusBody);
        createSpaceButton = findViewById(R.id.createSpaceButton);
        chooseAccountButton = findViewById(R.id.chooseAccountButton);
        openProtectedSpaceButton = findViewById(R.id.openProtectedSpaceButton);
        removeLibertyButton = findViewById(R.id.removeLibertyButton);
        dataNote = findViewById(R.id.dataNote);
    }

    private boolean isManagedLibertyProfile() {
        UserManager userManager = getSystemService(UserManager.class);
        if (userManager == null) {
            return false;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return userManager.isManagedProfile();
            }
            return policyManager != null
                    && policyManager.isProfileOwnerApp(getPackageName());
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private void updateAvailability() {
        boolean managedUsersSupported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS);
        boolean provisioningAllowed = policyManager != null
                && policyManager.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
        boolean libertyProfileAvailable = !getLibertyTargetProfiles().isEmpty();

        if (libertyProfileAvailable) {
            heroTitle.setText(R.string.profile_ready_title);
            heroBody.setText(R.string.profile_ready_body);
            setupSteps.setVisibility(View.GONE);
            statusCard.setBackgroundResource(R.drawable.bg_card_success);
            statusCard.setVisibility(View.VISIBLE);
            statusTitle.setText(R.string.profile_open_title);
            statusBody.setText(R.string.profile_open_body);
            openProtectedSpaceButton.setVisibility(View.VISIBLE);
            removeLibertyButton.setVisibility(View.VISIBLE);
            dataNote.setVisibility(View.GONE);
            return;
        }

        heroTitle.setText(R.string.setup_title);
        heroBody.setText(R.string.setup_intro);
        statusCard.setBackgroundResource(R.drawable.bg_card);
        dataNote.setVisibility(View.VISIBLE);

        if (managedUsersSupported && provisioningAllowed) {
            statusCard.setVisibility(View.GONE);
            setupSteps.setVisibility(View.VISIBLE);
            openProtectedSpaceButton.setVisibility(View.GONE);
            removeLibertyButton.setVisibility(View.GONE);
            return;
        }

        setupSteps.setVisibility(View.GONE);
        statusCard.setVisibility(View.VISIBLE);
        statusTitle.setText(R.string.unsupported_title);
        statusBody.setText(R.string.unsupported_body);
        openProtectedSpaceButton.setVisibility(View.GONE);
        removeLibertyButton.setVisibility(View.GONE);
    }

    @SuppressWarnings("deprecation")
    private void createManagedProfile() {
        if (policyManager == null
                || !policyManager.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)) {
            updateAvailability();
            return;
        }

        ComponentName admin = AdminComponents.receiver(this);
        Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, admin);
        String selectedLanguage = LocaleController.getSelectedLanguage(this);
        if (selectedLanguage != null) {
            PersistableBundle adminExtras = new PersistableBundle();
            adminExtras.putString(
                    PolicyComplianceActivity.EXTRA_INITIAL_LANGUAGE,
                    selectedLanguage
            );
            intent.putExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE,
                    adminExtras
            );
        }

        if (selectedMigrationAccount != null) {
            intent.putExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE,
                    selectedMigrationAccount
            );
            intent.putExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_KEEP_ACCOUNT_ON_MIGRATION,
                    true
            );
        }

        if (intent.resolveActivity(getPackageManager()) == null) {
            updateAvailability();
            return;
        }
        startActivityForResult(intent, REQUEST_PROVISION_PROFILE);
    }

    @SuppressWarnings("deprecation")
    private void openAccountPicker() {
        Intent intent = AccountManager.newChooseAccountIntent(
                null,
                null,
                new String[]{"com.google"},
                getString(R.string.choose_signin_account_description),
                null,
                null,
                null
        );
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.account_picker_unavailable, Toast.LENGTH_LONG).show();
            return;
        }
        startActivityForResult(intent, REQUEST_CHOOSE_ACCOUNT);
    }

    private List<UserHandle> getLibertyTargetProfiles() {
        if (crossProfileApps == null) {
            return Collections.emptyList();
        }
        try {
            return crossProfileApps.getTargetUserProfiles();
        } catch (IllegalStateException | SecurityException exception) {
            return Collections.emptyList();
        }
    }

    private void openProtectedSpace() {
        List<UserHandle> targetProfiles = getLibertyTargetProfiles();
        if (targetProfiles.isEmpty()) {
            updateAvailability();
            Toast.makeText(this, R.string.open_protected_space_failed, Toast.LENGTH_LONG).show();
            return;
        }

        waitingForProtectedSpace = true;
        openProtectedSpaceButton.setEnabled(false);
        openProtectedSpaceButton.setText(R.string.opening_protected_space);
        try {
            UserHandle targetProfile = targetProfiles.get(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                crossProfileApps.startMainActivity(
                        new ComponentName(this, MainActivity.class),
                        targetProfile,
                        this,
                        null
                );
            } else {
                crossProfileApps.startMainActivity(
                        new ComponentName(this, MainActivity.class),
                        targetProfile
                );
            }
            openProtectedSpaceButton.postDelayed(openProtectedSpaceTimeout, 1800L);
        } catch (ActivityNotFoundException | IllegalStateException | SecurityException exception) {
            resetOpenProtectedSpaceButton();
            showOpenProtectedSpaceHelp();
        }
    }

    private void resetOpenProtectedSpaceButton() {
        waitingForProtectedSpace = false;
        if (openProtectedSpaceButton == null) {
            return;
        }
        openProtectedSpaceButton.removeCallbacks(openProtectedSpaceTimeout);
        openProtectedSpaceButton.setEnabled(true);
        openProtectedSpaceButton.setText(R.string.open_protected_space);
    }

    private void showOpenProtectedSpaceHelp() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.open_help_title)
                .setMessage(R.string.open_help_body)
                .setNegativeButton(R.string.understood, null)
                .setPositiveButton(R.string.retry, (dialog, which) -> openProtectedSpace())
                .show();
    }

    private void confirmFullRemoval() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_liberty_title)
                .setMessage(R.string.remove_liberty_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.continue_action, (dialog, which) -> beginFullRemoval())
                .show();
    }

    private void beginFullRemoval() {
        if (getLibertyTargetProfiles().isEmpty()) {
            AppPreferences.setFullUninstallPending(this, true);
            continuePendingFullUninstall();
            return;
        }

        openProfileRemovalFallback();
    }

    private void openProfileRemovalFallback() {
        AppPreferences.setFullUninstallPending(this, true);
        Toast.makeText(this, R.string.remove_profile_fallback, Toast.LENGTH_LONG).show();
        openProtectedSpace();
    }

    private void continuePendingFullUninstall() {
        if (!AppPreferences.isFullUninstallPending(this)
                || !getLibertyTargetProfiles().isEmpty()
                || fullUninstallDialogVisible) {
            return;
        }

        fullUninstallDialogVisible = true;
        new AlertDialog.Builder(this)
                .setTitle(R.string.profile_removed_title)
                .setMessage(R.string.profile_removed_body)
                .setCancelable(false)
                .setNegativeButton(R.string.keep_app, (dialog, which) -> {
                    fullUninstallDialogVisible = false;
                    AppPreferences.setFullUninstallPending(this, false);
                })
                .setPositiveButton(R.string.remove_app_now, (dialog, which) -> {
                    fullUninstallDialogVisible = false;
                    AppPreferences.setFullUninstallPending(this, false);
                    requestSelfUninstall();
                })
                .show();
    }

    private void requestSelfUninstall() {
        Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + getPackageName()));
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.uninstall_not_available, Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(intent);
    }

    private void handleShortcutIntent(Intent intent) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_OPEN_PROTECTED_SPACE, false)) {
            return;
        }
        intent.removeExtra(EXTRA_OPEN_PROTECTED_SPACE);
        openProtectedSpace();
    }

}
