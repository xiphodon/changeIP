package com.example.topeasecpb.changeip;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {


    private TextView tv_1;
    private TextView tv_2;
    private TextView tv_3;
    private TextView tv_4;
    private TextView tv_5;
    private Button btn_start;
    private Button btn_stop;
    private SharedPreferences sp;
    private EditText et_zhouqi;
    private Switch mySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initData();
        initEvent();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        mySwitch = findViewById(R.id.my_switch);
        tv_1 = findViewById(R.id.tv_1);
        tv_2 = findViewById(R.id.tv_2);
        tv_3 = findViewById(R.id.tv_3);
        tv_4 = findViewById(R.id.tv_4);
        tv_5 = findViewById(R.id.tv_5);

        et_zhouqi = findViewById(R.id.et_zhouqi);

        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        sp = getSharedPreferences("changeip", MODE_PRIVATE);

        tv_1.setText("1、请输入切换一次ip的时间周期");
        tv_2.setText("2、点击start按钮开启外挂");
        tv_3.setText("3、进入设置，打开“changeIP”功能");
        tv_4.setText("4、置顶移动网络/APN界面，外挂开始工作");
        tv_5.setText("5、如需停止外挂，点击stop按钮停止外挂");

//        Toast.makeText(this, getScreenBrightness() + "", Toast.LENGTH_LONG).show();
//
//        new Thread(){
//            @Override
//            public void run() {
//                SystemClock.sleep(3000);
//                setScreenBrightness(2);
//                SystemClock.sleep(3000);
//                setScreenBrightness(0);
//                SystemClock.sleep(3000);
//                setScreenBrightness(1);
//            }
//        }.start();
    }

    /**
     * 初始化事件
     */
    private void initEvent() {

        btn_start.setOnClickListener(new View.OnClickListener() {

            private float et_zhouqi_f;

            @Override
            public void onClick(View view) {

                String et_zhouqi_str = et_zhouqi.getText().toString().trim();

                if(TextUtils.isEmpty(et_zhouqi_str)){
                    Toast.makeText(getBaseContext(), "请输入切换ip的周期", Toast.LENGTH_SHORT).show();
                    return;
                }

                try{
                    et_zhouqi_f = Float.parseFloat(et_zhouqi_str);
                }catch (Exception e){
                    Toast.makeText(getBaseContext(), "请输入正确的周期(整数、小数)", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (et_zhouqi_f <= 0){
                    Toast.makeText(getBaseContext(), "切换ip的周期需大于0", Toast.LENGTH_SHORT).show();
                    return;
                }

                sp.edit().putFloat("time", et_zhouqi_f).commit();
                sp.edit().putBoolean("apn_isChecked", mySwitch.isChecked()).commit();

                String start_toast_str = "移动数据 外挂已开启";
                Class clazz = MyService.class;

                if(mySwitch.isChecked()){
                    start_toast_str = "网络APN 外挂已开启";
                    clazz = MyServiceAPN.class;
                }

                startService(new Intent(MainActivity.this, clazz));
                Toast.makeText(getBaseContext(), start_toast_str, Toast.LENGTH_SHORT).show();

                btn_start.setEnabled(false);

            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getBaseContext(), "外挂已停止", Toast.LENGTH_SHORT).show();

                new Thread(){
                    @Override
                    public void run() {

                        SystemClock.sleep(1000);

                        String action_str = "com.example.topease.changeip";

                        Intent intent = new Intent();
                        intent.putExtra("isStop", true);

                        if(sp.getBoolean("apn_isChecked", false)){
                            action_str = "com.example.topease.changeipapn";
                        }
                        intent.setAction(action_str);
                        sendBroadcast(intent);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_start.setEnabled(true);
                            }
                        });
                    }
                }.start();
            }
        });

    }

    /**
     * 设置手机屏幕亮度为手动模式
     */
    public void setScrennManualMode() {
        ContentResolver contentResolver = this.getContentResolver();
        try {
            int mode = Settings.System.getInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取屏幕当前亮度
     * @return
     */
    private int getScreenBrightness() {
        ContentResolver contentResolver = this.getContentResolver();
        int defVal = 125;
        return Settings.System.getInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, defVal);
    }

    /**
     * 设置屏幕亮度
     */
    private void setScreenBrightness(int value) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
            } else {
                //有了权限，具体的动作
                setScrennManualMode();
                ContentResolver contentResolver = this.getContentResolver();
                Settings.System.putInt(contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS, value);
            }
        }

    }

}
