package com.nine.lsp;

import android.content.Context;

public class ConfigManager {
    private static final String PREFS_NAME = "module_config";
    private static final String KEY_ENABLED = "is_enabled";

    public static boolean isModuleEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, true);
    }

    public static void setModuleEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ENABLED, enabled)
                .apply();
    }
}