package com.sisemb.falldetector;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.preference.PreferenceFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    private Intent _ServiceIntent;
    private boolean _bServiceBound;
    private boolean _bSettingsOpen;
    private FallDetector _Service;
    private TimerHandler _TimerHandler;
    private LocalConnection _Connection;
    private SettingsPane _SettingsPane;

    public class SettingsPane extends PreferenceFragment {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
        public void onResume() {
            super.onResume();
            View v = getView();
            if (v != null)
                v.setBackgroundColor(Color.rgb(230, 230, 230));
        }
    }

    public class TimerHandler extends Handler {
        private static final int DISPLAY_DATA = 1;
        private int _nDelay;
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DISPLAY_DATA) {
                MainActivity.this.doGetSensorData();
                sendEmptyMessageDelayed(DISPLAY_DATA, _nDelay);
            }
        }
        public void start(int nDelay) {
            _nDelay = nDelay;
            sendEmptyMessageDelayed(DISPLAY_DATA, _nDelay);
        }
        public void stop() {
            removeMessages(DISPLAY_DATA);
        }
    }

    public class LocalConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            _Service = ((FallDetector.LocalBinder)service).getService();
            MainActivity.this.onServiceConnected();
        }
        public void onServiceDisconnected(ComponentName className) {
            MainActivity.this.onServiceDisconnected();
        }
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _ServiceIntent = new Intent(this, FallDetector.class);
        _Connection = new LocalConnection();
        _TimerHandler = new TimerHandler();
        _SettingsPane = new SettingsPane();
        _bServiceBound = false;
        _bSettingsOpen = false;
        _Service = null;

        doInitialize();
	}

    @Override
    protected void onDestroy(){
        super.onDestroy();
        doUnbindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _TimerHandler.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckBox checkBox = (CheckBox)findViewById(R.id.checkBox);
        if (checkBox.isChecked())
            _TimerHandler.start(1000);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (!_bSettingsOpen) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, _SettingsPane)
                    .addToBackStack(null)
                    .commit();
            _bSettingsOpen = true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (_bSettingsOpen) {
            getFragmentManager().popBackStack();
            _bSettingsOpen = false;
        }
        else super.onBackPressed();
    }

    private void doInitialize() {
        Button button = (Button)findViewById(R.id.button);
        if (FallDetector._bServiceStarted) {
            button.setText(R.string.stop_service);
            doBindService();
        } else {
            button.setText(R.string.start_service);
        }
    }

    private void doStartService() {
        Button button = (Button)findViewById(R.id.button);
        button.setText(R.string.stop_service);
        startService(_ServiceIntent);
        doBindService();
    }

    private void doStopService() {
        Button button = (Button)findViewById(R.id.button);
        button.setText(R.string.start_service);
        _Service.stop();
    }

    private void doBindService() {
        if (!_bServiceBound)
            bindService(_ServiceIntent, _Connection, 0);
    }

    private void doUnbindService() {
        if (_bServiceBound) {
            unbindService(_Connection);
            onServiceDisconnected();
        }
    }

    private void doGetSensorData() {
        if (_bServiceBound) {
            TextView textView;
            float arData[] = _Service.getData();
            textView = (TextView)findViewById(R.id.textView);
            textView.setText(Float.toString(arData[0]));
            textView = (TextView)findViewById(R.id.textView2);
            textView.setText(Float.toString(arData[1]));
            textView = (TextView)findViewById(R.id.textView3);
            textView.setText(Float.toString(arData[2]));
        }
    }

    private void onServiceConnected() {
        _bServiceBound = true;
        Button button = (Button)findViewById(R.id.button2);
        button.setEnabled(true);
    }

    private void onServiceDisconnected() {
        _Service = null;
        _bServiceBound = false;
        Button button = (Button)findViewById(R.id.button);
        button.setText(R.string.start_service);
        button = (Button)findViewById(R.id.button2);
        button.setEnabled(false);
    }

    public void onClickButton(View v) {
        if (FallDetector._bServiceStarted)
            doStopService();
        else
            doStartService();
    }

    public void onClickButton2(View v) {
        _Service.testService();
    }

    public void onClickCheckBox(View v) {
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.sensorPane);
        CheckBox checkBox = (CheckBox)findViewById(R.id.checkBox);
        if (checkBox.isChecked()) {
            layout.setVisibility(View.VISIBLE);
            _TimerHandler.start(1000);
        } else {
            layout.setVisibility(View.INVISIBLE);
            _TimerHandler.stop();
        }
    }
}
