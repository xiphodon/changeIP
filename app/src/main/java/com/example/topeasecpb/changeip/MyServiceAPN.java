package com.example.topeasecpb.changeip;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;

public class MyServiceAPN extends AccessibilityService {

    // 循环周期
    float zhouqi = (float) 1.0;

    AccessibilityNodeInfo target = null;
    Handler handler = new Handler();
    // TODO Auto-generated method stub
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.i("while", "循环中...");

            if (target != null) {
                try {
                    Log.i("runable_targetview", target.toString());

                    AccessibilityNodeInfo _checkBox = getCheckBoxView();
                    if (_checkBox != null) {
                        if (_checkBox.isChecked() == false) {
                            //状态：关
                            //点击打开
                            Log.i("状态关，点击打开", _checkBox.toString());
                            target.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else {
                            //状态：开
                            //点击关闭
                            Log.i("状态开，点击关闭", _checkBox.toString());
                            target.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                            Thread.sleep(8 * 1000);

                            AccessibilityNodeInfo _checkBox2 = getCheckBoxView();
                            if (_checkBox2 != null) {
                                if (_checkBox2.isChecked() == false) {
                                    //点击打开
                                    Log.i("状态关，点击打开", _checkBox2.toString());
                                    target.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                            } else {
                                Log.i("_checkBox2", "_checkBox is null");
                            }


                        }

                    } else {
                        Log.i("_checkBox", "_checkBox is null");
                    }

                    Thread.sleep(1500);
                    AccessibilityNodeInfo _checkBox3 = getCheckBoxView();
                    if(_checkBox3 != null){
                        Log.i("操作结束，查看状态", _checkBox3.toString());
                    }else {
                        Log.i("操作结束，查看状态", "_checkBox3 is null");
                    }

                    Log.i("div", "==========================================");

                } catch (Exception e) {
                    Log.i("exception", e.toString());
//                    target = null;
//                    if(handler != null){
//                        handler.removeCallbacks(this);
//                    }
                }

            } else {
                Log.i("target", "target is null");

            }

            handler.postDelayed(this, (long) (zhouqi * 60 * 1000));
        }
    };
    private BroadcastReceiver changeIPBroadcastReceiver;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver();
        SharedPreferences sp = getSharedPreferences("changeip", MODE_PRIVATE);
        zhouqi = sp.getFloat("time",1.0f);
        Log.i("zhouqi", zhouqi + "");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 必须重写的方法：此方法用了接受系统发来的event。在你注册的event发生是被调用。在整个生命周期会被调用多次。
     * <p>
     * <p>
     * 查看当前活动的activity
     * <p>
     * linux:
     * <p>
     * adb shell dumpsys activity | grep "mFocusedActivity"
     * <p>
     * windows:
     * <p>
     * adb shell dumpsys activity | findstr "mFocusedActivity"
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String className = event.getClassName().toString();
//        Log.i("MyService:", "find an activity:" + className);
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            if (className.equals("com.android.phone.settings.MobileNetworkSettings")) {
                Log.i("MyService:", "find this activity");

                AccessibilityNodeInfo _target = getTargetView();

                if (target == null) {
                    target = _target;
                    if(handler != null){
                        handler.removeCallbacks(runnable);
                        handler.postDelayed(runnable, 500);
                    }
                } else {
                    target = _target;
                }


            }
        }
    }

    /**
     * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
     */
    @Override
    public void onInterrupt() {
        Log.i("onInterrupt", "生命周期方法 onInterrupt");
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    /**
     * 当系统连接上你的服务时被调用
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i("onServiceConnected", "生命周期方法 onServiceConnected");
    }


    /**
     * 在系统要关闭此service时调用。
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("onUnbind", "生命周期方法 onUnbind");
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        return super.onUnbind(intent);
    }

    /**
     * 获取目标checkbox控件
     *
     * @return
     */
    private AccessibilityNodeInfo getCheckBoxView() {
        AccessibilityNodeInfo _target = getTargetView();
        if (_target == null) {
            return null;
        }
        target = _target;
        Log.i("getCheckBoxView", _target.toString());
        AccessibilityNodeInfo _checkBoxView = _target.getChild(2);
        return _checkBoxView;
    }

    /**
     * 获取目标可点击的控件
     *
     * @return
     */
    private AccessibilityNodeInfo getTargetView() {

        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo != null){
            Log.i("getTargetView", "nodeInfo != null");
            List<AccessibilityNodeInfo> list_button = nodeInfo
                    .findAccessibilityNodeInfosByText("启用数据网络");

            for (AccessibilityNodeInfo item : list_button) {
                AccessibilityNodeInfo parent = item.getParent();
                if (parent.isClickable()) {
                    return parent;
                }
            }

            return null;
        }else {
            Log.i("getTargetView", "nodeInfo == null");
            List<AccessibilityWindowInfo> windowInfos = getWindows();

            for (AccessibilityWindowInfo info : windowInfos){
                AccessibilityNodeInfo root = info.getRoot();
                if(root != null){
                    return root;
                }
            }
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(changeIPBroadcastReceiver);
        Log.i("onDestroy", "生命周期方法 onDestroy");
        if (handler != null) {
            handler.removeCallbacks(runnable);
            Log.i("onDestroy", "onDestroy_removeCallbacks");
        }
    }

    /**
     * 注册广播
     */
    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.topease.changeipapn");

        changeIPBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("broadcastReceiver","收到广播");
                Bundle bundle = intent.getExtras();
                boolean isStop = bundle.getBoolean("isStop", false);
                if(isStop){

                    onDestroy();
                    System.exit(0);
                }
            }
        };

        registerReceiver(changeIPBroadcastReceiver, intentFilter);
    }
}

