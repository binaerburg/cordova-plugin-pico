package cordova.plugin.pico;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;

import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.Context;
import android.util.Log;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

// TODO: Durch androidx ersetzen?
// import android.support.v4.app.ActivityCompat;
// import android.support.v4.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.palette.picoio.color.LAB;
import com.palette.picoio.color.Swatch;
import com.palette.picoio.color.Match;
import com.palette.picoio.color.SensorData;
import com.palette.picoio.color.SwatchMatcher;
import com.palette.picoio.hardware.Pico;
import com.palette.picoio.hardware.PicoConnectorListener;
import com.palette.picoio.hardware.PicoError;
import com.palette.picoio.hardware.PicoListener;
import com.palette.picoio.hardware.PicoConnector;
import com.palette.picoio.utils.Permissions;


public class PicoPlugin extends CordovaPlugin implements PicoConnectorListener, PicoListener {

    private static final int REQUEST_PERMISSION_LOCATION = 0;
    private Activity activity = null;
    private static Context context = null;

    // Reference to the web view for static access
    private static CordovaWebView webView = null;

    // Pico instance holder
    private Pico _pico;

    // current callback contexts -- Used to send data back to app
    // These are currently not used -> we use Intents to broadcast messages!
    private CallbackContext _curConnectCallbackContext = null;
    private CallbackContext _curDisconnectCallbackContext = null;
    private CallbackContext _curScanCallbackContext = null;
    private CallbackContext _curCalibrateCallbackContext = null;

    // Message Broadcaster
    private final Intent errorIntent = new Intent("error");
    private final Intent connectionIntent = new Intent("connection");
    private final Intent calibrationIntent = new Intent("calibration");
    private final Intent labIntent = new Intent("labScan");
    private final Intent sensorDataIntent = new Intent("sensorData");
    private final Intent batteryLevelIntent = new Intent("batteryLevel");
    private final Intent batteryStatusIntent = new Intent("batteryStatus");
    private final Intent picoInfoIntent = new Intent("picoInfo");

    @Override
    public void initialize(CordovaInterface cordovaInterface, CordovaWebView webView) {
        super.initialize(cordovaInterface, webView);

        PicoPlugin.webView = webView;
        activity = this.cordova.getActivity();
        context = activity.getApplicationContext();
        log("Plugin Activity: " + this.toString());
        log("Cordova Activity: " + this.cordova.toString());
        log("Parent App Activity: " + activity.toString());
        log("Parent App Context: " + context.toString());
        log("Web View: " + PicoPlugin.webView.toString());

        PicoConnector.getInstance(activity).setListener((PicoConnectorListener)this);
        log("Plugin initialized successfully!");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        log("execute called with action [" + action + "], args [" + args.toString() + "], callbackContext [" + callbackContext.toString() + "]");

        if(action.equals("destroy")) {
            this.destroy(callbackContext);
        }

        if(action.equals("connect")) {
            this.onConnectClick(callbackContext);
        }

        if(action.equals("disconnect")) {
            this.onDisconnectClick(callbackContext);
        }
        
        if(action.equals("info")) {
            this.onPicoInfoClick(callbackContext);
        }

        if(action.equals("scan")) {
            this.onScanClick(callbackContext);
        }

        if(action.equals("calibrate")) {
            this.onCalibrateClick(callbackContext);
        }

        return false;
    }

    /**
     * destroys the pico session and disconnects it
     */
    private void destroy(CallbackContext callback) {
        log("Destroying plugin");
        super.onDestroy();

        if (_pico != null)
        {
            _curDisconnectCallbackContext = callback;
            _pico.disconnect();
            _pico = null;
        }
        callback.success("pico context destroyed");
    }

