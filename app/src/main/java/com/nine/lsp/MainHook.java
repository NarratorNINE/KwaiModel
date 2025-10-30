package com.nine.lsp;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

//目前效果最好的一个
public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "KwaiModel：";
    private static final String TARGET_PACKAGE = "com.kuaishou.nebula";
    private static final String TARGET_CLASS = "com.kwai.imsdk.msg.KwaiMsg";
    private static final String TARGET_METHOD = "getContentBytes";
    private static final String KWAI_MSG_CLASS = "com.kwai.imsdk.msg.KwaiMsg";

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals(TARGET_PACKAGE) || !lpparam.processName.equals(TARGET_PACKAGE)) {
            return; // 忽略非主进程
        }

        XposedHelpers.findAndHookMethod(
                Application.class, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Context context = (Context) param.thisObject;
                        try {
                            FloatingWindowController.initialize(context);
                            XposedBridge.log(TAG + "尝试显示悬浮窗");
                        } catch (Exception e) {
                            XposedBridge.log(TAG + ": 显示悬浮窗错误 - " + e.getMessage());
                        };
                        new Handler(Looper.getMainLooper()).post(() ->
                                //确保在主线程显示
                                Utils.showOnce(context, TAG + "悬浮球开启成功")
                        );


                    }
                }
        );

        ClassLoader cl = lpparam.classLoader;

        try {

            XposedHelpers.findAndHookMethod(
                    KWAI_MSG_CLASS,
                    cl,
                    "setMsgType",
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {
                                int newType = (int) param.args[0];
                                Object msgObj = param.thisObject;

                                // 使用getSeqId替代msgId
                                long seqId = (long) XposedHelpers.callMethod(msgObj, "getSeqId");
                                int currentType = (int) XposedHelpers.callMethod(msgObj, "getMsgType");
                                String sender = safeGetString(msgObj, "getSender");
                                long fiveSeq = seqId % 100000; // 取最后 5 位防止日志过长


                                FloatingWindowController.log(String.format(
                                                "\n├ 消息Id: %d" +
                                                "\n├ 当前类型: %d" +
                                                "\n├ 新类型: %d" +
                                                "\n└ 发送者: %s",
                                        fiveSeq, currentType, newType, sender
                                ));
                                XposedBridge.log(TAG + String.format(
                                        "\n[setMsgType]" +
                                                "\n├ 消息Id: %d" +
                                                "\n├ 当前类型: %d" +
                                                "\n└ 新类型: %d",
                                        seqId, currentType, newType
                                ));

                                // 拦截撤回消息(type=11)
                                if (newType == 11) {
                                    param.args[0] = currentType; // 保持原类型
                                    XposedBridge.log(TAG + " ▶ 已阻止消息类型设为11");
                                }

                            } catch (Throwable e) {
                                XposedBridge.log(TAG + " ▶ setMsgType错误: " + e.getMessage());
                            }
                        }
                    }
            );

            /* Hook getContentBytes 方法打印字节内容，按需使用
            XposedHelpers.findAndHookMethod(
                    TARGET_CLASS,
                    lpparam.classLoader,
                    TARGET_METHOD,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                byte[] contentBytes = (byte[]) param.getResult();
                                if (contentBytes == null) {
                                    XposedBridge.log(TAG + ": 内容字节数组为 null");
                                    return;
                                }

                                Object msgObj = param.thisObject;
                                Long seqId = safeGetLong(msgObj, "getSeqId");
                                Integer msgType = safeGetInt(msgObj, "getMsgType");
                                String sender = safeGetString(msgObj, "getSender");

                                String decodedContent = safeDecodeUTF8(contentBytes);
                                String hexContent = toHex(contentBytes);

                                StringBuilder log = new StringBuilder();
                                log.append("════════════════════════════════════════════\n");
                                //log.append("原始 HEX 内容：\n").append(hexContent).append("\n");
                                log.append("解码内容：\n").append(decodedContent != null ? decodedContent : "[解码失败]").append("\n");
                                log.append("┌ 序列号: ").append(seqId != null ? seqId : "null").append("\n");
                                log.append("├ 类型: ").append(msgType != null ? msgType : "null").append("\n");
                                log.append("└ 发送者: ").append(sender != null ? sender : "null").append("\n");
                                log.append("════════════════════════════════════════════");

                                XposedBridge.log(TAG + ":\n" + log.toString());
                            } catch (Exception e) {
                                XposedBridge.log(TAG + ": 处理错误 - " + e.getMessage());
                            }
                        }
                    }
            );
             */

            // Hook isReplaceMsg 方法，强制返回 false，阻止撤回替换
            XposedHelpers.findAndHookMethod(
                    TARGET_CLASS,
                    lpparam.classLoader,
                    "isReplaceMsg",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(false); // 阻止 UI 替换为“xxx撤回了一条消息”
                        }
                    }
            );

            XposedBridge.log(TAG + ": Hook 已全部生效");

        } catch (Throwable e) {
            XposedBridge.log(TAG + ": 初始化失败 - " + e.getMessage());
        }
    }

    private String safeDecodeUTF8(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            try {
                return new String(bytes, "UTF-16");
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
            if ((i + 1) % 16 == 0) sb.append("\n");
        }
        return sb.toString();
    }

    private Long safeGetLong(Object obj, String methodName) {
        try {
            Object result = XposedHelpers.callMethod(obj, methodName);
            return result instanceof Number ? ((Number) result).longValue() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Integer safeGetInt(Object obj, String methodName) {
        try {
            Object result = XposedHelpers.callMethod(obj, methodName);
            return result instanceof Number ? ((Number) result).intValue() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String safeGetString(Object obj, String methodName) {
        try {
            Object result = XposedHelpers.callMethod(obj, methodName);
            return result != null ? result.toString() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }


}
