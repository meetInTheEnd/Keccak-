package meet.keccakrandomsamples;



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

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

import meet.keccakrandom.KeccakRandom;


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
        //屏幕清空
        TextView random = findViewById(R.id.random);
        random.setText("");
        //b=800,n=256
        long runTime_800_256 = runKeccak(10000);
        //计算速率，结果保留两位小数
        DecimalFormat df=new DecimalFormat("#.00");
        String speed = df.format(2560/(double)runTime_800_256);
        random.append("[b=800,每次输出长度n=256]\n10000次运行时间:" + String.valueOf(runTime_800_256) + "ms\n速率:" + speed + "Mbps\n");

    }

    public long runKeccak(int round) {
        long startTime = System.currentTimeMillis();
        KeccakRandom KeccakRandom = new KeccakRandom();
        String randomData = KeccakRandom.generate(sensorData);
        for (int i = 0; i < round; ++i) {
            randomData = KeccakRandom.generate(randomData);
        }
        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;
        return runTime;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

