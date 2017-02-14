package io.fullstack.firestack;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.content.Context;
import android.support.annotation.Nullable;

import com.facebook.common.activitylistener.BaseActivityListener;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReactContext;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ServerValue;

interface KeySetterFn {
  String setKeyOrDefault(String a, String b);
}

@SuppressWarnings("WeakerAccess")
public class FirestackModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener
{
  private static final String TAG = "Firestack";
  private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
  private Context context;
  private ReactContext mReactContext;
  private FirebaseApp app;

  public FirestackModule(ReactApplicationContext reactContext, Context context) {
    super(reactContext);
    this.context = context;
    mReactContext = reactContext;
    Log.d(TAG, "New instance");
  }

  @Override
  public String getName() {
    return TAG;
  }

  private WritableMap getPlayServicesStatus() {
    GoogleApiAvailability gapi = GoogleApiAvailability.getInstance();
    final int status = gapi.isGooglePlayServicesAvailable(getReactApplicationContext());
    WritableMap result = Arguments.createMap();
    result.putInt("status", status);
    if (status == ConnectionResult.SUCCESS) {
      result.putBoolean("isAvailable", true);
    } else {
      result.putBoolean("isAvailable", false);
      result.putBoolean("isUserResolvableError", gapi.isUserResolvableError(status));
      result.putString("error", gapi.getErrorString(status));
    }
    return result;
  }

  @ReactMethod
  public void doPlayServicesCheck(@Nullable final Callback onComplete) {
    Log.d(TAG, "doPlayServicesCheck()");
    boolean result = doPlayServicesCheck(getCurrentActivity());
    if (onComplete != null) onComplete.invoke(null, result);
  }

