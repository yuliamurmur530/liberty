package net.crinoidea.liberty.admin;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.crinoidea.liberty.security.SignatureVerifier;

public final class ProfileMaintenanceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())
                || !SignatureVerifier.isTrusted(context)) {
            return;
        }

        DevicePolicyManager policyManager = context.getSystemService(DevicePolicyManager.class);
        if (policyManager != null && policyManager.isProfileOwnerApp(context.getPackageName())) {
            SecurityPolicies.apply(context);
        }
    }
}
