package net.crinoidea.liberty.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

import net.crinoidea.liberty.R;
import net.crinoidea.liberty.security.SignatureVerifier;
import net.crinoidea.liberty.settings.LocaleController;

abstract class BaseActivity extends Activity {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleController.wrap(base));
    }

    protected boolean enforceReleaseSignature() {
        if (SignatureVerifier.isTrusted(this)) {
            return true;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.signature_error_title)
                .setMessage(R.string.signature_error_body)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                .show();
        return false;
    }

    protected void configureWindow(View root) {
        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.asphalt_950));
        window.setNavigationBarColor(getColor(R.color.asphalt_950));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            int initialLeft = root.getPaddingLeft();
            int initialTop = root.getPaddingTop();
            int initialRight = root.getPaddingRight();
            int initialBottom = root.getPaddingBottom();
            root.setOnApplyWindowInsetsListener((view, windowInsets) -> {
                Insets bars = windowInsets.getInsets(WindowInsets.Type.systemBars());
                view.setPadding(
                        initialLeft + bars.left,
                        initialTop + bars.top,
                        initialRight + bars.right,
                        initialBottom + bars.bottom
                );
                return windowInsets;
            });
            root.requestApplyInsets();
        }
    }
}
