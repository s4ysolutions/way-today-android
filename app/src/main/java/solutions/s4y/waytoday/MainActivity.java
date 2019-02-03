package solutions.s4y.waytoday;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.preferences.PreferenceIsTracking;
import solutions.s4y.waytoday.strategies.UserStrategy;

public class MainActivity extends AppCompatActivity {
    @Inject
    UserStrategy mUserStrategy;
    @Inject
    PreferenceIsTracking mIsTracking;

    @BindView(R.id.title_current)
    TextView mTextViewTitleCurrent;
    @BindView(R.id.title_prev_1)
    TextView mTextViewTitlePrev1;
    @BindView(R.id.title_prev_2)
    TextView mTextViewTitlePrev2;
    @BindView(R.id.title_prev_3)
    TextView mTextViewTitlePrev3;
    @BindView(R.id.title_next_1)
    TextView mTextViewTitleNext1;
    @BindView(R.id.title_next_2)
    TextView mTextViewTitleNext2;
    @BindView(R.id.title_next_3)
    TextView mTextViewTitleNext3;
    @BindView(R.id.row_current)
    View mViewRowCurrent;

    private CompositeDisposable resumeDisposables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WTApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            if (resumeDisposables != null) {
                ErrorsObservable.notify(new Exception("resumeDisposables != null"));
            }
        }
        resumeDisposables = new CompositeDisposable();
        resumeDisposables.add(mUserStrategy
                .observable
                .subscribe(userStrategy -> updateUserStrategy()));
        updateUserStrategy();
    }

    @Override
    protected void onPause() {
        if (resumeDisposables == null) {
            ErrorsObservable.notify(new Exception("resumeDisposables == null"));
        } else {
            resumeDisposables.clear();
            resumeDisposables = null;
        }
        super.onPause();
    }

    private void updateUserStrategy() {

        UserStrategy.UpdateFrequency currentFreq;
        currentFreq = mIsTracking.get() ? null : mUserStrategy.current();
        mTextViewTitleCurrent.setText(
                currentFreq == null
                        ? getString(R.string.off)
                        : UserStrategy.title(this, currentFreq));

        UserStrategy.UpdateFrequency freq;
// prevs
        freq = UserStrategy.getPrev(currentFreq);
        mTextViewTitlePrev1.setText(freq == null ? "" : UserStrategy.title(this, freq));

        freq = UserStrategy.getPrev(freq);
        mTextViewTitlePrev2.setText(freq == null ? "" : UserStrategy.title(this, freq));

        freq = UserStrategy.getPrev(freq);
        mTextViewTitlePrev3.setText(freq == null ? "" : UserStrategy.title(this, freq));
// nexts
        freq = UserStrategy.getNext(currentFreq);
        mTextViewTitleNext1.setText(freq == null ? "" : UserStrategy.title(this, freq));

        freq = UserStrategy.getNext(freq);
        mTextViewTitleNext2.setText(freq == null ? "" : UserStrategy.title(this, freq));

        freq = UserStrategy.getNext(freq);
        mTextViewTitleNext3.setText(freq == null ? "" : UserStrategy.title(this, freq));
    }
}
