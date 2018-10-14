package meet.keccakrandomsamples;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

public class sensor extends Service{

    private Thread workThread;

    @Override
    public void onCreate() {
        super.onCreate();
        workThread = new Thread(null,backgroudWork,"WorkThread");
        //传感器
        //获取SensorManager实例
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //获取Sensor传感器类型
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        //注册SensorEventListener
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private String sensorData;
    private SensorManager sensorManager;
    private SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            sensorData = String.valueOf(event.values[0]);
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (!workThread.isAlive()){
            workThread.start();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        workThread.interrupt();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Runnable backgroudWork = new Runnable(){
        @Override
        public void run() {
            try {
                while(!Thread.interrupted()){
                    MainActivity.UpdateGUI(sensorData);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


}
