package com.nine.lsp;

import android.content.Context;

public class FloatingWindowController {

    private static boolean isInitialized = false;

    public static void initialize(Context context) {
        if (isInitialized) return;
        FloatingBallManager.showFloating(context);
        isInitialized = true;
    }

    public static void log(String message) {
        LogManager.log(message);
    }

}

