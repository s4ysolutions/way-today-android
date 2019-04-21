package s4y.waytoday.strategies;

import s4y.waytoday.preferences.PreferenceUpdateFrequency;

public class UserStrategy implements Strategy {
    private final PreferenceUpdateFrequency preference;

    public UserStrategy(PreferenceUpdateFrequency preference) {
        this.preference = preference;
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
}