    /**
     * Only relevant in Android 6+ where we must handle requesting location permissions.
     */
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        log("On Request Permission Result");
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    PicoConnector.getInstance(activity).connect();
                break;
        }
    }

    /**
     * Helper function for displaying event messages to the screen.
     */
    private void log(String text) {
        Log.i("PicoPlugin", text);
    }

    /**
     * PICO ACTIONS --- Connect, Disconnect, Scan, Calibrate
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * get the pico sensor info - name, serial, BT address
     */
    public void onPicoInfoClick(CallbackContext callbackContext) {
        // broadcast the sensor info
        final Bundle picoInfoBundle = new Bundle();

        if (_pico != null) {
            final Bundle singleInfos = new Bundle();
            log ("Pico Name: " + _pico.getName());
            log ("Pico Serial: " + _pico.getSerial());
            log ("Pico BL address: " + _pico.getBluetoothAddress());
            singleInfos.putString("name", _pico.getName());
            singleInfos.putString("serial", _pico.getSerial());
            singleInfos.putString("bluetoothAddress", _pico.getBluetoothAddress());
            picoInfoBundle.putBundle("info", singleInfos);
        } else {
            picoInfoBundle.putString("info", null);
        }

        picoInfoIntent.putExtras(picoInfoBundle);
        LocalBroadcastManager.getInstance(activity).sendBroadcastSync(picoInfoIntent);
    }

    /**
     * scan a color
     */
    public void onScanClick(CallbackContext callbackContext) {
        log("Scan clicked");
        if (_pico != null)
            _curScanCallbackContext = callbackContext;

        // lab values with led support
        _pico.sendLabDataRequest();
    }

    /**
     * calibrate the pico sensor
     */
    public void onCalibrateClick(CallbackContext callbackContext) {
        log("Calibration clicked");
        if (_pico != null)
            _curCalibrateCallbackContext = callbackContext;
        _pico.sendCalibrationRequest();
    }

    /**
     * request a connection to pico
     */
    public void onConnectClick(CallbackContext callbackContext) {
        log("Connect clicked");
        // Bluetooth in Android 6+ requires location permission to function
        // so we request it here before continuing.
        _curConnectCallbackContext = callbackContext;

        if (!cordova.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            cordova.requestPermission(this, REQUEST_PERMISSION_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            PicoConnector.getInstance(activity).connect();
            new Timer().schedule(new TimerTask() {          
                @Override
                public void run() {
                    if (_pico == null) {
                        log("Connection Timeout");
                        PicoConnector.getInstance(activity).cancelConnect();
                        // broadcast connection failed [error]
                        final Bundle connectionErrorBundle = new Bundle();
                        connectionErrorBundle.putString("error", "Failed to connect to Pico: TIMEOUT");
                        errorIntent.putExtras(connectionErrorBundle);
                        LocalBroadcastManager.getInstance(activity).sendBroadcastSync(errorIntent);
                    }
                }
            }, 5000);
        }
    }

    /**
     * disconnect the pico sensor
     */
    public void onDisconnectClick(CallbackContext callbackContext) {
        log("Disconnect clicked");
        if (_pico != null) {
            _curDisconnectCallbackContext = callbackContext;
            _pico.disconnect();
            _pico = null;
        }
    }

    /**
     * PICO HANDLERS --- Connector Callbacks, Listener Callbacks
     * ---------------------------------------------------------------------------------------------
     */

    @Override
    public void onConnectSuccess(Pico paramPico) {
        log("Pico connected");

        // Upon connecting, set the listener for Pico specific callbacks.
        _pico = paramPico;
        _pico.setListener((PicoListener)this);

        boolean supportedBatteryLevelReq = _pico.sendBatteryLevelRequest();
        boolean supportedBatteryStatusReq = _pico.sendBatteryStatusRequest();

        log("Battery Level Request Supported: " + supportedBatteryLevelReq + ", Battery Level Request Supported: " + supportedBatteryStatusReq);
        if (_curConnectCallbackContext != null) {
            _curConnectCallbackContext.success("Pico connected");
        }
        
        // broadcast connect
        final Bundle connectionBundle = new Bundle();
        connectionBundle.putBoolean("connection", true);
        connectionIntent.putExtras(connectionBundle);
        LocalBroadcastManager.getInstance(activity).sendBroadcastSync(connectionIntent);
    }

    @Override
    public void onConnectFail(PicoError paramPicoError) {
        log("Failed to connect to Pico: " + paramPicoError.name());
        if (_curConnectCallbackContext != null) {
            _curConnectCallbackContext.error("Failed to connect to Pico: " + paramPicoError.name());
        }

        // broadcast connection failed [error]
        final Bundle connectionErrorBundle = new Bundle();
        connectionErrorBundle.putString("error", "Failed to connect to Pico: " + paramPicoError.name());
        errorIntent.putExtras(connectionErrorBundle);
        LocalBroadcastManager.getInstance(activity).sendBroadcastSync(errorIntent);
    }

    @Override
    public void onDisconnect(Pico pico) {
        log("Pico disconnected");
        if(_curDisconnectCallbackContext != null) {
            _curDisconnectCallbackContext.success("Pico disconnected");
        }

        // broadcast disconnect
        final Bundle connectionBundle = new Bundle();
        connectionBundle.putBoolean("connection", false);
        connectionIntent.putExtras(connectionBundle);
        LocalBroadcastManager.getInstance(activity).sendBroadcastSync(connectionIntent);
    }

    @Override
    public void onFetchLabData(Pico pico, LAB lab) {
        log("Received LAB: " + lab.toString());
        if (_curScanCallbackContext != null) {
            _curScanCallbackContext.success(lab.toString());
        }

        // additionally get raw sensor data without firing leds
        _pico.sendSensorDataRequest();

        // broadcast lab
        final Bundle labBundle = new Bundle();
        labBundle.putString("lab", lab.toString());
        labIntent.putExtras(labBundle);
        LocalBroadcastManager.getInstance(activity).sendBroadcastSync(labIntent);
    }

    @Override
    public void onFetchSensorData(Pico pico, SensorData sensorData) {
        log(sensorData.toString());

        // broadcast sensor data
        final Bundle sensorDataBundle = new Bundle();
        sensorDataBundle.putString("sensorData", sensorData.toString());
        sensorDataIntent.putExtras(sensorDataBundle);
        LocalBroadcastManager.getInstance(activity).sendBroadcastSync(sensorDataIntent);
    }

    @Override
    public void onCalibrationComplete(Pico pico, Pico.CalibrationResult result) {
        log("Calibration complete: " + result.name());
        if (_curCalibrateCallbackContext != null) {
            _curCalibrateCallbackContext.success("Calibration complete: " + result.name());
        }

        // broadcast calibration result
        final Bundle calibrationBundle = new Bundle();
        calibrationBundle.putString("calibrationResult", result.name());
        calibrationIntent.putExtras(calibrationBundle);
        LocalBroadcastManager.getInstance(activity).sendBroadcastSync(calibrationIntent);
    }

    @Override
    public void onFetchBatteryLevel(Pico pico, int level) {
        log("Battery level: " + level);

        // broadcast battery level
        final Bundle batteryLevelBundle = new Bundle();
        batteryLevelBundle.putInt("batteryLevel", level);
        batteryLevelIntent.putExtras(batteryLevelBundle);
        LocalBroadcastManager.getInstance(activity).sendBroadcastSync(batteryLevelIntent);
    }
    @Override
    public final void onFetchBatteryStatus(Pico pico, Pico.BatteryStatus status) {
        log("Battery status: " + status);

        // broadcast battery status
        final Bundle batteryStatusBundle = new Bundle();
        batteryStatusBundle.putString("batteryStatus", status.toString());
        batteryStatusIntent.putExtras(batteryStatusBundle);
        LocalBroadcastManager.getInstance(activity).sendBroadcastSync(batteryStatusIntent);
    }
}
