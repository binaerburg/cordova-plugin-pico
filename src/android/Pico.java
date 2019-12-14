package cordova.plugin.pico;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


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




/**
 * This class echoes a string called from JavaScript.
 */
public class Pico extends CordovaPlugin implements PicoConnectorListener, PicoListener{

    private Pico _pico;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }

        if(action.equals("connectToPico")){
            this.connectToPico(args,callbackContext);
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void connectToPico(JSONArray args, CallbackContext callback){
        if(args != null){
            try{
                //TODO connect to Pico
            
                callback.success("Success connectet");
            }catch(Exception exception){
                callback.error("Something went wrong " + exception);
            }
        }else{
            callback.error("Empty args");
        }
    }
}
