package com.nine.lsp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class FloatingBallManager {

    private static View floatingBallView;
    private static WindowManager windowManager;
    private static WindowManager.LayoutParams params;

    public static void showFloating(Context context) {
        if (floatingBallView != null) return;

        Context appContext = context.getApplicationContext();
        windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 200;
        params.y = 500;

        ImageView ball = new ImageView(appContext);
        ball.setImageResource(android.R.drawable.presence_online);
        ball.setBackground(Utils.createRoundRectDrawable(Color.argb(180, 30, 30, 30), 50));
        ball.setPadding(30, 30, 30, 30);

        ball.setOnTouchListener(new View.OnTouchListener() {
            int lastX, lastY, paramX, paramY;
            long downTime;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downTime = System.currentTimeMillis();
                        lastX = (int) e.getRawX();
                        lastY = (int) e.getRawY();
                        paramX = params.x;
                        paramY = params.y;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) e.getRawX() - lastX;
                        int dy = (int) e.getRawY() - lastY;
                        params.x = paramX + dx;
                        params.y = paramY + dy;
                        windowManager.updateViewLayout(v, params);
                        FloatingMenuManager.updateMenuPosition(params.x, params.y);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - downTime < 150) {
                            FloatingMenuManager manager = new FloatingMenuManager(); // 创建实例
                            manager.toggleMenu(context, windowManager, v); // 调用实例方法
                        }
                        return true;
                }
                return false;
            }
        });

        floatingBallView = ball;
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                windowManager.addView(floatingBallView, params);
            } catch (Exception ignored) {}
        });
    }

    public static View getFloatingBallView() {
        return floatingBallView;
    }
}
