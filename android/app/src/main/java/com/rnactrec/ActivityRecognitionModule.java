package com.rnactrec;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.gms.location.DetectedActivity;
import com.rnactrec.services.ActivityDetectionEventService;
import com.rnactrec.services.ActivityDetectionService;
import com.rnactrec.utils.Constants;

import java.util.List;
import android.util.Log;

/**
 * The module itself is responsible for defining the methods and props that will be
 * available to the RN layer in the native layer. To expose a Java method, it must be
 * annotated using @ReactMethod and the return type will always be void.
 */
public class ActivityRecognitionModule extends ReactContextBaseJavaModule {
    private static final String TAG = ActivityDetectionService.class.getSimpleName();

    public static final String REACT_CLASS = "G4MActivityRecognition";

    private static ReactApplicationContext mReactContext;
    private BroadcastReceiver mBroadcastReceiver;
    private static int detectedValue = 0;

    public ActivityRecognitionModule(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);
        this.mReactContext = reactContext;
    }

    /**
     *  The purpose of this method is to return the string name of the Native Module
     *  which represents this class in JavaScript.
     *
     *  So here we call this ActivityRecognition so that we can access it through
     *  React.NativeModules.ActivityRecognition in JavaScript
     *
     * @return the string name of the module in JS world
     */
    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    /**
     * To expose a method to JavaScript a Java method must be annotated using @ReactMethod.
     * The return type of a bridge method is always void. React Native bridge is asynchronous,
     * so the only way to pass a result to JavaScript is by using callbacks or emitting events
     */
    @ReactMethod
    public void startARTracking() {
//        Log.v(TAG, "G4F: startARTracking(): in startAR");
//        mBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
//                    int type = intent.getIntExtra("type", -1);
//                    int confidence = intent.getIntExtra("confidence", 0);
//                    handleUserActivity(context, type, confidence);
//                }
//            }
//        };
//
//        detectedValue = 0;
//        LocalBroadcastManager.getInstance(this.mReactContext).registerReceiver(mBroadcastReceiver,
//                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));

        Intent mIntent = new Intent(this.mReactContext, ActivityDetectionService.class);
        this.mReactContext.startService(mIntent);
    }

    private void handleUserActivity(Context ctx, int type, int confidence) {
        String label = "Unknown";
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = "In_Vehicle";
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = "On_Foot";
                break;
            }
            case DetectedActivity.RUNNING: {
                label = "Running";
                break;
            }
            case DetectedActivity.STILL: {
                label = "Still";
                break;
            }
            case DetectedActivity.TILTING: {
                label = "Tilting";
                break;
            }
            case DetectedActivity.WALKING: {
                label = "Walking";
                break;
            }
            case DetectedActivity.UNKNOWN: {
                break;
            }
        }

        Bundle bundle = new Bundle();

        if (detectedValue == 0) {
            detectedValue = confidence;
            bundle.putString("label", label);
            bundle.putInt("confidence", confidence);
        } else {
            if (confidence > Constants.CONFIDENCE) {
                detectedValue = confidence;
                bundle.putString("label", label);
                bundle.putInt("confidence", confidence);
            } else if (detectedValue <= confidence) {
                bundle.putString("label", label);
                bundle.putInt("confidence", confidence);
            }
        }
        if ( type == DetectedActivity.IN_VEHICLE ||
             type == DetectedActivity.WALKING ||
             type == DetectedActivity.STILL ) {
            sendEvent(ctx, Constants.ACTIVITY_TYPE, bundle);
        }
    }

    private void sendEvent(Context ctx, String eventName, @Nullable Bundle params) {

        /**
         * This part will be called every DETECTION_INTERVAL_IN_MILLISECONDS in order to detect
         * activity changes
         */
        if (!isAppOnForeground(ctx)) {
            Intent serviceIntent = new Intent(ctx, ActivityDetectionEventService.class);
            serviceIntent.putExtras(params);
            ctx.startService(serviceIntent);
            HeadlessJsTaskService.acquireWakeLockNow(ctx);
        }
    }

    private boolean isAppOnForeground(Context context) {
        /**
         We need to check if app is in foreground otherwise the app will crash.
         http://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
         **/
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    @ReactMethod
    public void stopARTracking() {
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this.mReactContext).unregisterReceiver(mBroadcastReceiver);
        }

        this.mReactContext.stopService(new Intent(this.mReactContext, ActivityDetectionService.class));
    }
}
