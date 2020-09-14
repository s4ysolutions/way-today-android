package s4y.waytoday.locations;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.subjects.BehaviorSubject;
import mad.location.manager.lib.Commons.Utils;

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

public class SensorAcc implements SensorEventListener {
    public static final String TAG = SensorAcc.class.getCanonicalName();
    public static final DataItemAcc zero=new DataItemAcc(0,0,0);
    public  final BehaviorSubject<DataItemAcc> subjectAcc =
            BehaviorSubject.createDefault( new DataItemAcc(DataItem.NOT_INITIALIZED,
                    DataItem.NOT_INITIALIZED,
                    DataItem.NOT_INITIALIZED));

    private final List<Sensor> lstSensors =
            new ArrayList<>();
    private final SensorManager sensorManager;

    /*accelerometer + rotation vector*/
    private static int[] sensorTypes = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };

    private float[] rotationMatrix = new float[16];
    private float[] rotationMatrixInv = new float[16];
    private float[] absAcceleration = new float[4];
    private float[] linearAcceleration = new float[4];

    private boolean enabled;

    public SensorAcc(Context context) {
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        enabled = sensorManager != null;
        if (!enabled) {
            return;
        }
        Arrays.fill(rotationMatrixInv, 1);

        for (Integer st : sensorTypes) {
            Sensor sensor = sensorManager.getDefaultSensor(st);
            if (sensor == null) {
                Log.d(TAG, String.format("Couldn't get sensor %d", st));
                continue;
            }
            lstSensors.add(sensor);
        }
    }

    public void startListen() {
        enabled = true;
        for (Sensor sensor : lstSensors) {
            sensorManager.unregisterListener(this, sensor);
            enabled &= !sensorManager.registerListener(this, sensor, 600000000);
                    //Utils.hertz2periodUs(sensorFfequencyHz));
        }
    }

    public void stopListen() {
        enabled = false;
        for (Sensor sensor : lstSensors)
            sensorManager.unregisterListener(this, sensor);
    }

    public DataItemAcc data() {
       return subjectAcc.getValue();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final int east = 0;
        final int north = 1;
        final int up = 2;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, linearAcceleration, 0, event.values.length);
                    android.opengl.Matrix.multiplyMV(absAcceleration, 0, rotationMatrixInv,
                            0, linearAcceleration, 0);

                    DataItemAcc sdi = new DataItemAcc(
                            absAcceleration[north],
                            absAcceleration[east],
                            absAcceleration[up]);
                    subjectAcc.onNext(sdi);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                android.opengl.Matrix.invertM(rotationMatrixInv, 0, rotationMatrix, 0);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*do nothing*/
    }
}
