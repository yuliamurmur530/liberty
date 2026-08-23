package net.crinoidea.liberty.admin;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import net.crinoidea.liberty.security.SignatureVerifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LibertyAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        if (!SignatureVerifier.isTrusted(context)) {
            return;
        }
        PendingResult pendingResult = goAsync();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                finishProvisioning(context, intent);
            } finally {
                pendingResult.finish();
                executor.shutdown();
            }
        });
    }

    private void finishProvisioning(Context context, Intent intent) {
        DevicePolicyManager policyManager = context.getSystemService(DevicePolicyManager.class);
        ComponentName admin = AdminComponents.receiver(context);
        if (policyManager == null || !policyManager.isProfileOwnerApp(context.getPackageName())) {
            return;
        }

        SecurityPolicies.apply(context);
        try {
            policyManager.setProfileEnabled(admin);
        } catch (IllegalStateException | SecurityException ignored) {
            // Системный мастер некоторых прошивок включает профиль самостоятельно.
        }
    }

}
