package solutions.s4y.waytoday;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.reactivex.disposables.CompositeDisposable;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.mainactivity.FrequencyGestureListener;
import solutions.s4y.waytoday.preferences.PreferenceUpdateFrequency;
import solutions.s4y.waytoday.sound.MediaPlayerUtils;

public class MainActivity extends AppCompatActivity {
    private final static String LT = AppCompatActivity.class.getSimpleName();
    @Inject
    PreferenceUpdateFrequency mUserStrategyFrequency;

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
    @BindView(R.id.gesture_controller)
    ViewGroup mViewGestureController;
    @BindView(R.id.switch_on)
    ImageView mImageBtnOn;
    @BindView(R.id.switch_off)
    ImageView mImageBtnOff;

    private CompositeDisposable resumeDisposables;
    private GestureDetectorCompat mDetector;
    private Animation mSwitchAnimationFadeOut;
    private Animation mSwitchAnimationFadeIn;
    private boolean isSwitching;

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            if (resumeDisposables != null) {
                ErrorsObservable.notify(new Exception("resumeDisposables != null"));
            }
        }
        resumeDisposables = new CompositeDisposable();
        resumeDisposables.add(mUserStrategyFrequency
                .subject
                .subscribe(userStrategy -> updateUserStrategyChooser()));
        updateUserStrategyChooser();
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

    @OnTouch(R.id.gesture_controller)
    public boolean detectGesture(MotionEvent event) {
        if (mDetector.onTouchEvent(event)) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "R.id.gesture_controller OnTouch gesture detected");
            }
            return true;
        }
        if (BuildConfig.DEBUG) {
            Log.d(LT, "R.id.gesture_controller OnTouch gesture not detected");
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WTApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mTextViewTitlePrev3.setTag(R.id.TAG_IS_TITLE, true);
        mTextViewTitlePrev2.setTag(R.id.TAG_IS_TITLE, true);
        mTextViewTitlePrev1.setTag(R.id.TAG_IS_TITLE, true);
        mTextViewTitleNext1.setTag(R.id.TAG_IS_TITLE, true);
        mTextViewTitleNext2.setTag(R.id.TAG_IS_TITLE, true);
        mTextViewTitleNext3.setTag(R.id.TAG_IS_TITLE, true);
        mDetector = new GestureDetectorCompat(this,
                new FrequencyGestureListener(mViewGestureController, mUserStrategyFrequency));
        mSwitchAnimationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        mSwitchAnimationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
    }

    @OnClick(R.id.switch_on)
    public void switchOn(View view) {
        if (isSwitching) return;
        MediaPlayerUtils.getInstance().playSwitchSound(this);
        isSwitching = true;
        mImageBtnOff.startAnimation(mSwitchAnimationFadeIn);
        mImageBtnOn.startAnimation(mSwitchAnimationFadeOut);
        mSwitchAnimationFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mImageBtnOff.setVisibility(View.VISIBLE);
                mImageBtnOn.setVisibility(View.GONE);
                isSwitching = false;
                mSwitchAnimationFadeOut.setAnimationListener(null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @OnClick(R.id.switch_off)
    public void switchOff(View view) {
        if (isSwitching) return;
        MediaPlayerUtils.getInstance().playSwitchSound(this);
        isSwitching = true;
        mImageBtnOff.startAnimation(mSwitchAnimationFadeOut);
        mImageBtnOn.startAnimation(mSwitchAnimationFadeIn);
        mSwitchAnimationFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mImageBtnOff.setVisibility(View.GONE);
                mImageBtnOn.setVisibility(View.VISIBLE);
                isSwitching = false;
                mSwitchAnimationFadeOut.setAnimationListener(null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void updateUserStrategyChooser() {
        PreferenceUpdateFrequency.Frequencies current =
                mUserStrategyFrequency.get();

        mTextViewTitlePrev3.setText(current.getTitleResID(-3));
        mTextViewTitlePrev2.setText(current.getTitleResID(-2));
        mTextViewTitlePrev1.setText(current.getTitleResID(-1));
        mTextViewTitleCurrent.setText(current.getTitleResID());
        mTextViewTitleNext1.setText(current.getTitleResID(1));
        mTextViewTitleNext2.setText(current.getTitleResID(2));
        mTextViewTitleNext3.setText(current.getTitleResID(3));
    }
}
