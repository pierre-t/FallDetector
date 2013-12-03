package com.sisemb.falldetector;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

public class FallDetector extends Service implements SensorEventListener {

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
		Toast.makeText(this, "Starting fall detector", Toast.LENGTH_SHORT)
				.show();
		_sensorManager.registerListener(
				this, _acceleratorSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "Fall!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float x2 = event.values[0]/SensorManager.GRAVITY_EARTH;
		float y2 = event.values[1]/SensorManager.GRAVITY_EARTH;
		float z2 = event.values[2]/SensorManager.GRAVITY_EARTH;
		
		double t = Math.sqrt(x2*x2 + y2*y2 + z2*z2);
		
		if (t > 2.5) {
			stopSelf();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	private SensorManager _sensorManager;
	private Sensor _acceleratorSensor;
}
