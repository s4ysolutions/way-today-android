package s4y.waytoday.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import s4y.waytoday.BuildConfig;
import s4y.waytoday.errors.ErrorsObservable;
import s4y.waytoday.upload.UploadJobService;

class RetryUploadAlarm {
    private static final String LT = RetryUploadAlarm.class.getSimpleName();
    private static final String ACTION_REASON_BY_ALARM = " s4y.waytoday.uploadservice.byalarm";
    private static final int INTERVAL = 10*60*1000;
    private static PendingIntent sAlarmPIntent;

    private static BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LT, "receiver");
            UploadJobService.enqueueUploadLocations(context);
        }
    };

    static void startRetryUploadAlarmmanager(Context context) {
        if (isAlarmStarted()) {
            cancelRetryUploadAlarmmanager(context);
        }
        IntentFilter filter = new IntentFilter(ACTION_REASON_BY_ALARM);
        Log.d(LT, "will do registerReceiver");
        context.registerReceiver(receiver, filter);
        Log.d(LT, "done registerReceiver");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        if (sAlarmPIntent != null ) {
            alarmManager.cancel(sAlarmPIntent);
        }

        Intent intent = new Intent(ACTION_REASON_BY_ALARM);
        sAlarmPIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + INTERVAL, sAlarmPIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + INTERVAL, sAlarmPIntent);
        }
        Log.d(LT, "Start alarm delay=" + INTERVAL);
    }

    static void cancelRetryUploadAlarmmanager(Context context) {
        PendingIntent atomicPIntent = sAlarmPIntent;
        if (atomicPIntent == null) return;
        try {
            Log.d(LT, "will do unregisterReceiver");
            context.unregisterReceiver(receiver);
            Log.d(LT, "done unregisterReceiver");
        } catch (IllegalArgumentException arg) {
            // TODO: must be fixed
            ErrorsObservable.notify(arg, BuildConfig.DEBUG);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(atomicPIntent);
        } else {
            ErrorsObservable.notify(new Exception("alarmManager == null"), BuildConfig.DEBUG);
        }
        sAlarmPIntent = null;
    }

    private synchronized static boolean isAlarmStarted() {
        return sAlarmPIntent != null;
    }
}
