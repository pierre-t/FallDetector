package com.sisemb.falldetector;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.telephony.SmsManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.util.Log;
import android.os.StrictMode;

public class FallDetector extends Service implements SensorEventListener {

    /** Constants */
    private static final String SENDER_EMAIL_ADDR = "appfalldetector@gmail.com";
    private static final String SENDER_EMAIL_PASS = "senhasupersimples";

    /** Service running flag */
    public static boolean _bServiceStarted = false;

    /** Shared objects */
    private final IBinder _localBinder = new LocalBinder();
    private SharedPreferences _sharedPreferences;
    private SensorManager _sensorManager;
    private Sensor _acceleratorSensor;

    /** Sensor info */
    private float _fSensorX;
    private float _fSensorY;
    private float _fSensorZ;

    /** Service info */
    private String _strOwnerName;
    private String _strEmailAddr;
    private String _strSmsNumber;
    private String _strPhoneNumber;
    private String _strBodyText;
    private String _strSubject;

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
        // initializations for the mail sender
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // initializations for the sensor reader
        _sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		_acceleratorSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (_acceleratorSensor == null) {
			stopSelf();
		}

        // initialization of the shared preferences object
        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, R.string.service_welcome, Toast.LENGTH_SHORT) .show();
		_sensorManager.registerListener(this, _acceleratorSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
		if (t > 2.5)
            onFallDetected();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

    public void onFallDetected() {
        Toast.makeText(this, R.string.fall_detected, Toast.LENGTH_SHORT).show();

        _strOwnerName = _sharedPreferences.getString("pref_key_owner_name", "");
        _strEmailAddr = _sharedPreferences.getString("pref_key_action_email_addr", "");
        _strSmsNumber = _sharedPreferences.getString("pref_key_action_sms_number", "");
        _strPhoneNumber = _sharedPreferences.getString("pref_key_action_call_number", "");
        _strBodyText = String.format(getString(R.string.message_body_text), _strOwnerName);
        _strSubject = getText(R.string.message_subject_text).toString();

        if (_sharedPreferences.getBoolean("pref_key_action_send_email", false))
            doSendEmail();
        if (_sharedPreferences.getBoolean("pref_key_action_send_sms", false))
            doSendSms();
        if (_sharedPreferences.getBoolean("pref_key_action_make_call", false))
            doMakeCall();
        if (_sharedPreferences.getBoolean("pref_key_action_stop_service", true))
            stopSelf();
    }

    private void doSendEmail() {
        try {
            GMailSender sender = new GMailSender(SENDER_EMAIL_ADDR, SENDER_EMAIL_PASS);
            sender.sendMail(_strSubject, _strBodyText, SENDER_EMAIL_ADDR, _strEmailAddr);
            Toast.makeText(this, getString(R.string.email_dispatch_success), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("doSendEmail", e.getMessage(), e);
            Toast.makeText(this, getString(R.string.email_dispatch_fail), Toast.LENGTH_SHORT).show();
        }
    }

    private void doSendSms() {
        try {
            SmsManager.getDefault().sendTextMessage(_strSmsNumber, null, _strBodyText, null, null);
            Toast.makeText(this, getString(R.string.sms_dispatch_success), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("doSendSms", e.getMessage(), e);
            Toast.makeText(this, getString(R.string.sms_dispatch_fail), Toast.LENGTH_SHORT).show();
        }
    }

    private void doMakeCall() {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + _strPhoneNumber));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(this, getString(R.string.call_dispatch_success), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("doMakeCall", e.getMessage(), e);
            Toast.makeText(this, getString(R.string.call_dispatch_fail), Toast.LENGTH_SHORT).show();
        }
    }

    /** method for clients */
    public float[] getData() {
        return new float[]{_fSensorX, _fSensorY, _fSensorZ};
    }

    /** method for clients */
    public void stop() {
        stopSelf();
    }

    /** method for clients */
    public void testService() {
        onFallDetected();
    }
}

// end of FallDetector