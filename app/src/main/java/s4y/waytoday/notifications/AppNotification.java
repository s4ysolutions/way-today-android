package s4y.waytoday.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import s4y.waytoday.MainActivity;
import s4y.waytoday.R;

public class AppNotification {
    public static final int FOREGROUND_NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "s4y.solutions.waytoday:wt1";
    private final Context context;
    private final NotificationCompat.Builder mBuilder;
    private boolean mChannelCreated = false;

    public AppNotification(Context context) {
        this.context = context;
        mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText(context.getResources().getString(R.string.waytoday_in_background))
                        .setTicker(null)
                        .setSmallIcon(context.getApplicationInfo().icon);
    }

    private void createNotificationChannel() {
        if (!mChannelCreated && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_MIN;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setSound(null, null);
            channel.setShowBadge(false);
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                mChannelCreated = true;
            }
        }
    }

    public Notification notificationForForeground() {
        createNotificationChannel();
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        return mBuilder
                .setContentIntent(resultPendingIntent)
                .build();
    }

}
