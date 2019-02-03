package solutions.s4y.waytoday.strategies;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Observable;
import solutions.s4y.waytoday.R;
import solutions.s4y.waytoday.preferences.PreferenceUserStrategyUpdateFrequency;

public class UserStrategy implements Strategy {
    public final Observable<UserStrategy> observable;
    private final PreferenceUserStrategyUpdateFrequency preference;

    UserStrategy(PreferenceUserStrategyUpdateFrequency preference) {
        this.preference = preference;
        this.observable = preference.subject.map(s -> this);
    }

    @Nullable
    public static UpdateFrequency getPrev(@Nullable UpdateFrequency updateFrequency) {
        if (updateFrequency == null) {
            return null;
        }
        switch (updateFrequency) {
            case HOUR1:
                return UpdateFrequency.MIN30;
            case MIN30:
                return UpdateFrequency.MIN15;
            case MIN15:
                return UpdateFrequency.MIN5;
            case MIN5:
                return UpdateFrequency.MIN1;
            case MIN1:
                return UpdateFrequency.SEC15;
            case SEC15:
                return UpdateFrequency.SEC5;
            case SEC5:
                return UpdateFrequency.SEC1;
            default:
                return null;
        }
    }

    @Nullable
    public static UpdateFrequency getNext(@Nullable UpdateFrequency updateFrequency) {
        if (updateFrequency == null) {
            return UpdateFrequency.SEC1;
        }
        switch (updateFrequency) {
            case HOUR1:
                return null;
            case MIN30:
                return UpdateFrequency.HOUR1;
            case MIN15:
                return UpdateFrequency.MIN30;
            case MIN5:
                return UpdateFrequency.MIN15;
            case MIN1:
                return UpdateFrequency.MIN5;
            case SEC15:
                return UpdateFrequency.MIN1;
            case SEC5:
                return UpdateFrequency.SEC15;
            case SEC1:
                return UpdateFrequency.SEC5;
            default:
                return UpdateFrequency.SEC1;
        }
    }

    @NonNull
    public static String title(@NonNull Context context, @NonNull UpdateFrequency frequency) {
        int resid;
        switch (frequency) {
            case HOUR1:
                resid = R.string.freq_hour_1;
                break;
            case MIN30:
                resid = R.string.freq_min_30;
                break;
            case MIN15:
                resid = R.string.freq_min_15;
                break;
            case MIN5:
                resid = R.string.freq_min_1;
                break;
            case MIN1:
                resid = R.string.freq_min_1;
                break;
            case SEC15:
                resid = R.string.freq_sec_15;
                break;
            case SEC5:
                resid = R.string.freq_sec_5;
                break;
            default:
                resid = R.string.freq_sec_1;
                break;
        }
        return context.getString(resid);
    }

    @Override
    public long getMinDistance() {
        return 1;
    }

    @Override
    public long getMinTime() {
        switch (preference.get()) {
            case HOUR1:
                return 3600000;
            case MIN30:
                return 1800000;
            case MIN15:
                return 900000;
            case MIN5:
                return 300000;
            case MIN1:
                return 60000;
            case SEC15:
                return 15000;
            case SEC5:
                return 5000;
            default:
                return 1000;
        }
    }

    public UpdateFrequency current() {
        return preference.get();
    }

    @NonNull
    public String titleCurrent(@NonNull Context context) {
        return title(context, preference.get());
    }

    public enum UpdateFrequency {
        SEC1, SEC5, SEC15, MIN1, MIN5, MIN15, MIN30, HOUR1
    }
}
