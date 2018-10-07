package zzzqi.keccak;



import java.nio.charset.Charset;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //监听"获取随机数"按钮
        Button getRandom = findViewById(R.id.getRandom);
        getRandom.setOnClickListener(this);

        final Intent serviceIntent = new Intent(this,sensor.class);
        startService(serviceIntent);
    }

    private static Handler handler = new Handler();
    private static String sensorData ;

    public static void UpdateGUI(String refreshStr){
        sensorData = refreshStr;
        handler.post(RefreshLable);
    }

    private static Runnable RefreshLable = new Runnable(){
        @Override
        public void run() {}
    };

    @Override
    public void onClick(View v)
    {
        long startTime = System.currentTimeMillis();

        TextView random = findViewById(R.id.random);
        random.setText("");

        long time =System.currentTimeMillis();
        byte[] messageBytes = sensorData.getBytes(Charset.forName("UTF-8"));
        byte[] hashBytes = FIPS202.HashFunction.SHA3_256.apply(messageBytes);
        String hashHex = FIPS202.hexFromBytes(hashBytes);
        for (int i = 0; i < 9999; ++i) {
           messageBytes = hashHex.getBytes();
           hashBytes = FIPS202.HashFunction.SHA3_256.apply(messageBytes);
           hashHex=FIPS202.hexFromBytes(hashBytes);
       }
       long time2= System.currentTimeMillis();
       System.out.println(time2-time);

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;

        //获取安卓设备资源占用数据
        //数据输出到屏幕
        random.append("运行时间:\n" +String.valueOf(runTime)+"ms\n");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

