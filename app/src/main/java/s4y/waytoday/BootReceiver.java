package s4y.waytoday;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import s4y.waytoday.background.BackgroundService;
import s4y.waytoday.preferences.PreferenceIsTracking;

public class BootReceiver extends BroadcastReceiver {
    private final static String LT = BroadcastReceiver.class.getSimpleName();

    @Inject
    PreferenceIsTracking isTracking;

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        Log.d(LT, "onReceive");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AppComponent component = ((WTApplication) context.getApplicationContext()).getAppComponent();
            component.inject(this);
            Log.d(LT, "onReceive ACTION_BOOT_COMPLETED isTracking=" + isTracking.isOn());
            if (isTracking.isOn()) {
                ((WTApplication) context.getApplicationContext()).getAppComponent().inject(this);
                BackgroundService.startService(context, true);
            }
        }
    }
}
