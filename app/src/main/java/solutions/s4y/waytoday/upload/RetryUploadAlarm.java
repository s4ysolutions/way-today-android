package solutions.s4y.waytoday.upload;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

class RetryUploadAlarm {
    private static final String ACTOIN_REASON_BY_ALARM = " solutions.s4y.waytoday.uploadservice.byalarm";
    private static final int INTERVAL = 30000;
    private static PendingIntent sAlarmPIntent;
    private static BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UploadService.enqueueUploadLocations(context);
        }
    };

    static void startRetryUploadAlarmmanager(Context context) {
        if (isAlarmStarted()) {
            cancelRetryUploadAlarmmanager(context);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UploadService.class);
        intent.putExtra(ACTOIN_REASON_BY_ALARM, true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTOIN_REASON_BY_ALARM);
        context.registerReceiver(receiver, filter);

        sAlarmPIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL, INTERVAL, sAlarmPIntent);
    }

    static void cancelRetryUploadAlarmmanager(Context context) {
        if (sAlarmPIntent == null) return;
        context.unregisterReceiver(receiver);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sAlarmPIntent);
        sAlarmPIntent = null;
    }

    private synchronized static boolean isAlarmStarted() {
        return sAlarmPIntent != null;
    }
}
