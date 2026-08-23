package net.crinoidea.liberty.admin;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserManager;

import net.crinoidea.liberty.R;

import java.util.Collections;
import java.util.List;

public final class SecurityPolicies {
    private SecurityPolicies() {
    }

    public static void apply(Context context) {
        DevicePolicyManager policyManager = context.getSystemService(DevicePolicyManager.class);
        ComponentName admin = AdminComponents.receiver(context);
        if (policyManager == null || !policyManager.isProfileOwnerApp(context.getPackageName())) {
            return;
        }

        applySafely(() -> policyManager.setOrganizationName(
                admin,
                context.getString(R.string.organization_name)
        ));
        applySafely(() -> policyManager.setProfileName(
                admin,
                context.getString(R.string.app_name)
        ));
        applySafely(() -> policyManager.addUserRestriction(
                admin,
                UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE
        ));
        // VPN личного профиля не распространяется на рабочий профиль. Дополнительно запрещаем
        // настраивать и запускать отдельный VPN внутри защищённого профиля Liberty.
        applySafely(() -> policyManager.addUserRestriction(
                admin,
                UserManager.DISALLOW_CONFIG_VPN
        ));
        // В защищённом профиле Liberty не должно быть собственного always-on VPN.
        // Настройки VPN личного профиля этим вызовом не затрагиваются.
        applySafely(() -> {
            try {
                policyManager.setAlwaysOnVpnPackage(admin, null, false);
            } catch (PackageManager.NameNotFoundException ignored) {
                // При очистке пакета нет; отдельные прошивки всё равно объявляют исключение.
            }
        });
        applySafely(() -> policyManager.setCrossProfileCallerIdDisabled(admin, true));
        applySafely(() -> policyManager.setCrossProfileContactsSearchDisabled(admin, true));
        applySafely(() -> policyManager.setPermittedCrossProfileNotificationListeners(
                admin,
                Collections.emptyList()
        ));
        applySafely(() -> policyManager.setPermissionPolicy(
                admin,
                DevicePolicyManager.PERMISSION_POLICY_PROMPT
        ));

        // Удаляем фильтр передачи списка приложений, который использовался в ранних версиях.
        applySafely(() -> policyManager.clearCrossProfileIntentFilters(admin));

        // Пользователь должен иметь возможность сохранять чеки. Liberty не блокирует снимки экрана.
        applySafely(() -> policyManager.setScreenCaptureDisabled(admin, false));
    }

    public static boolean applyAndVerify(Context context) {
        apply(context);
        return isProtectionConfirmed(context);
    }

    public static boolean isProtectionConfirmed(Context context) {
        DevicePolicyManager policyManager = context.getSystemService(DevicePolicyManager.class);
        UserManager userManager = context.getSystemService(UserManager.class);
        ComponentName admin = AdminComponents.receiver(context);
        if (policyManager == null
                || userManager == null
                || !policyManager.isProfileOwnerApp(context.getPackageName())) {
            return false;
        }

        try {
            List<String> notificationListeners =
                    policyManager.getPermittedCrossProfileNotificationListeners(admin);
            return userManager.hasUserRestriction(
                    UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE
            )
                    && userManager.hasUserRestriction(UserManager.DISALLOW_CONFIG_VPN)
                    && policyManager.getAlwaysOnVpnPackage(admin) == null
                    && policyManager.getCrossProfileCallerIdDisabled(admin)
                    && policyManager.getCrossProfileContactsSearchDisabled(admin)
                    && notificationListeners != null
                    && notificationListeners.isEmpty()
                    && policyManager.getPermissionPolicy(admin)
                    == DevicePolicyManager.PERMISSION_POLICY_PROMPT;
        } catch (IllegalArgumentException
                 | IllegalStateException
                 | SecurityException
                 | UnsupportedOperationException exception) {
            return false;
        }
    }

    private static void applySafely(Runnable policy) {
        try {
            policy.run();
        } catch (IllegalArgumentException
                 | IllegalStateException
                 | SecurityException
                 | UnsupportedOperationException ignored) {
            // Производители Android поддерживают разные наборы политик. Отказ одной политики
            // не должен прерывать создание или запуск защищённого пространства.
        }
    }
}
