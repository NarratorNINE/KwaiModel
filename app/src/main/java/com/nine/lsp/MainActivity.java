package com.nine.lsp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity {

    // 视图声明
    private com.google.android.material.materialswitch.MaterialSwitch switchEnableModule;
    private MaterialButton btnSettings;
    private MaterialButton fabClear;
    private MaterialButton fabAbout;
    private TextView tvLog;
    private MaterialToolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogManager.init(this);

        // 初始化视图
        initViews();
        setupToolbar();
        setupLogSystem();
        setupSwitch();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        switchEnableModule = findViewById(R.id.switch_enable_module);
        btnSettings = findViewById(R.id.btn_settings);
        fabClear = findViewById(R.id.btn_clear);
        fabAbout = findViewById(R.id.btn_about);
        tvLog = findViewById(R.id.tv_log);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_settings) {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupLogSystem() {
        LogManager.registerLogView(tvLog);
        scrollToBottom();
        LogManager.log("系统初始化完成");
    }

    private void setupSwitch() {
        switchEnableModule.setChecked(ConfigManager.isModuleEnabled(this));
        switchEnableModule.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConfigManager.setModuleEnabled(this, isChecked);
            LogManager.log("模块状态: " + (isChecked ? "启用" : "禁用"));
        });
    }

    private void setupButtons() {
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        fabClear.setOnClickListener(v -> {
            LogManager.clearLogs();
        });

        fabAbout.setOnClickListener(v -> showAboutDialog());
    }


    private void showAboutDialog() {
        try {
            new MaterialAlertDialogBuilder(this) // 移除 R.style.ThemeOverlay_MyApp_Dialog_Center
                    .setTitle(R.string.about_title)
                    .setMessage(R.string.about_message)
                    .setPositiveButton("确定", null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            // 备用方案
            new android.app.AlertDialog.Builder(this)
                    .setTitle("关于")
                    .setMessage("应用版本信息")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }

    private void scrollToBottom() {
        tvLog.post(() -> ((ScrollView) tvLog.getParent()).fullScroll(View.FOCUS_DOWN));
    }

    @Override
    protected void onDestroy() {
        LogManager.unregisterLogView(tvLog);
        super.onDestroy();
    }
}

/* 原来的代码
public class MainActivity extends AppCompatActivity {

    private SwitchMaterial switchEnableModule;
    private MaterialButton btnSettings;
    private MaterialButton btnClearLog;
    private MaterialButton btnAbout;
    private TextView tvStatus;
    private TextView tvLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化控件
        switchEnableModule = findViewById(R.id.switch_enable_module);
        btnSettings = findViewById(R.id.btn_settings);
        btnClearLog = findViewById(R.id.btn_clear);
        btnAbout = findViewById(R.id.btn_about);
        //tvStatus = findViewById(R.id.tv_status);
        tvLog = findViewById(R.id.tv_log);

        // 注册并绑定日志视图
        if (tvLog != null) {
            LogManager.init(this);
            LogManager.registerLogView(tvLog);
            scrollToBottom();
        }

        LogManager.log("应用启动");



        // 模块开关监听
        if (switchEnableModule != null) {
            switchEnableModule.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "模块已启用" : "模块已禁用";
                if (tvStatus != null) {
                    tvStatus.setText(status);
                }
                LogManager.log("模块状态切换：" + (isChecked ? "启用" : "禁用"));
            });
        }
        // 设置按钮点击
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                LogManager.log("点击了高级设置按钮");
            });
        }

        // 清空日志
        if (btnClearLog != null) {
            btnClearLog.setOnClickListener(v -> {
                LogManager.clearLogs();
            });
        }

        // 关于我们
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this, R.style.CustomDialog)
                        .setTitle("关于")
                        .setMessage("作者：NINE\n版本：1.0\n感谢使用本模块！")
                        .setPositiveButton("确定", null)
                        .show();
            });
        }
    }

    private void scrollToBottom() {
        if (tvLog == null) return;

        ViewParent parent = tvLog.getParent();
        if (parent instanceof ScrollView) {
            ((ScrollView) parent).post(() -> {
                ((ScrollView) parent).fullScroll(View.FOCUS_DOWN);
            });
        }
    }
}

 */
