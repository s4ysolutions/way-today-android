package s4y.waytoday.watchdog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import javax.inject.Inject;

import s4y.waytoday.BuildConfig;
import s4y.waytoday.MainActivity;
import s4y.waytoday.WTApplication;
import s4y.waytoday.background.BackgroundService;
import s4y.waytoday.errors.ErrorsObservable;
import s4y.waytoday.preferences.PreferenceNextExpectedActivityTS;

public class Watchdog {
    private final static String LT = Watchdog.class.getSimpleName();
    private final static String ACTION_WATCHDOG = "solutions.s4y.waytoday.WATCHDOG";
    PendingIntent wakeupPIntent;
    long interval;

    public class WatchdogBroadcastReceiver extends BroadcastReceiver {
        @Inject
        PreferenceNextExpectedActivityTS preferenceNextExpectedActivityTS;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LT, "receiver");
            WTApplication.sApplication.getAppComponent().inject(this);
            if (ACTION_WATCHDOG.equals(intent.getAction())) {
                Log.d(LT, "got watchdog");
                long nextTS = preferenceNextExpectedActivityTS.get();
                if (nextTS > 0 && nextTS < System.currentTimeMillis()) {
                    if (!MainActivity.sHasFocus) {
                        Log.d(LT, "watchdog will start service");
                        BackgroundService.startService(context, true);
                    }
                }
                if (isAlarmStarted()) {
                    startAlarm(context);
                }
            }
        }
    }

    private BroadcastReceiver receiver = new WatchdogBroadcastReceiver();

    private synchronized void startAlarm(Context context){
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        if (wakeupPIntent != null) {
            alarmManager.cancel(wakeupPIntent);
        }
        this.wakeupPIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_WATCHDOG), 0);
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + Math.max(90*1000,interval), wakeupPIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + Math.max(0,interval), wakeupPIntent);
        }
        Log.d(LT, "Start alarm delay=" + interval);
    }

    public synchronized void start(Context context, long interval) {
        if (isAlarmStarted()) {
            stop(context);
        }

        IntentFilter filter = new IntentFilter(ACTION_WATCHDOG);
        Log.d(LT, "will do registerReceiver");
        context.registerReceiver(receiver, filter);
        Log.d(LT, "done registerReceiver");

        this.interval = interval;
        startAlarm(context);
    }

    public synchronized void stop(Context context) {
        try {
            Log.d(LT, "will do unregisterReceiver");
            context.unregisterReceiver(receiver);
            Log.d(LT, "done unregisterReceiver");
        } catch (IllegalArgumentException arg) {
            // TODO: must be fixed
            ErrorsObservable.notify(arg, BuildConfig.DEBUG);
        }
        if (wakeupPIntent == null) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(wakeupPIntent);
        } else {
            ErrorsObservable.notify(new Exception("alarmManager == null"), BuildConfig.DEBUG);
        }
        wakeupPIntent = null;
    }

    private synchronized boolean isAlarmStarted() {
        return wakeupPIntent != null;
    }
}
