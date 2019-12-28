package cordova.plugin.pico;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.Context;
import android.util.Log;

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
    private CallbackContext _curConnectCallbackContext = null;
    private CallbackContext _curDisconnectCallbackContext = null;
    private CallbackContext _curScanCallbackContext = null;
    private CallbackContext _curCalibrateCallbackContext = null;

    @Override
    public void initialize(CordovaInterface cordovaInterface, CordovaWebView webView) {
        super.initialize(cordovaInterface, webView);

        PicoPlugin.webView = webView;
        activity = this.cordova.getActivity();
        context = activity.getApplicationContext();
        log("Plugin this: " + PicoPlugin.this.toString());
        log("Plugin Activity: " + this.toString());
        log("Cordova Activity: " + this.cordova.toString());
        log("Parent App Activity: " + activity.toString());
        log("Parent App Context: " + context.toString());
        log("Web View: " + PicoPlugin.webView.toString());

        PicoConnector.getInstance(activity).setListener((PicoConnectorListener)this);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        log("execute called...");

        if(action.equals("destroy")) {
            this.destroy(callbackContext);
        }

        if(action.equals("connect")) {
            this.onConnectClick(callbackContext);
        }

        if(action.equals("disconnect")) {
            this.onDisconnectClick(callbackContext);
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
     * scan a color
     */
    public void onScanClick(CallbackContext callbackContext) {
        log("Scan clicked");
        if (_pico != null)
            _curScanCallbackContext = callbackContext;
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

        // TODO: schauen, dass permission da ist und immer connecten... -- Handler einbauen
        if (!cordova.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            cordova.requestPermission(this, REQUEST_PERMISSION_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
            //ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);
            // Permissions.requestLocationPermission(activity, REQUEST_PERMISSION_LOCATION);
        } else {
            PicoConnector.getInstance(activity).connect();
            // TODO: Timer implementieren, der den connect Vorgang unterbricht -- oder manueller Abbruch
            // PicoConnector.getInstance(context).cancelConnect();
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

        _pico.sendBatteryLevelRequest();
        _pico.sendBatteryStatusRequest();

        log(_curConnectCallbackContext.toString());
        if (_curConnectCallbackContext != null) {
            _curConnectCallbackContext.success("Pico connected");
        }
    }
    @Override
    public void onConnectFail(PicoError paramPicoError) {
        log("Failed to connect to Pico: " + paramPicoError.name());
        if (_curConnectCallbackContext != null) {
            _curConnectCallbackContext.error("Failed to connect to Pico: " + paramPicoError.name());
        }
    }

    @Override
    public void onDisconnect(Pico pico) {
        log("Pico disconnected");
        if(_curDisconnectCallbackContext != null) {
            _curDisconnectCallbackContext.success("Pico disconnected");
        }
    }

    @Override
    public void onFetchLabData(Pico pico, LAB lab) {
        log("Received LAB: " + lab.toString());
        if (_curScanCallbackContext != null) {
            _curScanCallbackContext.success(lab.toString());
        }
    }

    public void onFetchSensorData(Pico pico, SensorData sensorData) {
        log(sensorData.toString());
    }

    @Override
    public void onCalibrationComplete(Pico pico, Pico.CalibrationResult result) {
        log("Calibration complete: " + result.name());
        if (_curCalibrateCallbackContext != null) {
            _curCalibrateCallbackContext.success("Calibration complete: " + result.name());
        }
    }

    @Override
    public void onFetchBatteryLevel(Pico pico, int level) {
        log("Battery level: " + level);
    }
    @Override
    public final void onFetchBatteryStatus(Pico pico, Pico.BatteryStatus status) {
        log("Battery status: " + status);
    }
}
