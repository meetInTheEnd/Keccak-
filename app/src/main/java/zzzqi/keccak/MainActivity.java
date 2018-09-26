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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import java.util.Date;
import java.util.Calendar;

import java.text.SimpleDateFormat;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //监听"获取随机数"按钮
        Button getRandom = findViewById(R.id.getRandom);
        getRandom.setOnClickListener(this);

        //传感器
        //获取SensorManager实例
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //获取Sensor传感器类型
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        //注册SensorEventListener
        sensorManager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
    }
    public String sensorData;
    private SensorManager sensorManager;
    //对传感器信号进行监听
    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            sensorData = String.valueOf(event.values[0]);
        }
        @Override
        public void onAccuracyChanged(Sensor sensor,int accuracy) {

        }
    };

    @Override
    public void onClick(View v) {
        //开始时间
      Calendar c = Calendar.getInstance();
        int beginHour = c.get(Calendar.HOUR_OF_DAY);
        int beginMinute = c.get(Calendar.MINUTE);
        int beginSecond = c.get(Calendar.SECOND);
        //执行Keccak算法
        byte[] data = sensorData.getBytes(Charset.forName("UTF-8"));
        TextView random = findViewById(R.id.random);
        Keccak keccak = new Keccak();
        random.setText("随机序列为:\n");
        random.append(convertBytesToString(keccak.getHash(data, KECCAK_224)));
        //结束时间
        int endHour = c.get(Calendar.HOUR_OF_DAY);
        int endMinute = c.get(Calendar.MINUTE);
        int endSecond = c.get(Calendar.SECOND);
        int costSecond = (endHour - beginHour) * 3600 + (endMinute - beginMinute) * 60 + (endSecond - beginSecond);
        random.append("\n运行时间:\n" + costSecond + "s");
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        //传感器使用完毕，释放资源
        if(sensorManager!=null){
            sensorManager.unregisterListener(listener);
        }
    }

}
