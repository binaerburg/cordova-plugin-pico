package com.palette.picoio.demo;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.palette.picoio.color.LAB;
import com.palette.picoio.color.Match;
import com.palette.picoio.color.SensorData;
import com.palette.picoio.color.SwatchMatcher;
import com.palette.picoio.hardware.Pico;
import com.palette.picoio.hardware.PicoConnectorListener;
import com.palette.picoio.hardware.PicoError;
import com.palette.picoio.hardware.PicoListener;
import com.palette.picoio.hardware.PicoConnector;
import com.palette.picoio.utils.Permissions;

import java.util.List;

public class MainActivity extends Activity implements PicoConnectorListener, PicoListener
{
    private static final int REQUEST_PERMISSION_LOCATION = 0;

    private TextView _lblPico;

    private ViewGroup _layScan;
    private View _viewScan;

    private ScrollView _scroll;
    private TextView _lblLog;

    private Pico _pico;


    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _lblPico = findViewById(R.id.lblPico);

        _layScan = findViewById(R.id.layScan);
        _viewScan = findViewById(R.id.viewScan);

        _scroll = findViewById(R.id.scroll);
        _lblLog = findViewById(R.id.lblLog);

        // set callback for connector
        PicoConnector.getInstance(this).setListener(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (_pico != null)
        {
            _pico.disconnect();
            _pico = null;
        }
    }


    /**
     * Helper function for displaying event messages to the screen.
     */
    private void log(String text)
    {
        _lblLog.append(text + "\n");
        _scroll.post(new Runnable()
        {
            public void run()
            {
                _scroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }


    /**
     * Only relevant in Android 6+ where we must handle requesting location permissions.
     */
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    PicoConnector.getInstance(this).connect();
                break;
        }
    }


    /**
     * UI click events.
     */

    public void onScanClick(View view)
    {
        if (_pico != null)
            _pico.sendLabDataRequest();
    }
    public void onCalibrateClick(View view)
    {
        if (_pico != null)
            _pico.sendCalibrationRequest();
    }

    public void onConnectClick(View view)
    {
        // Bluetooth in Android 6+ requires location permission to function
        // so we request it here before continuing.
        if (!Permissions.hasLocationPermission(this))
            Permissions.requestLocationPermission(this, REQUEST_PERMISSION_LOCATION);
        else
            PicoConnector.getInstance(this).connect();
    }
    public void onDisconnectClick(View view)
    {
        if (_pico != null)
        {
            _pico.disconnect();
            _pico = null;
        }
    }


    /**
     * PicoConnectorListener callbacks.
     */

    @Override
    public void onConnectSuccess(Pico pico)
    {
        log("Pico connected");

        // Upon connecting, set the listener for Pico specific callbacks.
        _pico = pico;
        _pico.setListener(this);

        _lblPico.setVisibility(View.VISIBLE);
        _lblPico.setText(_pico.getName() + "\n" + _pico.getSerial());

        _pico.sendBatteryLevelRequest();
        _pico.sendBatteryStatusRequest();
    }
    @Override
    public void onConnectFail(PicoError e)
    {
        log("Failed to connect to Pico: " + e.name());
    }


    /**
     * PicoListener callbacks.
     */

    @Override
    public void onDisconnect(Pico pico)
    {
        log("Pico disconnected");

        _lblPico.setVisibility(View.GONE);
    }

    @Override
    public void onFetchLabData(Pico pico, LAB lab)
    {
        log("Received LAB: " + lab.toString());

        // Convert lab to rgb and display it.
        _viewScan.setBackgroundColor(lab.getColor());

        // Find matches and display them.
        List<Match> matches = SwatchMatcher.getMatches(lab, ExampleColorDbProvider.getSampleColors(), 3);

        for (int i = 0; i < matches.size(); i++)
        {
            Match match = matches.get(i);

            ViewGroup viewMatch = (ViewGroup)_layScan.getChildAt(i + 1);
            TextView lblName = (TextView)viewMatch.getChildAt(1);
            TextView lblDE = (TextView)viewMatch.getChildAt(2);

            viewMatch.setBackgroundColor(match.getSwatch().getLab().getColor());
            lblName.setText(match.getSwatch().getName());
            lblDE.setText(String.format("dE %.2f", match.getDistance()));
        }

        _layScan.setVisibility(View.VISIBLE);

    }
    public void onFetchSensorData(Pico pico, SensorData sensorData)
    {
        log("Received sensor data: " + sensorData.toString());
    }

    @Override
    public void onCalibrationComplete(Pico pico, Pico.CalibrationResult result)
    {
        log("Calibration complete: " + result.name());
    }

    @Override
    public void onFetchBatteryLevel(Pico pico, int level)
    {
        log("Battery level: " + level);
    }
    @Override
    public final void onFetchBatteryStatus(Pico pico, Pico.BatteryStatus status)
    {
        log("Battery status: " + status);
    }
}
