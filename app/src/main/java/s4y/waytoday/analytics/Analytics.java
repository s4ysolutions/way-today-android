package s4y.waytoday.analytics;

import android.os.Bundle;

import androidx.annotation.NonNull;

public interface Analytics {
    class Param {
        public static final String ITEM_ID = "item_id";
        public static final String ITEM_NAME = "item_name";
        public static final String CONTENT_TYPE = "content_type";
        public static final String VALUE = "value";
    }
    void fa(@NonNull final String event, Bundle bundle);

    void fa(@NonNull final String event);

    void faRequestID();

    void faOn();

    void faOff();

    void faShare();

    void faVisit();

    void faSoundOn();

    void faSoundOff();

    void faFreqFling(long freq);

    void faFreqTap(long freq);

    void faNoPermission();
}
