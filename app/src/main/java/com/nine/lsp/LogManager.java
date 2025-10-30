package com.nine.lsp;

import android.content.*;
import android.view.View;
import android.view.ViewParent;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogManager {

    private static final String LOG_KEY = "FLOAT_LOG";
    private static final String PREFS_NAME = "float_config";
    private static final String ACTION_LOG_UPDATE = "com.nine.lsp.LOG_UPDATE";

    private static StringBuilder logCache = new StringBuilder();
    private static SharedPreferences prefs;
    private static Context appContext;

    private static TextView boundTextView;
    private static BroadcastReceiver logReceiver;
    private static boolean registered = false;

    /**
     * 初始化日志系统（任一进程中仅需调用一次）
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(LOG_KEY, "");
        logCache = new StringBuilder(saved);
    }

    /**
     * 写入日志，同时广播同步到其他 UI
     */
    public static void log(String message) {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String fullMsg = "[" + time + "] " + message + "\n";

        logCache.append(fullMsg);

        // 保存到 SharedPreferences
        if (prefs != null) {
            prefs.edit().putString(LOG_KEY, logCache.toString()).apply();
        }

        // 发送广播
        if (appContext != null) {
            Intent intent = new Intent(ACTION_LOG_UPDATE);
            intent.putExtra("log_msg", fullMsg);
            appContext.sendBroadcast(intent);
        }
    }

    /**
     * 清空日志并广播通知 UI
     */
    public static void clearLogs() {
        logCache.setLength(0);
        if (prefs != null) prefs.edit().remove(LOG_KEY).apply();

        if (appContext != null) {
            Intent intent = new Intent(ACTION_LOG_UPDATE);
            intent.putExtra("log_clear", true);
            appContext.sendBroadcast(intent);
        }
    }

    /**
     * 将日志绑定到指定 TextView（用于悬浮窗或 Activity）
     */
    public static void registerLogView(TextView textView) {
        boundTextView = textView;
        if (boundTextView != null && prefs != null) {
            boundTextView.setText(prefs.getString(LOG_KEY, ""));
        }

        if (!registered && appContext != null) {
            logReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (!ACTION_LOG_UPDATE.equals(intent.getAction())) return;

                    if (prefs == null) {
                        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    }

                    if (intent.getBooleanExtra("log_clear", false)) {
                        if (boundTextView != null) boundTextView.setText("");
                        if (prefs != null) prefs.edit().remove(LOG_KEY).apply(); // ✅ 写入自己进程的 SharedPreferences
                    } else {
                        String msg = intent.getStringExtra("log_msg");
                        if (msg != null) {
                            if (boundTextView != null) {
                                boundTextView.append(msg);
                                ViewParent parent = boundTextView.getParent();
                                if (parent instanceof ScrollView) {
                                    ((ScrollView) parent).post(() ->
                                            ((ScrollView) parent).fullScroll(View.FOCUS_DOWN));
                                }
                            }

                            // ✅ 将接收到的日志也写入本进程 SharedPreferences
                            if (prefs != null) {
                                String existing = prefs.getString(LOG_KEY, "");
                                prefs.edit().putString(LOG_KEY, existing + msg).apply();
                            }
                        }
                    }
                }

            };

            IntentFilter filter = new IntentFilter(ACTION_LOG_UPDATE);
            appContext.registerReceiver(logReceiver, filter, Context.RECEIVER_EXPORTED);
            registered = true;
        }
    }
    public static void unregisterLogView(TextView view) {
        if (boundTextView == view) {
            boundTextView = null;
        }
    }
}
