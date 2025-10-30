package com.nine.lsp;

import android.widget.Button;

public class FeatureToggleManager {

    private static boolean antiRecallEnabled = true;

    public static boolean isAntiRecallEnabled() {
        return antiRecallEnabled;
    }

    public static void toggleAntiRecall(Button toggle) {
        antiRecallEnabled = !antiRecallEnabled;
        toggle.setText("防撤回：" + (antiRecallEnabled ? "开启" : "关闭"));
        LogManager.log("防撤回状态：" + (antiRecallEnabled ? "开启" : "关闭"));
    }

}

