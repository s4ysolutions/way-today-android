package solutions.s4y.waytoday;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GestureDetectorCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.reactivex.disposables.CompositeDisposable;
import solutions.s4y.waytoday.background.BackgroundService;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.locations.LocationUpdatesListener;
import solutions.s4y.waytoday.mainactivity.FrequencyGestureListener;
import solutions.s4y.waytoday.permissions.PermissionRequest;
import solutions.s4y.waytoday.permissions.PermissionRequestObservable;
import solutions.s4y.waytoday.preferences.PreferenceIsTracking;
import solutions.s4y.waytoday.preferences.PreferenceUpdateFrequency;
import solutions.s4y.waytoday.sound.MediaPlayerUtils;

public class MainActivity extends AppCompatActivity {
    private final static String LT = AppCompatActivity.class.getSimpleName();
    @Inject
    PreferenceUpdateFrequency mUserStrategyFrequency;
    public static boolean sHasFocus = false;
    @Inject
    PreferenceIsTracking mIsActive;

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
    @BindView(R.id.status_tracking_off)
    ImageView mLedTrackingOff;
    @BindView(R.id.status_tracking)
    ImageView mLedTrackingUnknown;
    @BindView(R.id.status_tracking_suspended)
    ImageView mLedTrackingSuspended;
    @BindView(R.id.status_tracking_on)
    ImageView mLedTrackingOn;

    private CompositeDisposable resumeDisposables;
    private GestureDetectorCompat mDetector;
    private Animation mSwitchAnimationFadeOut;
    private Animation mSwitchAnimationFadeIn;
    private boolean isSwitching;
    SparseArray<PermissionRequest> mPermissionRequests = new SparseArray<>(2);
    private BackgroundService mBackgroundService;

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

    @NonNull
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       @NonNull IBinder binder) {
            mBackgroundService = ((BackgroundService.LocationsServiceBinder) binder).getService();
            if (sHasFocus)
                mBackgroundService.removeFromForeground();
            else
                mBackgroundService.putInForeground();
            updateLedUpload();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBackgroundService = null;
            updateLedUpload();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, BackgroundService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        if (BuildConfig.DEBUG) {
            if (resumeDisposables != null) {
                ErrorsObservable.notify(new Exception("resumeDisposables != null"));
            }
        }
        resumeDisposables = new CompositeDisposable();
        resumeDisposables.add(mUserStrategyFrequency
                .subject
                .subscribe(userStrategy -> updateUserStrategyChooser()));
        resumeDisposables.add(mIsActive
                .subject
                .subscribe(userStrategy -> updateSwitch()));
        resumeDisposables.add(LocationUpdatesListener
                .subjectTrackingState
                .subscribe(state -> updateLedUpload()));
        resumeDisposables.add(PermissionRequestObservable
                .subject
                .subscribe(this::onPermissionRequest));
        updateUserStrategyChooser();
        updateSwitch();
        updateLedUpload();
    }

    @Override
    protected void onPause() {
        if (resumeDisposables == null) {
            ErrorsObservable.notify(new Exception("resumeDisposables == null"));
        } else {
            resumeDisposables.clear();
            resumeDisposables = null;
        }
        if (mIsActive.isOn()) {
            BackgroundService.startService(this, true);
        }
        if (mBackgroundService != null) {
            unbindService(mServiceConnection);
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

    @OnClick(R.id.switch_on)
    public void switchOn(View view) {
        if (isSwitching) return;
        startService();
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
                isSwitching = false;
                mSwitchAnimationFadeOut.setAnimationListener(null);
                mIsActive.set(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @OnClick(R.id.switch_off)
    public void switchOff(View view) {
        if (isSwitching) return;
        stopService();
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
                isSwitching = false;
                mSwitchAnimationFadeOut.setAnimationListener(null);
                mIsActive.set(false);
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

    private void updateSwitch() {
        if (mIsActive.isOn()) {
            mImageBtnOff.setVisibility(View.VISIBLE);
            mImageBtnOn.setVisibility(View.GONE);
        } else {
            mImageBtnOff.setVisibility(View.GONE);
            mImageBtnOn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus != sHasFocus) {
            sHasFocus = hasFocus;
        }
        if (mBackgroundService != null) {
            if (sHasFocus)
                mBackgroundService.removeFromForeground();
            else
                mBackgroundService.putInForeground();
        }
    }

    private void updateLedUpload() {
        if (BuildConfig.DEBUG) {
            Log.d(LT,
                    String.format("updateLedUpload: mBackgroundService=%b, isUpdating=%b, isSuspended=%b",
                            mBackgroundService != null,
                            LocationUpdatesListener.isUpdating,
                            LocationUpdatesListener.isSuspended
                    ));
        }
        if (mBackgroundService == null) {
            mLedTrackingOff.setVisibility(View.GONE);
            mLedTrackingUnknown.setVisibility(View.VISIBLE);
            mLedTrackingSuspended.setVisibility(View.GONE);
            mLedTrackingOn.setVisibility(View.GONE);
        } else {
            if (LocationUpdatesListener.isUpdating) {
                if (LocationUpdatesListener.isSuspended) {
                    mLedTrackingOff.setVisibility(View.GONE);
                    mLedTrackingUnknown.setVisibility(View.GONE);
                    mLedTrackingSuspended.setVisibility(View.VISIBLE);
                    mLedTrackingOn.setVisibility(View.GONE);
                } else {
                    mLedTrackingOff.setVisibility(View.GONE);
                    mLedTrackingUnknown.setVisibility(View.GONE);
                    mLedTrackingSuspended.setVisibility(View.GONE);
                    mLedTrackingOn.setVisibility(View.VISIBLE);
                }
            } else {
                mLedTrackingOff.setVisibility(View.VISIBLE);
                mLedTrackingUnknown.setVisibility(View.GONE);
                mLedTrackingSuspended.setVisibility(View.GONE);
                mLedTrackingOn.setVisibility(View.GONE);
            }
        }
    }

    private void startService() {
        if (mBackgroundService != null) {
            mBackgroundService.start(false);
        }
    }

    private void stopService() {
        if (mBackgroundService != null) {
            mBackgroundService.stop();
        }
    }

    private void onPermissionRequest(PermissionRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(request.permission) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(request.permission)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.request_location_permission)
                            .setTitle(R.string.request_permission_title)
                            .setPositiveButton(android.R.string.ok,
                                    (dialog, which) -> {
                                        int code = (int) Math.round(Math.random() * 1000);
                                        requestPermissions(new String[]{request.permission}, code);
                                    })
                            .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                            .show();
                } else {
                    // isScanRequestAbortedBecauseOfPermission=true;
                    int code = (int) Math.round(Math.random() * 1000);
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, code);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionRequest request = (mPermissionRequests.get(requestCode, null));
        if (request != null) {
            mPermissionRequests.delete(requestCode);
            request.restarter.restart();
        }
    }
}
