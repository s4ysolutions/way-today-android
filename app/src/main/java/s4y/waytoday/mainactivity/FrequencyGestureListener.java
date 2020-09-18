package s4y.waytoday.mainactivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import s4y.waytoday.BuildConfig;
import s4y.waytoday.R;
import s4y.waytoday.WTApplication;
import s4y.waytoday.preferences.PreferenceUpdateFrequency;
import s4y.waytoday.strategies.UserStrategy;

public class FrequencyGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final String LT = FrequencyGestureListener.class.getSimpleName();
    @NonNull
    private final PreferenceUpdateFrequency preference;
    @NonNull
    private final ViewGroup parent;

    private final UserStrategy userStrategy;

    public FrequencyGestureListener( @NonNull ViewGroup parent,
                                    @NonNull PreferenceUpdateFrequency preference) {
        this.preference = preference;
        this.parent = parent;
        userStrategy = new UserStrategy(preference);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "onFling: velocityX " + velocityX + " velocityY " + velocityY);
        }
        if (velocityX > 0) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "onFling: prev frequency");
            }
            preference.prev();
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "onFling: next frequency");
            }
            preference.next();
        }
        WTApplication.faFreqFling(userStrategy.getMinTime());
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Rect rect = new Rect();
        for (int index = 0; index < (parent).getChildCount(); ++index) {
            View row = (parent).getChildAt(index);
            row.getHitRect(rect);
            if (rect.contains((int) e.getX(), (int) e.getY())) {
                int move;
                switch (row.getId()) {
                    case R.id.row_prev_3:
                        move = -3;
                        break;
                    case R.id.row_prev_2:
                        move = -2;
                        break;
                    case R.id.row_prev_1:
                        move = -1;
                        break;
                    case R.id.row_next_3:
                        move = 3;
                        break;
                    case R.id.row_next_2:
                        move = 2;
                        break;
                    case R.id.row_next_1:
                        move = 1;
                        break;
                    default:
                        move = 0;
                }
                if (move != 0) {
                    preference.move(move);
                    return true;
                }
            }
        }
        WTApplication.faFreqTap(userStrategy.getMinTime());
        return super.onSingleTapUp(e);
    }
}
