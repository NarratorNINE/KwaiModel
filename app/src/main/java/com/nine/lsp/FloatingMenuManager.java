package com.nine.lsp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FloatingMenuManager {

    private static View menuView;
    private static boolean isMenuVisible = false;
    private static int lastBallX, lastBallY;
    private static WindowManager.LayoutParams menuParams;

    public void toggleMenu(Context context, WindowManager wm, View ballView) {
        if (isMenuVisible) {
            hideMenu(wm);
            return;
        }

        if (menuView != null) return;

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackground(Utils.getRoundedBackground(Color.argb(200, 30, 30, 30), 16));
        layout.setElevation(10);
        layout.setAlpha(0f);
        layout.setScaleX(0.8f);
        layout.setScaleY(0.8f);

        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                Utils.dpToPx(context, 140)
        ));
        scrollView.setBackgroundColor(Color.parseColor("#222222"));

        TextView logTextView = new TextView(context);
        LogManager.init(context);
        LogManager.registerLogView(logTextView);
        scrollView.addView(logTextView);
        layout.addView(scrollView);

        LinearLayout buttons = new LinearLayout(context);
        buttons.setOrientation(LinearLayout.VERTICAL);
        buttons.setPadding(0, 20, 0, 0);

        Button toggle = new Button(context);
        toggle.setText("防撤回：" + (FeatureToggleManager.isAntiRecallEnabled() ? "开启" : "关闭"));
        toggle.setOnClickListener(v -> FeatureToggleManager.toggleAntiRecall(toggle));

        Button clear = new Button(context);
        clear.setText("清空日志");
        clear.setOnClickListener(v -> LogManager.clearLogs());

        Button close = new Button(context);
        close.setText("关闭菜单");
        close.setOnClickListener(v -> hideMenu(wm));


        buttons.addView(toggle);
        buttons.addView(clear);
        buttons.addView(close);
        layout.addView(buttons);

        int[] loc = new int[2];
        ballView.getLocationOnScreen(loc);
        lastBallX = loc[0];
        lastBallY = loc[1];

        WindowManager.LayoutParams menuParams = new WindowManager.LayoutParams();
        menuParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        menuParams.format = PixelFormat.TRANSLUCENT;
        menuParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        menuParams.width = Utils.dpToPx(context, 240);
        menuParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        menuParams.gravity = Gravity.TOP | Gravity.START;
        menuParams.x = lastBallX;
        menuParams.y = lastBallY - 96;  // 修正位置

        try {
            wm.addView(layout, menuParams);
            layout.post(() -> {
                int[] menuLoc = new int[2];
                layout.getLocationOnScreen(menuLoc);
                layout.setPivotX(lastBallX + ballView.getWidth() / 2f - menuLoc[0]);
                layout.setPivotY(lastBallY + ballView.getHeight() / 2f - menuLoc[1]);
                layout.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator())
                        .withEndAction(() -> isMenuVisible = true)
                        .start();
            });
            menuView = layout;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateMenuPosition(int ballX, int ballY) {
        lastBallX = ballX;
        lastBallY = ballY;

        if (menuView != null && menuParams != null) {
            menuParams.x = ballX;
            menuParams.y = ballY; // 保持菜单在球上方
            try {
                WindowManager wm = (WindowManager) menuView.getContext().getSystemService(Context.WINDOW_SERVICE);
                wm.updateViewLayout(menuView, menuParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static void hideMenu(WindowManager wm) {
        if (menuView == null || !isMenuVisible) return;

        menuView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        menuView.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    try {
                        menuView.setVisibility(View.GONE);
                        wm.removeView(menuView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    menuView.setLayerType(View.LAYER_TYPE_NONE, null);
                    menuView = null;
                    isMenuVisible = false;
                })
                .start();
    }
}