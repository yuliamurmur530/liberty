package net.crinoidea.liberty.ui;

import android.app.admin.DevicePolicyManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.crinoidea.liberty.R;
import net.crinoidea.liberty.admin.AdminComponents;
import net.crinoidea.liberty.admin.SecurityPolicies;
import net.crinoidea.liberty.model.AppCatalog;
import net.crinoidea.liberty.model.AppEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class VaultActivity extends BaseActivity {
    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private ProtectedAppsAdapter adapter;
    private DevicePolicyManager policyManager;
    private ComponentName admin;
    private View challengeCard;
    private TextView challengeTitle;
    private TextView challengeBody;
    private Button setCodeButton;
    private View securityCard;
    private ImageView securityStatusIcon;
    private TextView securityStatusTitle;
    private TextView securitySummary;
    private TextView networkStatusTitle;
    private TextView networkStatusBody;
    private Button addAppsButton;
    private boolean protectionConfirmed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!enforceOfficialSignature()) {
            return;
        }
        policyManager = getSystemService(DevicePolicyManager.class);
        admin = AdminComponents.receiver(this);
        adapter = new ProtectedAppsAdapter();

        setContentView(R.layout.activity_vault);
        configureWindow(findViewById(R.id.root));

        challengeCard = findViewById(R.id.challengeCard);
        challengeTitle = findViewById(R.id.challengeTitle);
        challengeBody = findViewById(R.id.challengeBody);
        setCodeButton = findViewById(R.id.setCodeButton);
        securityCard = findViewById(R.id.securityCard);
        securityStatusIcon = findViewById(R.id.securityStatusIcon);
        securityStatusTitle = findViewById(R.id.securityStatusTitle);
        securitySummary = findViewById(R.id.securitySummary);
        networkStatusTitle = findViewById(R.id.networkStatusTitle);
        networkStatusBody = findViewById(R.id.networkStatusBody);
        addAppsButton = findViewById(R.id.addAppsButton);

        GridView appsList = findViewById(R.id.appsList);
        TextView emptyView = findViewById(R.id.emptyView);
        appsList.setAdapter(adapter);
        appsList.setEmptyView(emptyView);
        appsList.setOnItemClickListener((parent, view, position, id) -> launch(adapter.getItem(position)));

        addAppsButton.setOnClickListener(view -> showAddAppsHelp());
        findViewById(R.id.changeLanguageButton).setOnClickListener(
                view -> startActivity(new Intent(this, LanguageActivity.class))
        );
        findViewById(R.id.lockButton).setOnClickListener(view -> confirmEmergencyLock());
        findViewById(R.id.removeProfileButton).setOnClickListener(
                view -> startActivity(new Intent(this, ProfileRemovalActivity.class))
        );
        setCodeButton.setOnClickListener(view -> requestSeparateChallenge());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProtectionStatus();
        showChallengeSetup();
        updateNetworkStatus();
        try {
            loadApps();
        } catch (RuntimeException ignored) {
            // Интерфейс и удаление пространства должны оставаться доступны даже при ошибке
            // каталога приложений на нестандартной прошивке.
        }
    }

    @Override
    protected void onDestroy() {
        loader.shutdownNow();
        super.onDestroy();
    }

    private void loadApps() {
        loader.execute(() -> {
            List<AppEntry> apps = AppCatalog.loadLaunchableApps(this, false);
            apps.removeIf(entry -> "com.android.vending".equals(entry.packageName()));
            runOnUiThread(() -> adapter.replace(apps));
        });
    }

    private void showChallengeSetup() {
        challengeCard.setVisibility(View.VISIBLE);
        if (hasSeparateProfileChallenge()) {
            challengeCard.setBackgroundResource(R.drawable.bg_card_success);
            challengeTitle.setText(R.string.separate_code_ready);
            challengeBody.setText(R.string.separate_code_ready_body);
            setCodeButton.setVisibility(View.GONE);
            return;
        }

        challengeCard.setBackgroundResource(R.drawable.bg_card);
        challengeTitle.setText(R.string.separate_code_title);
        challengeBody.setText(R.string.separate_code_body);
        setCodeButton.setVisibility(View.VISIBLE);
    }

    private boolean hasSeparateProfileChallenge() {
        if (policyManager == null
                || !policyManager.isProfileOwnerApp(getPackageName())) {
            return false;
        }
        try {
            return !policyManager.isUsingUnifiedPassword(admin);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException exception) {
            return false;
        }
    }

    private void requestSeparateChallenge() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void showAddAppsHelp() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_apps_help_title)
                .setMessage(R.string.add_apps_help_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.open_protected_store, (dialog, which) -> openAppStore())
                .show();
    }

    private void openAppStore() {
        Intent protectedPlay = getPackageManager().getLaunchIntentForPackage("com.android.vending");
        if (protectedPlay != null) {
            protectedPlay.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(protectedPlay);
            return;
        }

        // Некоторые Android-сборки скрывают launcher-activity магазина от PackageManager.
        // В запасном Intent запрос обязательно непустой: пустой market://search?q=
        // ошибочно показывается отдельными версиями Google Play как отсутствие сети.
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=apps&c=apps"));
        if (market.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.store_not_found, Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(market);
    }

    private void launch(AppEntry entry) {
        if (!protectionConfirmed) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.protection_launch_blocked_title)
                    .setMessage(R.string.protection_launch_blocked_body)
                    .setPositiveButton(R.string.understood, null)
                    .show();
            return;
        }
        if (inspectNetwork() != NetworkCheck.DIRECT) {
            updateNetworkStatus();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.network_launch_blocked_title)
                    .setMessage(R.string.network_launch_blocked_body)
                    .setNegativeButton(R.string.understood, null)
                    .setPositiveButton(R.string.network_check_again, (dialog, which) -> updateNetworkStatus())
                    .show();
            return;
        }
        Intent intent = getPackageManager().getLaunchIntentForPackage(entry.packageName());
        if (intent == null) {
            Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }

    private void updateProtectionStatus() {
        protectionConfirmed = SecurityPolicies.applyAndVerify(this);
        if (protectionConfirmed) {
            securityCard.setBackgroundResource(R.drawable.bg_card_success);
            securityStatusIcon.setVisibility(View.VISIBLE);
            securityStatusTitle.setText(R.string.security_active);
            securityStatusTitle.setTextColor(getColor(R.color.success));
            securitySummary.setText(R.string.security_summary);
            return;
        }

        securityCard.setBackgroundResource(R.drawable.bg_card);
        securityStatusIcon.setVisibility(View.GONE);
        securityStatusTitle.setText(R.string.security_attention);
        securityStatusTitle.setTextColor(getColor(R.color.liberty_gold));
        securitySummary.setText(R.string.security_attention_summary);
    }

    private void updateNetworkStatus() {
        NetworkCheck check = inspectNetwork();
        if (check == NetworkCheck.DIRECT) {
            networkStatusTitle.setText(R.string.network_direct_title);
            networkStatusTitle.setTextColor(getColor(R.color.success));
            networkStatusBody.setText(R.string.network_direct_body);
            return;
        }
        networkStatusTitle.setTextColor(getColor(R.color.liberty_gold));
        if (check == NetworkCheck.VPN_OR_PROXY) {
            networkStatusTitle.setText(R.string.network_attention_title);
            networkStatusBody.setText(R.string.network_attention_body);
        } else {
            networkStatusTitle.setText(R.string.network_unknown_title);
            networkStatusBody.setText(R.string.network_unknown_body);
        }
    }

    private NetworkCheck inspectNetwork() {
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        if (connectivityManager == null) {
            return NetworkCheck.UNKNOWN;
        }
        try {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) {
                return NetworkCheck.UNKNOWN;
            }
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            LinkProperties linkProperties = connectivityManager.getLinkProperties(activeNetwork);
            if (capabilities == null) {
                return NetworkCheck.UNKNOWN;
            }
            boolean vpn = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
            boolean httpProxy = linkProperties != null && linkProperties.getHttpProxy() != null;
            return vpn || httpProxy ? NetworkCheck.VPN_OR_PROXY : NetworkCheck.DIRECT;
        } catch (RuntimeException exception) {
            return NetworkCheck.UNKNOWN;
        }
    }

    private void lockProfile() {
        if (policyManager == null) {
            Toast.makeText(this, R.string.lock_failed, Toast.LENGTH_LONG).show();
            return;
        }
        try {
            policyManager.lockNow(DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException exception) {
            try {
                policyManager.lockNow();
            } catch (IllegalArgumentException
                     | IllegalStateException
                     | SecurityException fallbackException) {
                Toast.makeText(this, R.string.lock_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void confirmEmergencyLock() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.emergency_lock_title)
                .setMessage(R.string.emergency_lock_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.emergency_lock_confirm, (dialog, which) -> lockProfile())
                .show();
    }

    private final class ProtectedAppsAdapter extends BaseAdapter {
        private final List<AppEntry> apps = new ArrayList<>();
        private final LruCache<String, Drawable> iconCache = new LruCache<>(24);
        private final LayoutInflater inflater = LayoutInflater.from(VaultActivity.this);

        void replace(List<AppEntry> replacement) {
            apps.clear();
            apps.addAll(replacement);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return apps.size();
        }

        @Override
        public AppEntry getItem(int position) {
            return apps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).packageName().hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_app_launch, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppEntry entry = getItem(position);
            holder.label.setText(entry.label());
            Drawable icon = iconCache.get(entry.packageName());
            if (icon == null) {
                icon = entry.resolveInfo().loadIcon(getPackageManager());
                if (icon != null) {
                    iconCache.put(entry.packageName(), icon);
                }
            }
            holder.icon.setImageDrawable(icon);
            return convertView;
        }
    }

    private static final class ViewHolder {
        final ImageView icon;
        final TextView label;

        ViewHolder(View root) {
            icon = root.findViewById(R.id.appIcon);
            label = root.findViewById(R.id.appLabel);
        }
    }

    private enum NetworkCheck {
        DIRECT,
        VPN_OR_PROXY,
        UNKNOWN
    }
}
