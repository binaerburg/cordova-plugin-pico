package cordova.plugin.pico;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

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


public class PicoPlugin extends CordovaPlugin implements PicoConnectorListener, PicoListener{

    // Pico instance holder
    private Pico _pico;

    // current callback contexts -- Used to send data back to app
    private CallbackContext _curConnectCallbackContext;
    private CallbackContext _curDisconnectCallbackContext;
    private CallbackContext _curScanCallbackContext;
    private CallbackContext _curCalibrateCallbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
     
        if(action.equals("init")) {
            this.initialize(callbackContext);
        }

        if(action.equals("destroy")) {
            this.destroy(callbackContext)
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
     * initializes the pico handlers
     */
    private void initialize(CallbackContext callback) {
        // set callback for Pico connector
        PicoConnector.getInstance(this).setListener(this);
    }

    /**
     * destroys the pico session and disconnects it
     */
    private void destroy(CallbackContext callback) {
        super.onDestroy();

        if (_pico != null)
        {
            _pico.disconnect();
            _pico = null;
        }
    }

    private void connect(JSONArray args, CallbackContext callback) {
        if(args != null){
            try{
                //TODO connect to Pico

                callback.success("Success connected");
            }catch(Exception exception){
                callback.error("Something went wrong " + exception);
            }
        }else{
            callback.error("Empty args");
        }
    }

    /**
     * Only relevant in Android 6+ where we must handle requesting location permissions.
     */
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    PicoConnector.getInstance(this).connect();
                break;
        }
    }

    /**
     * Helper function for displaying event messages to the screen.
     */
    private void log(String text) {
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
     * PICO ACTIONS --- Connect, Disconnect, Scan, Calibrate
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * scan a color
     */
    public void onScanClick(CallbackContext callbackContext) {
        if (_pico != null)
            _curScanCallbackContext = callbackContext;
            _pico.sendLabDataRequest();
    }

    /**
     * calibrate the pico sensor
     */
    public void onCalibrateClick(CallbackContext callbackContext) {
        if (_pico != null)
            _curCalibrateCallbackContext = callbackContext;
            _pico.sendCalibrationRequest();
    }

    /**
     * request a connection to pico
     */
    public void onConnectClick(CallbackContext callbackContext) {
        // Bluetooth in Android 6+ requires location permission to function
        // so we request it here before continuing.
        _curConnectCallbackContext = callbackContext;
        if (!Permissions.hasLocationPermission(this))
            Permissions.requestLocationPermission(this, REQUEST_PERMISSION_LOCATION);
        else
            PicoConnector.getInstance(this).connect();
    }

    /**
     * disconnect the pico sensor
     */
    public void onDisconnectClick(CallbackContext callbackContext) {
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
    public void onConnectSuccess(Pico pico) {
        log("Pico connected");

        // Upon connecting, set the listener for Pico specific callbacks.
        _pico = pico;
        _pico.setListener(this);

        _pico.sendBatteryLevelRequest();
        _pico.sendBatteryStatusRequest();
        
        if (_curConnectCallbackContext != null) {
            _curConnectCallbackContext.success("Pico connected");
        }
    }
    @Override
    public void onConnectFail(PicoError e) {
        log("Failed to connect to Pico: " + e.name());
        if (_curConnectCallbackContext != null) {
            _curConnectCallbackContext.error("Failed to connect to Pico: " + e.name());
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
        /*
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
        */
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
