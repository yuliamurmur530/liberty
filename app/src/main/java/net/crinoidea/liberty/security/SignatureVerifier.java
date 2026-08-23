package net.crinoidea.liberty.security;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;

import net.crinoidea.liberty.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SignatureVerifier {
    private static final String EXPECTED_PACKAGE = "net.crinoidea.liberty";
    private static final byte[] EXPECTED_CERTIFICATE_SHA256 = fromHex(
            "3c10f7cc83e0868226cef3699ebca9e982730dd298dfcd8f81155ad8f66a96c7"
    );

    private SignatureVerifier() {
    }

    public static boolean isTrusted(Context context) {
        if (!context.getResources().getBoolean(R.bool.enforce_release_signature)) {
            return true;
        }
        if (!EXPECTED_PACKAGE.equals(context.getPackageName())) {
            return false;
        }

        try {
            @SuppressWarnings("deprecation")
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_SIGNING_CERTIFICATES
            );
            SigningInfo signingInfo = packageInfo.signingInfo;
            if (signingInfo == null) {
                return false;
            }

            Signature[] signatures = signingInfo.hasMultipleSigners()
                    ? signingInfo.getApkContentsSigners()
                    : signingInfo.getSigningCertificateHistory();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Signature signature : signatures) {
                if (MessageDigest.isEqual(
                        EXPECTED_CERTIFICATE_SHA256,
                        digest.digest(signature.toByteArray())
                )) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException exception) {
            return false;
        }
        return false;
    }

    private static byte[] fromHex(String value) {
        byte[] result = new byte[value.length() / 2];
        for (int index = 0; index < result.length; index++) {
            int offset = index * 2;
            result[index] = (byte) Integer.parseInt(value.substring(offset, offset + 2), 16);
        }
        return result;
    }
}
