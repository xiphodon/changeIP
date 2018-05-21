package com.example.topeasecpb.changeip;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.TimeUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;

public class MyServiceAPN extends AccessibilityService {

    long time_pre = -1;
    // 循环周期
    float zhouqi = (float) 1.0;

    AccessibilityNodeInfo target = null;
    Handler handler = new Handler();
    // TODO Auto-generated method stub
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.i("loop_div", "=====================" + getCostTimes());

            if (target != null) {

                try {
//                    Log.i("runable_targetview", target.toString());

                    Log.i("choice_new_APN", hasGPRS(target)?"switch to GPRS":"switch to WAP");
                    target.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                } catch (Exception e) {
                    Log.i("exception", e.toString());
                }

            } else {
                Log.i("target", "target is null");

            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    target = getTargetView();

                    float offset = 1.0f;
                    // gprs 信号质量优于 wap，若下次目标为gprs，则等待时间缩短至offset倍周期
                    try {
                        if(hasGPRS(target)){
                            offset = 0.4f;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    handler.postDelayed(runnable, (long) (offset * zhouqi * 60 * 1000));
                }
            }, 4 * 1000);


        }
    };

    private BroadcastReceiver changeIPBroadcastReceiver;


    /**
     * 获取花费时间
     * @return
     */
    public double getCostTimes(){
        if(time_pre == -1){
            time_pre = System.currentTimeMillis();
            return 0;
        }else {
            long time_now = System.currentTimeMillis();
            long time_range = time_now - time_pre;
            time_pre = time_now;
            return time_range/1000.0;
        }
    }


    /**
     * 是否为GPRS的apn
     * @param _target
     * @return
     */
    public boolean hasGPRS(AccessibilityNodeInfo _target) throws Exception{
        if(_target != null){
            List<AccessibilityNodeInfo> list_gprs = _target.findAccessibilityNodeInfosByText("GPRS");
            if(list_gprs != null && list_gprs.size() > 0){
                return true;
            }else {
                return false;
            }
        }else {
            throw new Exception("_target is null");
        }

    }


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
            if (className.equals("com.android.settings.Settings$ApnSettingsActivity")) {
                Log.i("MyService:", "find this activity");

                AccessibilityNodeInfo _target = getTargetView();

                if (target == null) {
                    target = _target;
                    if(handler != null){
                        handler.removeCallbacks(runnable);
                        handler.postDelayed(runnable, 500);
                    }
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
     * 获取目标可点击的控件
     *
     * @return
     */
    private AccessibilityNodeInfo getTargetView() {

        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo != null){
//            Log.i("getTargetView", "nodeInfo != null");
            List<AccessibilityNodeInfo> list_button = nodeInfo
                    .findAccessibilityNodeInfosByText("中国电信");

            for (AccessibilityNodeInfo item : list_button) {
//                Log.i("find view:", item.toString());
//                Log.i("isCheckable", item.isCheckable() + "");
//                Log.i("isChecked", item.isChecked() + "");
                if (item.isCheckable() && !item.isChecked()) {
                    try {
                        Log.i("find target view", (hasGPRS(item)?"GPRS__":"WAP__") + item.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    AccessibilityNodeInfo parent = item.getParent();
//                    Log.i("target parent view", parent.toString());
                    if(parent.isClickable()){
                        return parent;
                    }
                }
            }

            return null;
        }else {
//            Log.i("getTargetView", "nodeInfo == null");
//            List<AccessibilityWindowInfo> windowInfos = getWindows();
//
//            for (AccessibilityWindowInfo info : windowInfos){
//                AccessibilityNodeInfo root = info.getRoot();
//                if(root != null){
//                    return root;
//                }
//            }
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