  public static boolean doPlayServicesCheck(final Activity activity) {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
    resultCode = ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
    Log.d(TAG, "GoogleApiAvailability.isGooglePlayServicesAvailable resultCode: "+resultCode);
    if (resultCode != ConnectionResult.SUCCESS) {
      Log.d(TAG, "was not successful");
      if (apiAvailability.isUserResolvableError(resultCode)) {
        Log.d(TAG, "is user resolvable");
        apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST, new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            System.exit(0);
          }
        }).show();
      }
      else {
        Log.d(TAG, "This device is not supported.");
      }
      return false;
    }
    return true;
  }

  @ReactMethod
  public void configureWithOptions(final ReadableMap params, @Nullable final Callback onComplete) {
    Log.i(TAG, "configureWithOptions");

    FirebaseOptions.Builder builder = new FirebaseOptions.Builder();
    FirebaseOptions defaultOptions = FirebaseOptions.fromResource(this.context);

    if (defaultOptions == null) {
      defaultOptions = new FirebaseOptions.Builder().build();
    }

    KeySetterFn fn = new KeySetterFn() {
      public String setKeyOrDefault(
          final String key,
          final String defaultValue) {
        if (params.hasKey(key)) {
          // User-set key
          final String val = params.getString(key);
          Log.d(TAG, "Setting " + key + " from params to: " + val);
          return val;
        } else if (defaultValue != null && !defaultValue.equals("")) {
          Log.d(TAG, "Setting " + key + " from params to: " + defaultValue);
          return defaultValue;
        } else {
          return null;
        }
      }
    };

    String val = fn.setKeyOrDefault("applicationId", defaultOptions.getApplicationId());
    if (val != null) builder.setApplicationId(val);

    val = fn.setKeyOrDefault("apiKey", defaultOptions.getApiKey());
    if (val != null) builder.setApiKey(val);

    val = fn.setKeyOrDefault("gcmSenderID", defaultOptions.getGcmSenderId());
    if (val != null) builder.setGcmSenderId(val);

    val = fn.setKeyOrDefault("storageBucket", defaultOptions.getStorageBucket());
    if (val != null) builder.setStorageBucket(val);

    val = fn.setKeyOrDefault("databaseURL", defaultOptions.getDatabaseUrl());
    if (val != null) builder.setDatabaseUrl(val);

    val = fn.setKeyOrDefault("databaseUrl", defaultOptions.getDatabaseUrl());
    if (val != null) builder.setDatabaseUrl(val);

    val = fn.setKeyOrDefault("clientId", defaultOptions.getApplicationId());
    if (val != null) builder.setApplicationId(val);


    // if (params.hasKey("applicationId")) {
    //   final String applicationId = params.getString("applicationId");
    //   Log.d(TAG, "Setting applicationId from params " + applicationId);
    //   builder.setApplicationId(applicationId);
    // }
    // if (params.hasKey("apiKey")) {
    //   final String apiKey = params.getString("apiKey");
    //   Log.d(TAG, "Setting API key from params " + apiKey);
    //   builder.setApiKey(apiKey);
    // }
    // if (params.hasKey("APIKey")) {
    //   final String apiKey = params.getString("APIKey");
    //   Log.d(TAG, "Setting API key from params " + apiKey);
    //   builder.setApiKey(apiKey);
    // }
    // if (params.hasKey("gcmSenderID")) {
    //   final String gcmSenderID = params.getString("gcmSenderID");
    //   Log.d(TAG, "Setting gcmSenderID from params " + gcmSenderID );
    //   builder.setGcmSenderId(gcmSenderID);
    // }
    // if (params.hasKey("storageBucket")) {
    //   final String storageBucket = params.getString("storageBucket");
    //   Log.d(TAG, "Setting storageBucket from params " + storageBucket);
    //   builder.setStorageBucket(storageBucket);
    // }
    // if (params.hasKey("databaseURL")) {
    //   final String databaseURL = params.getString("databaseURL");
    //   Log.d(TAG, "Setting databaseURL from params " + databaseURL);
    //   builder.setDatabaseUrl(databaseURL);
    // }
    // if (params.hasKey("clientID")) {
    //   final String clientID = params.getString("clientID");
    //   Log.d(TAG, "Setting clientID from params " + clientID);
    //   builder.setApplicationId(clientID);
    // }

    try {
      Log.i(TAG, "Configuring app");
      if (app == null) {
        app = FirebaseApp.initializeApp(this.context, builder.build());
      }
      Log.i(TAG, "Configured");

      WritableMap resp = Arguments.createMap();
      resp.putString("msg", "success");
      onComplete.invoke(null, resp);
    } catch (Exception ex) {
      Log.e(TAG, "ERROR configureWithOptions");
      Log.e(TAG, ex.getMessage());

      WritableMap resp = Arguments.createMap();
      resp.putString("msg", ex.getMessage());

      onComplete.invoke(resp);
    }
  }

  @ReactMethod
  public void serverValue(@Nullable final Callback onComplete) {
    WritableMap timestampMap = Arguments.createMap();
    for (Map.Entry<String, String> entry : ServerValue.TIMESTAMP.entrySet()) {
      timestampMap.putString(entry.getKey(), entry.getValue());
    }

    WritableMap map = Arguments.createMap();
    map.putMap("TIMESTAMP", timestampMap);
    if (onComplete != null) onComplete.invoke(null, map);
  }

  // Internal helpers
  @Override
  public void onHostResume() {
    WritableMap params = Arguments.createMap();
    params.putBoolean("isForground", true);
    Utils.sendEvent(mReactContext, "FirestackAppState", params);
  }

  @Override
  public void onHostPause() {
    WritableMap params = Arguments.createMap();
    params.putBoolean("isForground", false);
    Utils.sendEvent(mReactContext, "FirestackAppState", params);
  }

  @Override
  public void onHostDestroy() {

  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("googleApiAvailability", getPlayServicesStatus());
    return constants;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST) {
      restartActivity(getCurrentActivity());
    }
  }

  static private void restartActivity(Activity activity) {
    Intent mStartActivity = new Intent(activity, activity.getClass());
    int mPendingIntentId = 123456;
    PendingIntent mPendingIntent = PendingIntent.getActivity(activity, mPendingIntentId, mStartActivity,
                                                             PendingIntent.FLAG_CANCEL_CURRENT);
    AlarmManager mgr = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
    System.exit(0);
  }
}
