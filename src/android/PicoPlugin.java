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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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


public class PicoPlugin extends CordovaPlugin implements PicoConnectorListener, PicoListener {

    private static final int REQUEST_PERMISSION_LOCATION = 0;
    private static final int REQUEST_PERMISSION_BLE = 1;
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
    private CallbackContext _curScanRawCallbackContext = null;
    private CallbackContext _curCalibrateCallbackContext = null;

    // Flag to indicate if the current sensor data request is for raw scan
    private boolean _isRawScanRequest = false;

    // Message Broadcaster
    private final Intent errorIntent = new Intent("error");
    private final Intent connectionIntent = new Intent("connection");
    private final Intent calibrationIntent = new Intent("calibration");
    private final Intent labIntent = new Intent("labScan");
    private final Intent rawScanIntent = new Intent("rawScan");
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
            return true;
        }

        if(action.equals("connect")) {
            this.onConnectClick(callbackContext);
            return true;
        }

        if(action.equals("disconnect")) {
            this.onDisconnectClick(callbackContext);
            return true;
        }

        if(action.equals("scan")) {
            this.onScanClick(callbackContext);
            return true;
        }

        if(action.equals("scanRaw")) {
            this.onScanRawClick(callbackContext);
            return true;
        }

        if(action.equals("calibrate")) {
            this.onCalibrateClick(callbackContext);
            return true;
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
        log("On Request Permission Result: " + requestCode);
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Delay slightly to allow permission system to fully propagate
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            activity.runOnUiThread(() -> startConnectWithTimeout());
                        }
                    }, 300);
                }
                break;
            case REQUEST_PERMISSION_BLE:
                boolean allGranted = true;
                if (grantResults != null) {
                    for (int r : grantResults) {
                        if (r != PackageManager.PERMISSION_GRANTED) {
                            allGranted = false;
                            break;
                        }
                    }
                }
                if (allGranted) {
                    // Delay slightly to allow permission system to fully propagate
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            activity.runOnUiThread(() -> startConnectWithTimeout());
                        }
                    }, 300);
                } else {
                    log("BLE permissions denied");
                }
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
        if (_pico != null) {
            _curScanCallbackContext = callbackContext;
            // lab values with led support
            _pico.sendLabDataRequest();
        } else {
            log("Error: Pico not connected");
            callbackContext.error("Pico not connected");
        }
    }

    /**
     * scan raw data - fires LEDs via LAB request, then captures sensor data
     */
    public void onScanRawClick(CallbackContext callbackContext) {
        log("Scan Raw clicked");
        if (_pico != null) {
            _curScanRawCallbackContext = callbackContext;
            _isRawScanRequest = true;
            // Use LAB request to fire LEDs; sensor data will be read in onFetchLabData
            _pico.sendLabDataRequest();
        } else {
            log("Error: Pico not connected");
            callbackContext.error("Pico not connected");
        }
    }

    /**
     * calibrate the pico sensor
     */
    public void onCalibrateClick(CallbackContext callbackContext) {
        log("Calibration clicked");
        if (_pico != null) {
            _curCalibrateCallbackContext = callbackContext;
            _pico.sendCalibrationRequest();
        } else {
            log("Error: Pico not connected");
            callbackContext.error("Pico not connected");
        }
    }

    /**
     * request a connection to pico
     */
    public void onConnectClick(CallbackContext callbackContext) {
        log("Connect clicked");
        _curConnectCallbackContext = callbackContext;

        // Build permission list depending on platform capabilities.
        // Android 12+ requires BLUETOOTH_SCAN/CONNECT; older stacks use coarse/fine location.
        boolean supportsModernBle = android.os.Build.VERSION.SDK_INT >= 31;

        if (supportsModernBle) {
            String[] needed = new String[] {
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                // Some BLE stacks still require location even with neverForLocation flag.
                Manifest.permission.ACCESS_FINE_LOCATION
            };
            boolean hasScan = cordova.hasPermission(Manifest.permission.BLUETOOTH_SCAN);
            boolean hasConnect = cordova.hasPermission(Manifest.permission.BLUETOOTH_CONNECT);
            boolean hasLocation = cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (!hasScan || !hasConnect || !hasLocation) {
                cordova.requestPermissions(this, REQUEST_PERMISSION_BLE, needed);
                return;
            }
        } else {
            if (!cordova.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                cordova.requestPermission(this, REQUEST_PERMISSION_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
                return;
            }
        }

        startConnectWithTimeout();
    }

    private void startConnectWithTimeout() {
        log("Starting Pico connection...");
        PicoConnector.getInstance(activity).connect();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (_pico == null) {
                    log("Connection Timeout");
                    PicoConnector.getInstance(activity).cancelConnect();
                    final Bundle connectionErrorBundle = new Bundle();
                    connectionErrorBundle.putString("error", "Failed to connect to Pico: TIMEOUT");
                    errorIntent.putExtras(connectionErrorBundle);
                    LocalBroadcastManager.getInstance(activity).sendBroadcastSync(errorIntent);
                }
            }
        }, 10000);
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

        // For raw scan requests, convert LAB to sensor-scale raw values
        // Android SDK doesn't support per-LED raw readings like iOS,
        // and sendSensorDataRequest() reads without LEDs (returns zeros).
        if (_isRawScanRequest) {
            _isRawScanRequest = false;
            log("Raw scan request - converting LAB to raw sensor scale");

            // Convert LAB to RGB (0-1 range)
            double[] rgbNorm = labToRgbNormalized(lab.l, lab.a, lab.b);

            // Scale to sensor raw values using typical white standards:
            // rwr=2000, gwg=2500, bwb=3660
            // The app normalizes: value/whiteStandard * 255
            // So we need: rgbNorm * whiteStandard
            int rawR = (int) Math.round(rgbNorm[0] * 2000);
            int rawG = (int) Math.round(rgbNorm[1] * 2500);
            int rawB = (int) Math.round(rgbNorm[2] * 3660);

            log("Converted RGB normalized: [" + rgbNorm[0] + ", " + rgbNorm[1] + ", " + rgbNorm[2] + "]");
            log("Converted to raw scale: [" + rawR + ", " + rawG + ", " + rawB + "]");

            if (_curScanRawCallbackContext != null) {
                // Format as array: [R.R, R.G, R.B, G.R, G.G, G.B, B.R, B.G, B.B]
                // Only diagonal values matter for the app's rawRgbToRgb function
                int[] rawData = new int[] {
                    rawR, 0, 0,     // Red LED: only R sensor matters
                    0, rawG, 0,     // Green LED: only G sensor matters
                    0, 0, rawB      // Blue LED: only B sensor matters
                };
                _curScanRawCallbackContext.success(java.util.Arrays.toString(rawData));
            }

            // broadcast raw scan data
            final Bundle rawScanBundle = new Bundle();
            int[] rawData = new int[] {
                rawR, 0, 0,
                0, rawG, 0,
                0, 0, rawB
            };
            rawScanBundle.putIntArray("raw", rawData);
            rawScanIntent.putExtras(rawScanBundle);
            LocalBroadcastManager.getInstance(activity).sendBroadcastSync(rawScanIntent);
            return;
        }

        if (_curScanCallbackContext != null) {
            _curScanCallbackContext.success(lab.toString());
        }

        // additionally get raw sensor data (for sensorData broadcast)
        _pico.sendSensorDataRequest();

        // broadcast lab
        final Bundle labBundle = new Bundle();
        final Bundle singleLABParts = new Bundle();
        singleLABParts.putFloat("l", lab.l);
        singleLABParts.putFloat("a", lab.a);
        singleLABParts.putFloat("b", lab.b);
        labBundle.putBundle("lab", singleLABParts);
        labIntent.putExtras(labBundle);
        LocalBroadcastManager.getInstance(activity).sendBroadcastSync(labIntent);
    }
    
    /**
     * Convert LAB color space to normalized RGB (0-1 range)
     * Standard D65 illuminant conversion
     */
    private double[] labToRgbNormalized(float l, float a, float b) {
        // LAB to XYZ
        double y = (l + 16.0) / 116.0;
        double x = a / 500.0 + y;
        double z = y - b / 200.0;

        // Apply inverse f function
        double x3 = x * x * x;
        double y3 = y * y * y;
        double z3 = z * z * z;

        x = x3 > 0.008856 ? x3 : (x - 16.0/116.0) / 7.787;
        y = y3 > 0.008856 ? y3 : (y - 16.0/116.0) / 7.787;
        z = z3 > 0.008856 ? z3 : (z - 16.0/116.0) / 7.787;

        // D65 reference white
        x *= 95.047;
        y *= 100.0;
        z *= 108.883;

        // XYZ to RGB
        x /= 100.0;
        y /= 100.0;
        z /= 100.0;

        double r = x * 3.2406 + y * -1.5372 + z * -0.4986;
        double g = x * -0.9689 + y * 1.8758 + z * 0.0415;
        double bVal = x * 0.0557 + y * -0.2040 + z * 1.0570;

        // Apply gamma correction
        r = r > 0.0031308 ? 1.055 * Math.pow(r, 1.0/2.4) - 0.055 : 12.92 * r;
        g = g > 0.0031308 ? 1.055 * Math.pow(g, 1.0/2.4) - 0.055 : 12.92 * g;
        bVal = bVal > 0.0031308 ? 1.055 * Math.pow(bVal, 1.0/2.4) - 0.055 : 12.92 * bVal;

        // Clamp to 0-1 range and return normalized values
        double rNorm = Math.max(0, Math.min(1, r));
        double gNorm = Math.max(0, Math.min(1, g));
        double bNorm = Math.max(0, Math.min(1, bVal));

        return new double[] { rNorm, gNorm, bNorm };
    }

    @Override
    public void onFetchSensorData(Pico pico, SensorData sensorData) {
        log("Received Sensor Data: " + sensorData.toString());

        // Regular sensor data broadcast
        final Bundle sensorDataBundle = new Bundle();
        final Bundle singleSensorDataParts = new Bundle();
        singleSensorDataParts.putInt("r", sensorData.r);
        singleSensorDataParts.putInt("g", sensorData.g);
        singleSensorDataParts.putInt("b", sensorData.b);
        sensorDataBundle.putBundle("sensorData", singleSensorDataParts);
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
