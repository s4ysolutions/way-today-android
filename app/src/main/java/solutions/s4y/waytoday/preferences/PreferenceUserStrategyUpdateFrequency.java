package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import solutions.s4y.waytoday.R;

public class PreferenceUserStrategyUpdateFrequency extends BaseStringPreference {
    PreferenceUserStrategyUpdateFrequency(@NonNull SharedPreferences preferences) {
        super(preferences, "freq", Frequencies.SEC1.toString());
    }

    public Frequencies get() {
        String s = super.getBoxed();
        return Frequencies.valueOf(s);
    }

    void set(@NonNull Frequencies value) {
        set(value.toString());
    }

    public boolean next() {
        Frequencies next = get().getNext();
        if (next == null)
            return false;
        set(next);
        return true;
    }

    public boolean prev() {
        Frequencies prev = get().getPrev();
        if (prev == null)
            return false;
        set(prev);
        return true;
    }

    public enum Frequencies {
        SEC1, SEC5, SEC15, MIN1, MIN5, MIN15, MIN30, HOUR1;

        public static final Frequencies FIRST = SEC1;
        public static final Frequencies LAST = HOUR1;

        @Nullable
        public Frequencies getPrev() {
            switch (this) {
                case HOUR1:
                    return MIN30;
                case MIN30:
                    return MIN15;
                case MIN15:
                    return MIN5;
                case MIN5:
                    return MIN1;
                case MIN1:
                    return SEC15;
                case SEC15:
                    return SEC5;
                case SEC5:
                    return SEC1;
                default:
                    return null;
            }
        }

        @Nullable
        public Frequencies getNext() {
            switch (this) {
                case HOUR1:
                    return null;
                case MIN30:
                    return HOUR1;
                case MIN15:
                    return MIN30;
                case MIN5:
                    return MIN15;
                case MIN1:
                    return MIN5;
                case SEC15:
                    return MIN1;
                case SEC5:
                    return SEC15;
                case SEC1:
                    return SEC5;
                default:
                    return SEC1;
            }
        }

        @Nullable
        public Frequencies getPrev(int count) {
            Frequencies ret = getPrev();
            while (ret != null && count > 1) {
                ret = ret.getPrev();
                count--;
            }
            return ret;
        }

        @Nullable
        public Frequencies getNext(int count) {
            Frequencies ret = getNext();
            while (ret != null && count > 1) {
                ret = ret.getNext();
                count--;
            }
            return ret;
        }

        public int getTitleResID() {
            int resid;
            switch (this) {
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
                    resid = R.string.freq_min_5;
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
            return resid;
        }

        public int getTitleResID(int count) {
            Frequencies freq = count < 0 ? getPrev(-count) : getNext(count);
            if (freq == null)
                return R.string.empty;
            else
                return freq.getTitleResID();
        }
    }
}
