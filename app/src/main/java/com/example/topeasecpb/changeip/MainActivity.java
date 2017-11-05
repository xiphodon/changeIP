package com.example.topeasecpb.changeip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
        tv_4.setText("4、进入移动网络界面，保持置顶，外挂开始工作");
        tv_5.setText("5、如需停止外挂，点击stop按钮停止外挂");
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

                startService(new Intent(MainActivity.this, MyService.class));
                Toast.makeText(getBaseContext(), "外挂已开启", Toast.LENGTH_SHORT).show();

            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getBaseContext(), "外挂已停止", Toast.LENGTH_SHORT).show();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.putExtra("isStop", true);
                intent.setAction("com.example.topease.changeip");
                sendBroadcast(intent);

            }
        });

    }



}
