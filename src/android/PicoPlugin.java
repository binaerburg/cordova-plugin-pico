package cordova.plugin.pico;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.palette.picoio.color.LAB;
import com.palette.picoio.color.SensorData;
import com.palette.picoio.hardware.Pico;
import com.palette.picoio.hardware.PicoConnector;
import com.palette.picoio.hardware.PicoConnectorListener;
import com.palette.picoio.hardware.PicoError;
import com.palette.picoio.hardware.PicoListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Arrays;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class PicoPlugin extends CordovaPlugin implements PicoConnectorListener, PicoListener {

    private static final int REQUEST_ACCESS_LOCATION = 2;

    private Activity activity = null;
    private Context context = null;

    // Reference to the web view for static access
    private static CordovaWebView webView = null;

    // Pico instance holder
    private Pico _pico = null;

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
    private final Intent rawIntent = new Intent("rawScan");
    private final Intent sensorDataIntent = new Intent("sensorData");
    private final Intent batteryLevelIntent = new Intent("batteryLevel");
    private final Intent batteryStatusIntent = new Intent("batteryStatus");
    private final Intent picoInfoIntent = new Intent("picoInfo");

    @Override
    public void initialize(CordovaInterface cordovaInterface, CordovaWebView webView) {
        super.initialize(cordovaInterface, webView);

        PicoPlugin.webView = webView;
        activity = this.cordova.getActivity();
        context = this.cordova.getActivity().getApplicationContext();
        log("Plugin Activity: " + this.toString());
        log("Cordova Activity: " + this.cordova.toString());
        log("Parent App Activity: " + activity.toString());
        log("Parent App Context: " + context.toString());
        log("Web View: " + PicoPlugin.webView.toString());

        PicoConnector.getInstance(context).setListener((PicoConnectorListener) this);
        log("Plugin initialized successfully!");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        log("execute called with action [" + action + "], args [" + args.toString() + "], callbackContext [" + callbackContext.toString() + "]");

        if (action.equals("destroy")) {
            this.destroy(callbackContext);
        }

        if (action.equals("connect")) {
            this.onConnectClick(callbackContext);
        }

        if (action.equals("disconnect")) {
            this.onDisconnectClick(callbackContext);
        }

        if (action.equals("scan")) {
            this.onScanClick(callbackContext);
        }

        if (action.equals("scanRaw")) {
            this.onScanRawClick(callbackContext);
        }

        if (action.equals("calibrate")) {
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

        if (_pico != null) {
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
        log("On Request Permission Result. Code: " + requestCode);
        log("On Request Permission Results. Granted: " + grantResults[0] + "," + grantResults[1]);
        switch (requestCode) {
            case REQUEST_ACCESS_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    PicoConnector.getInstance(context).connect();
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
     * scan a color and retunr the raw RGB values with each LED firing
     */
    public void onScanRawClick(CallbackContext callbackContext) {
        log("Scan Raw clicked");
        if (_pico != null)
            _curScanCallbackContext = callbackContext;

        // raw RGB values with led support
        _pico.sendRawDataRequest();
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

        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };

        if (!cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                !cordova.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            cordova.requestPermissions(this, REQUEST_ACCESS_LOCATION, permissions);
        } else {
            PicoConnector.getInstance(context).connect();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (_pico == null) {
                        log("Connection Timeout");
                        PicoConnector.getInstance(context).cancelConnect();
                        // broadcast connection failed [error]
                        final Bundle connectionErrorBundle = new Bundle();
                        connectionErrorBundle.putString("error", "Failed to connect to Pico: TIMEOUT");
                        errorIntent.putExtras(connectionErrorBundle);
                        LocalBroadcastManager.getInstance(context).sendBroadcastSync(errorIntent);
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
        _pico.setListener((PicoListener) this);

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
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(connectionIntent);

        // broadcast the sensor info
        final Bundle picoInfoBundle = new Bundle();

        if (_pico != null) {
            final Bundle singleInfos = new Bundle();
            log("Pico Name: " + _pico.getName());
            log("Pico Serial: " + _pico.getSerial());
            log("Pico BL address: " + _pico.getBluetoothAddress());
            singleInfos.putString("name", _pico.getName());
            singleInfos.putString("serial", _pico.getSerial());
            singleInfos.putString("bluetoothAddress", _pico.getBluetoothAddress());
            picoInfoBundle.putBundle("info", singleInfos);
        } else {
            picoInfoBundle.putString("info", null);
        }

        picoInfoIntent.putExtras(picoInfoBundle);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(picoInfoIntent);
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
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(errorIntent);
    }

    @Override
    public void onDisconnect(Pico pico) {
        log("Pico disconnected");
        if (_curDisconnectCallbackContext != null) {
            _curDisconnectCallbackContext.success("Pico disconnected");
        }

        // broadcast disconnect
        final Bundle connectionBundle = new Bundle();
        connectionBundle.putBoolean("connection", false);
        connectionIntent.putExtras(connectionBundle);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(connectionIntent);
    }

    @Override
    public void onFetchLabData(Pico pico, LAB lab) {
        log("Received LAB: " + lab.toString());
        if (_curScanCallbackContext != null) {
            _curScanCallbackContext.success(lab.toString());
        }

        // broadcast lab
        final Bundle labBundle = new Bundle();
        final Bundle singleLABParts = new Bundle();
        singleLABParts.putFloat("l", lab.l);
        singleLABParts.putFloat("a", lab.a);
        singleLABParts.putFloat("b", lab.b);
        labBundle.putBundle("lab", singleLABParts);
        labIntent.putExtras(labBundle);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(labIntent);
    }

    @Override
    public void onFetchRawData(Pico pico, int[] rawData) {
        ArrayList<int> rawDataList = Arrays.asList(rawData);
        log("Received raw RGB data: " + Arrays.toString(rawData));
        if (_curScanCallbackContext != null) {
            _curScanCallbackContext.success(Arrays.toString(rawData));
        }

        // broadcast raw RGB
        final Bundle rawBundle = new Bundle();
        rawBundle.putIntArrayList("raw", rawDataList);
        rawIntent.putExtras(rawBundle);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(rawIntent);
    }

    @Override
    public void onFetchSensorData(Pico pico, SensorData sensorData) {
        log("Received Sensor Data: " + sensorData.toString());

        // broadcast sensor data
        final Bundle sensorDataBundle = new Bundle();
        final Bundle singleSensorDataParts = new Bundle();
        singleSensorDataParts.putInt("r", sensorData.r);
        singleSensorDataParts.putInt("g", sensorData.g);
        singleSensorDataParts.putInt("b", sensorData.b);
        sensorDataBundle.putBundle("sensorData", singleSensorDataParts);
        sensorDataIntent.putExtras(sensorDataBundle);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(sensorDataIntent);
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
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(calibrationIntent);
    }

    @Override
    public void onFetchBatteryLevel(Pico pico, int level) {
        log("Battery level: " + level);

        // broadcast battery level
        final Bundle batteryLevelBundle = new Bundle();
        batteryLevelBundle.putInt("batteryLevel", level);
        batteryLevelIntent.putExtras(batteryLevelBundle);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(batteryLevelIntent);
    }

    @Override
    public final void onFetchBatteryStatus(Pico pico, Pico.BatteryStatus status) {
        log("Battery status: " + status);

        // broadcast battery status
        final Bundle batteryStatusBundle = new Bundle();
        batteryStatusBundle.putString("batteryStatus", status.toString());
        batteryStatusIntent.putExtras(batteryStatusBundle);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(batteryStatusIntent);
    }
}
