package zzzqi.keccak;


import static zzzqi.keccak.Parameters.KECCAK_224;
import static zzzqi.keccak.Parameters.KECCAK_256;
import static zzzqi.keccak.Parameters.KECCAK_384;
import static zzzqi.keccak.Parameters.KECCAK_512;
import static zzzqi.keccak.Parameters.SHA3_224;
import static zzzqi.keccak.Parameters.SHA3_256;
import static zzzqi.keccak.Parameters.SHA3_384;
import static zzzqi.keccak.Parameters.SHA3_512;
import static zzzqi.keccak.Parameters.SHAKE128;
import static zzzqi.keccak.Parameters.SHAKE256;
import static zzzqi.keccak.HexUtils.convertBytesToString;

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

        final Intent serviceIntent = new Intent(this, sensor.class);
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

       for(int i=0; i<2; i++) {
           byte[] data = sensorData.getBytes(Charset.forName("UTF-8"));
           Keccak keccak = new Keccak();
         //convertBytesToString(keccak.getHash(data, KECCAK_512));
           random.append(convertBytesToString(keccak.getHash(data, KECCAK_512)) + "\n");
        }

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;

        //获取安卓设备资源占用数据
        AndroidMonitor AndroidMonitor = new AndroidMonitor();
        double CPU = AndroidMonitor.getCPU(getPackageName());
        double memory = AndroidMonitor.getMemory(getPackageName());
        System.out.println(getPackageName());
        //数据输出到屏幕
        random.append("运行时间:\n" +String.valueOf(runTime)+"ms\n");
        random.append("CPU:\n" + String.valueOf(CPU)+"\n");
        random.append("memory:\n" +String.valueOf(memory)+"\n");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

