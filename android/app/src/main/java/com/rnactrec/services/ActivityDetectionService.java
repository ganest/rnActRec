package com.rnactrec.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.rnactrec.MainActivity;
import com.rnactrec.R;
import com.rnactrec.utils.Constants;

public class ActivityDetectionService extends Service {
    private static final String TAG = ActivityDetectionService.class.getSimpleName();

    private static final int SERVICE_NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = "RN_ACT_REC";

    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;

    public ActivityDetectionService() {
    }

    protected void initialize() {
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        mIntentService = new Intent(this, DetectedActivityIntentService.class);
        mPendingIntent = PendingIntent.getService(
                this,
                1,
                mIntentService,
                PendingIntent.FLAG_UPDATE_CURRENT);
        requestActivityUpdatesHandler();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate(): create Activity Recognition client");
        super.onCreate();
        initialize();
    }

    // request updates and set up callbacks for success or failure
    private  void requestActivityUpdatesHandler() {
        if(mActivityRecognitionClient != null){
            Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                    Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                    mPendingIntent);

            // Adds a listener that is called if the Task completes successfully.
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG, "Successfully requested activity updates");
                }
            });
            // Adds a listener that is called if the Task fails.
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Requesting activity updates failed to start");
                }
            });
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RN_ACT_REC", importance);
            channel.setDescription("CHANEL DESCRIPTION");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;

//        createNotificationChannel();
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("act rec service")
//                .setContentText("Running...")
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentIntent(contentIntent)
//                .setOngoing(true)
//                .build();
//        startForeground(SERVICE_NOTIFICATION_ID, notification);
//        return START_STICKY;START_STICKY
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // need to remove the request to Google play services. Brings down the connection.
        removeActivityUpdatesHandler();
    }

    // remove updates and set up callbacks for success or failure
    public void removeActivityUpdatesHandler() {
        if (mActivityRecognitionClient != null) {
            Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
                    mPendingIntent);
            // Adds a listener that is called if the Task completes successfully.
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG, "Removed activity updates successfully!");
                }
            });
            // Adds a listener that is called if the Task fails.
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Failed to remove activity updates!");
                }
            });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    @Override public void onTaskRemoved(Intent rootIntent){
//        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
//        PendingIntent restartServicePendingIntent = PendingIntent.getService(
//                getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager alarmService = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000,
//                restartServicePendingIntent);
//
//        super.onTaskRemoved(rootIntent);
//    }

}
