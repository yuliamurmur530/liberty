package net.crinoidea.liberty.admin;

import android.content.ComponentName;
import android.content.Context;

public final class AdminComponents {
    private AdminComponents() {
    }

    public static ComponentName receiver(Context context) {
        return new ComponentName(context, LibertyAdminReceiver.class);
    }
}
