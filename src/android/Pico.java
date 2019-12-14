package cordova.plugin.pico;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.palette.picoio.*;


/**
 * This class echoes a string called from JavaScript.
 */
public class Pico extends CordovaPlugin{


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }

        if(action.equals("connect")){
            this.connect(args,callbackContext);
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

    private void connect(JSONArray args, CallbackContext callback){
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
}
