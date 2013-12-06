package com.sisemb.falldetector;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class FallDetector extends Service implements SensorEventListener {

    private final IBinder _localBinder = new LocalBinder();
    public static boolean _bServiceStarted = false;
    private float _fSensorX;
    private float _fSensorY;
    private float _fSensorZ;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        FallDetector getService() {
            // Return this instance of FallDetector so clients can call public methods
            return FallDetector.this;
        }
    }

	@Override
	public void onCreate() {
		_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		_acceleratorSensor =
				_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		if (_acceleratorSensor == null) {
			stopSelf();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, R.string.service_welcome, Toast.LENGTH_SHORT)
				.show();
		_sensorManager.registerListener(
				this, _acceleratorSensor, SensorManager.SENSOR_DELAY_NORMAL);

        _bServiceStarted = true;
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
        return _localBinder;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, R.string.service_farewell, Toast.LENGTH_SHORT).show();
        _bServiceStarted = false;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
        _fSensorX = event.values[0]/SensorManager.GRAVITY_EARTH;
        _fSensorY = event.values[1]/SensorManager.GRAVITY_EARTH;
        _fSensorZ = event.values[2]/SensorManager.GRAVITY_EARTH;
		
		double t = Math.sqrt(_fSensorX*_fSensorX + _fSensorY*_fSensorY + _fSensorZ*_fSensorZ);
		
		if (t > 2.5) {
            Toast.makeText(this, R.string.fall_detected, Toast.LENGTH_SHORT).show();
            // TODO: send email, SMS and a phone ring
            stopSelf();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

    /** method for clients */
    public float[] getData() {
        return new float[]{_fSensorX, _fSensorY, _fSensorZ};
    }

    /** method for clients */
    public void stop() {
        stopSelf();
    }

	private SensorManager _sensorManager;
	private Sensor _acceleratorSensor;
}

// end of FallDetector