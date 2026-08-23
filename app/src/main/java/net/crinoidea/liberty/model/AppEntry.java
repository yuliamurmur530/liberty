package net.crinoidea.liberty.model;

import android.content.pm.ResolveInfo;

public final class AppEntry {
    private final String label;
    private final String packageName;
    private final ResolveInfo resolveInfo;

    public AppEntry(String label, String packageName, ResolveInfo resolveInfo) {
        this.label = label;
        this.packageName = packageName;
        this.resolveInfo = resolveInfo;
    }

    public String label() {
        return label;
    }

    public String packageName() {
        return packageName;
    }

    public ResolveInfo resolveInfo() {
        return resolveInfo;
    }
}
