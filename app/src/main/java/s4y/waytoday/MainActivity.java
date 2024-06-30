package s4y.waytoday;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import s4y.gps.sdk.GPSUpdate;
import s4y.gps.sdk.android.GPSPermissionManager;
import s4y.gps.sdk.android.GPSPowerManager;
import s4y.gps.sdk.android.GPSUpdatesForegroundService;
import s4y.gps.sdk.dependencies.IGPSUpdatesProvider;
import s4y.waytoday.analytics.Analytics;
import s4y.waytoday.errors.ErrorsObservable;
import s4y.waytoday.mainactivity.FrequencyGestureListener;
import s4y.waytoday.preferences.PreferenceSound;
import s4y.waytoday.preferences.PreferenceUpdateFrequency;
import s4y.waytoday.sound.MediaPlayerUtils;

import solutions.s4y.waytoday.sdk.AndroidWayTodayClient;
import solutions.s4y.waytoday.sdk.ITrackIdChangeListener;
import solutions.s4y.waytoday.sdk.IUploadingLocationsStatusChangeListener;
import solutions.s4y.waytoday.sdk.UploadingLocationsStatus;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 123;
    private final static String LT = MainActivity.class.getSimpleName();
    @Inject
    PreferenceUpdateFrequency mUserStrategyFrequency;
    public static boolean sHasFocus = false;
    @Inject
    PreferenceSound mSound;
    @Inject
    Analytics mAnalytics;
    @Inject
    AndroidWayTodayClient mWTClient;
    TextView mTextViewTitleCurrent;
    TextView mTextViewTitlePrev1;
    TextView mTextViewTitlePrev2;
    TextView mTextViewTitlePrev3;
    TextView mTextViewTitleNext1;
    TextView mTextViewTitleNext2;
    TextView mTextViewTitleNext3;
    View mViewRowCurrent;
    ViewGroup mViewGestureController;
    ImageView mImageBtnOn;
    ImageView mImageBtnOff;
    ImageView mLedTrackingWarmUp;
    ImageView mLedTrackingOff;
    ImageView mLedTrackingError;
    ImageView mLedTrackingOn;
    TextView mTextID;
    ImageView mBtnTrackID;
    ImageView mLedGpsWait;
    ImageView mLedGpsNew;
    ImageView mLedUploadEmpty;
    ImageView mLedUploadQueue;
    ImageView mLedUploadUploading;
    ImageView mLedUploadError;
    ImageView mBtnSoundOn;
    ImageView mBtnSoundOff;
    TextView mTextOn;
    TextView mTextOff;

    private CompositeDisposable resumeDisposables;
    private GestureDetectorCompat mDetector;
    private Animation mSwitchAnimationFadeOut;
    private Animation mSwitchAnimationFadeIn;
    private Animation mLedGpsNewAnimationFadeOut;
    private Animation mLedUploadingAnimationFadeOut;
    private Animation mTextOnAnimationFadeOut;
    private Animation mTextOffAnimationFadeOut;
    private boolean isSwitching;

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT > 32) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Should we show rationale?
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Notification Permission Required")
                            .setMessage("The application will display an icon when running in the background so you can activate and stop it.")
                            .setPositiveButton(
                                    "OK",
                                    (dialog, which) -> requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION_PERMISSION))
                            .setNegativeButton(
                                    "Cancel",
                                    null)
                            .create()
                            .show();
                } else {
                    // No explanation needed, request the permission directly
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION_PERMISSION);
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WTApplication) getApplication()).getDaggerComponent().inject(this);

        setContentView(R.layout.activity_main);
        mTextViewTitleCurrent = findViewById(R.id.title_current);
        mTextViewTitlePrev1 = findViewById(R.id.title_prev_1);
        mTextViewTitlePrev2 = findViewById(R.id.title_prev_2);
        mTextViewTitlePrev3 = findViewById(R.id.title_prev_3);
        mTextViewTitleNext1 = findViewById(R.id.title_next_1);
        mTextViewTitleNext2 = findViewById(R.id.title_next_2);
        mTextViewTitleNext3 = findViewById(R.id.title_next_3);
        mViewRowCurrent = findViewById(R.id.row_current);
        mViewGestureController = findViewById(R.id.gesture_controller);
        mImageBtnOn = findViewById(R.id.switch_on);
        mImageBtnOff = findViewById(R.id.switch_off);
        mLedTrackingWarmUp = findViewById(R.id.status_tracking_warmup);
        mLedTrackingOff = findViewById(R.id.status_tracking_off);
        mLedTrackingError = findViewById(R.id.status_tracking_error);
        mLedTrackingOn = findViewById(R.id.status_tracking_on);
        mTextID = findViewById(R.id.textID);
        mBtnTrackID = findViewById(R.id.btn_track_id);
        mLedGpsWait = findViewById(R.id.status_gps_wait);
        mLedGpsNew = findViewById(R.id.status_gps_new);
        mLedUploadEmpty = findViewById(R.id.status_upload_empty);
        mLedUploadQueue = findViewById(R.id.status_upload_queue);
        mLedUploadUploading = findViewById(R.id.status_upload_uploading);
        mLedUploadError = findViewById(R.id.status_upload_error);
        mBtnSoundOn = findViewById(R.id.btn_sound_on);
        mBtnSoundOff = findViewById(R.id.btn_sound_off);
        mTextOn = findViewById(R.id.text_on);
        mTextOff = findViewById(R.id.text_off);

        mTextViewTitlePrev3.setTag(R.id.TAG_IS_TITLE, true);
        mTextViewTitlePrev2.setTag(R.id.TAG_IS_TITLE, true);
        mTextViewTitlePrev1.setTag(R.id.TAG_IS_TITLE, true);
        mTextViewTitleNext1.setTag(R.id.TAG_IS_TITLE, true);
        mTextViewTitleNext2.setTag(R.id.TAG_IS_TITLE, true);
        mTextViewTitleNext3.setTag(R.id.TAG_IS_TITLE, true);
        mDetector = new GestureDetectorCompat(this,
                new FrequencyGestureListener(
                        mAnalytics,
                        mViewGestureController,
                        mUserStrategyFrequency));
        mSwitchAnimationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein400);
        mSwitchAnimationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout400);
        mLedGpsNewAnimationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout300);
        mLedGpsNewAnimationFadeOut.setAnimationListener(ledGpsNewAnimationListener);
        mLedUploadingAnimationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout400);
        mLedUploadingAnimationFadeOut.setAnimationListener(ledUploadingAnimationListener);
        mTextOnAnimationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout_on_off);
        mTextOnAnimationFadeOut.setAnimationListener(textOnOffAnimationListener);
        mTextOffAnimationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout_on_off);
        mTextOffAnimationFadeOut.setAnimationListener(textOnOffAnimationListener);

        findViewById(R.id.switch_on).setOnTouchListener((v, event) -> {
            if (isSwitching) return true;
            if (GPSPermissionManager.needPermissionRequest(this, true)) {
                GPSPermissionManager.requestPermissions(this, true);
            }
            startTracking();
            MediaPlayerUtils.getInstance(this).playSwitchSound(this);
            isSwitching = true;
            mImageBtnOff.setVisibility(View.VISIBLE);
//        mImageBtnOff.setAlpha(0f);
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
                    mWTClient.turnTrackingOn();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mTextOff.setVisibility(View.GONE);
            mTextOn.setVisibility(View.VISIBLE);
            mTextOn.setAlpha(1);
            mTextOn.startAnimation(mTextOnAnimationFadeOut);
            mAnalytics.faOn();
            checkNotificationPermission();
            return true;
        });

        findViewById(R.id.gesture_controller).setOnTouchListener((v, event) -> {
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
        });

        findViewById(R.id.switch_off).setOnClickListener((View view) -> {
            if (isSwitching) return;
            stopTracking();
            MediaPlayerUtils.getInstance(this).playSwitchSound(this);
            isSwitching = true;
            mImageBtnOn.setVisibility(View.VISIBLE);
            //    mImageBtnOn.setAlpha(0f);
            mImageBtnOn.startAnimation(mSwitchAnimationFadeIn);
            mImageBtnOff.startAnimation(mSwitchAnimationFadeOut);
            mSwitchAnimationFadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    isSwitching = false;
                    mSwitchAnimationFadeOut.setAnimationListener(null);
                    // GPSUpdatesForegroundService subscribed to the status and will stop itself
                    mWTClient.turnTrackingOff();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mTextOff.setVisibility(View.VISIBLE);
            mTextOff.setAlpha(1);
            mTextOff.startAnimation(mTextOffAnimationFadeOut);
            mAnalytics.faOff();
        });

        findViewById(R.id.btn_track_id).setOnClickListener((View view) -> {
            mBtnTrackID.setAlpha(alphaIdInProgress);
            mTextID.setAlpha(alphaIdInProgress);
            mWTClient.enqueueTrackIdWorkRequest(this);
            mAnalytics.faRequestID();
        });

        findViewById(R.id.btn_sound_on).setOnClickListener((View v) -> {
            mSound.set(false);
            updateSound();
            mAnalytics.faSoundOn();
        });

        findViewById(R.id.btn_sound_off).setOnClickListener((View v) -> {
            mSound.set(true);
            updateSound();
            mAnalytics.faSoundOff();
        });

        findViewById(R.id.btn_way_today).setOnClickListener((View v) -> {
            if (!mWTClient.wtClient.hasTrackerId()) return;
            Intent browserIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://way.today/#" +
                            mWTClient.wtClient.getCurrentTrackerId()));
            startActivity(browserIntent);
            mAnalytics.faVisit();
        });

        findViewById(R.id.btn_share).setOnClickListener((View v) -> {
            if (!mWTClient.wtClient.hasTrackerId()) return;
            String txt = String.format(
                    getResources().getString(R.string.share_link),
                    mWTClient.wtClient.getCurrentTrackerId());
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, txt);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subj));
            // sendIntent.setType("message/rfc822");
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.share_title)));
            mAnalytics.faShare();
        });

        if (mWTClient.isTrackingOn() && !GPSPermissionManager.needPermissionRequest(this, true)) {
            startTracking();
        }
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
        resumeDisposables.add(mUserStrategyFrequency
                .subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userStrategy -> {
                    updateUserStrategyChooser();
                    requestIgnoreOptimization();
                }));
        resumeDisposables.add(ErrorsObservable
                .subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignored -> updateAllViews()));
        mWTClient.wtClient.addTrackIdChangeListener(updateTrackIDWithDebug);
        mWTClient.gpsUpdatesManager.getStatus().addListener(onTrackingStatusChange);
        mWTClient.gpsUpdatesManager.getLast().addListener(onGPSUpdate);
        mWTClient.wtClient.addUploadingLocationsStatusChangeListener(this.onUploadStatusChanged);
        if (!mWTClient.wtClient.hasTrackerId()) {
            mWTClient.enqueueTrackIdWorkRequest(this);
        }
        updateAllViews();
        if (mWTClient.isTrackingOn() && GPSPermissionManager.needPermissionRequest(this, true)) {
            runOnUiThread(() -> GPSPermissionManager.requestPermissions(this, true));
        }
    }

    @Override
    protected void onPause() {
        if (resumeDisposables == null) {
            ErrorsObservable.notify(new Exception("resumeDisposables == null"));
        } else {
            resumeDisposables.clear();
            resumeDisposables = null;
        }
        mWTClient.wtClient.removeTrackIdChangeListener(updateTrackIDWithDebug);
        mWTClient.gpsUpdatesManager.getStatus().removeListener(onTrackingStatusChange);
        mWTClient.gpsUpdatesManager.getLast().removeListener(onGPSUpdate);
        mWTClient.wtClient.removeUploadingLocationsStatusChangeListener(this.onUploadStatusChanged);
        if (mWTClient.isTrackingOn() && !GPSPermissionManager.needPermissionRequest(this, true))
            GPSUpdatesForegroundService.start(this);
        super.onPause();
    }

    private final Animation.AnimationListener textOnOffAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mTextOff.setVisibility(View.GONE);
            mTextOn.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private boolean mLedGpsNewAnimated = false;
    private static final float alphaIdInProgress = 0.3f;
    private static final float alphaIdNotInProgress = 0.9f;

    long requestCount = 1;
    long requestCountM = 5;

    private void requestIgnoreOptimization() {
        if (!mWTClient.isTrackingOn())
            return;
        boolean needRequestIgnoreOptimization =
                GPSPowerManager.needRequestIgnoreOptimization(this);
        if (!needRequestIgnoreOptimization)
            return;
        if (requestCount++ % requestCountM != 0)
            return;
        requestCountM++;
        GPSPowerManager.requestIgnoreOptimization(this);
    }

    private void updateUserStrategyChooser() {
        PreferenceUpdateFrequency.Frequencies current =
                mUserStrategyFrequency.get();
        mWTClient.gpsUpdatesManager.setIntervalSec(current.getSeconds());

        mTextViewTitlePrev3.setText(current.getTitleResID(-3));
        mTextViewTitlePrev2.setText(current.getTitleResID(-2));
        mTextViewTitlePrev1.setText(current.getTitleResID(-1));
        mTextViewTitleCurrent.setText(current.getTitleResID());
        mTextViewTitleNext1.setText(current.getTitleResID(1));
        mTextViewTitleNext2.setText(current.getTitleResID(2));
        mTextViewTitleNext3.setText(current.getTitleResID(3));
        requestIgnoreOptimization();
    }

    private void updateSwitchOnOff() {
        boolean isTrackingOn = mWTClient.isTrackingOn();
        Log.d("updateSwitch", "updateSwitch isTrackingOn=" + isTrackingOn);
        if (isTrackingOn) {
            mImageBtnOff.setVisibility(View.VISIBLE);
            mImageBtnOn.setVisibility(View.GONE);
        } else {
            mImageBtnOff.setVisibility(View.GONE);
            mImageBtnOn.setVisibility(View.VISIBLE);
        }
    }

    private final Function1<IGPSUpdatesProvider.Status, Unit> onTrackingStatusChange =
            (IGPSUpdatesProvider.Status status) -> {
                Log.d("updateSwitch", "updateSwitch status=" + status.toString());
                runOnUiThread(() -> {
                    updateSwitchOnOff();
                    updateLedTracking();
                });
                return Unit.INSTANCE;
            };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus != sHasFocus) {
            sHasFocus = hasFocus;
            if (sHasFocus) {
                GPSUpdatesForegroundService.removeFromForeground(this);
            }
        }
    }

    private void updateLedTracking() {
        if (BuildConfig.DEBUG) {
            Log.d(LT, String.format("updateLedTracking: status=%s, needPermissionRequest=%b",
                    mWTClient.gpsUpdatesManager.getStatus(),
                    GPSPermissionManager.needPermissionRequest(this, true)
            ));
        }

        if (!mWTClient.gpsUpdatesManager.getStatus().isIdle()) {
            mLedGpsWait.setVisibility(View.VISIBLE);
            mLedUploadEmpty.setVisibility(View.VISIBLE);
        }

        if (mWTClient.gpsUpdatesManager.getStatus().isIdle()) {
            mLedTrackingOff.setVisibility(View.VISIBLE);
            mLedTrackingWarmUp.setVisibility(View.GONE);
            mLedTrackingOn.setVisibility(View.GONE);
            mLedTrackingError.setVisibility(View.GONE);

            mLedGpsWait.setVisibility(View.GONE);
            mLedGpsNew.setVisibility(View.GONE);

            mLedUploadEmpty.setVisibility(View.GONE);
            mLedUploadQueue.setVisibility(View.GONE);
            mLedUploadUploading.setVisibility(View.GONE);
            mLedUploadError.setVisibility(View.GONE);

        } else if (mWTClient.gpsUpdatesManager.getStatus().isWarmingUp()) {
            mLedTrackingOff.setVisibility(View.GONE);
            mLedTrackingWarmUp.setVisibility(View.VISIBLE);
            mLedTrackingOn.setVisibility(View.GONE);
            mLedTrackingError.setVisibility(View.GONE);
        } else if (mWTClient.gpsUpdatesManager.getStatus().isActive()) {
            mLedTrackingOff.setVisibility(View.GONE);
            mLedTrackingWarmUp.setVisibility(View.GONE);
            mLedTrackingOn.setVisibility(View.VISIBLE);
            mLedTrackingError.setVisibility(View.GONE);
        } else {
            mLedTrackingOff.setVisibility(View.GONE);
            mLedTrackingWarmUp.setVisibility(View.GONE);
            mLedTrackingOn.setVisibility(View.GONE);
            mLedTrackingError.setVisibility(View.VISIBLE);
        }
    }


    private void updateAllViews() {
        updateUserStrategyChooser();
        updateSwitchOnOff();
        updateLedTracking();
        updateTrackID();
        updateLedUploading();
        updateSound();
        hideOnOff();
    }


    private final ITrackIdChangeListener updateTrackIDWithDebug = trackID -> {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "updateTrackIDWithSound ${notUsed}");
        }
        runOnUiThread(this::updateTrackID);
    };

    private void updateTrackID() {
        boolean inProgress = mWTClient.wtClient.isRequestNewTrackerIdInProgress();
        boolean failed = mWTClient.wtClient.isRequestNewTrackerIdFailed();

        float alpha = (inProgress && !failed)
                ? alphaIdInProgress : alphaIdNotInProgress;
        mBtnTrackID.setAlpha(alpha);
        mTextID.setAlpha(alpha);
        if (BuildConfig.DEBUG) {
            Log.d(LT,
                    "updateTrackID id=\"" + mWTClient.wtClient.getCurrentTrackerId() + "\" isSet=" + mWTClient.wtClient.hasTrackerId() +
                            " failed=" + failed + " inProgress=" + inProgress +
                            " alpha=" + alpha);
        }
        if (mWTClient.wtClient.hasTrackerId()) {
            mTextID.setText(mWTClient.wtClient.getCurrentTrackerId());
        } else {
            if (failed) {
                mTextID.setText(R.string.id_request_failed);
            } else {
                mTextID.setText(" ... ");
            }
        }
    }

    private final Animation.AnimationListener ledGpsNewAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mLedGpsNewAnimated = false;
            mLedGpsNew.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    private boolean sUploading;
    private final Animation.AnimationListener ledUploadingAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            sUploading = false;
            updateLedUploading();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private void hideOnOff() {
        mTextOn.setVisibility(View.GONE);
        mTextOff.setVisibility(View.GONE);
    }

    private final Function1<GPSUpdate, Unit> onGPSUpdate = (GPSUpdate gpsUpdate) -> {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "onGPSUpdate " + gpsUpdate.toString());
        }
        runOnUiThread(this::updateLedGpsNew);
        return Unit.INSTANCE;
    };

    private void updateLedGpsNew() {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "updateLedGpsNew " + mLedGpsNewAnimated);
        }
        if (mLedGpsNewAnimated)
            return;
        mLedGpsNew.setVisibility(View.VISIBLE);
        mLedGpsNew.setAlpha(1.0f);
        mLedGpsNewAnimated = true;
        mLedGpsNew.startAnimation(mLedGpsNewAnimationFadeOut);
    }

    private void fadeOutUploading() {
        mLedUploadUploading.startAnimation(mLedUploadingAnimationFadeOut);
    }

    private final IUploadingLocationsStatusChangeListener onUploadStatusChanged = status -> {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "onUploadStatusChanged " + status.toString());
        }
        runOnUiThread(this::updateLedUploading);
    };

    private void updateLedUploading() {
        UploadingLocationsStatus status = mWTClient.wtClient.getUploadingLocationsStatus();
        if (BuildConfig.DEBUG) {
            Log.d(LT, "updateLedUploading " + sUploading + " " + status.toString());
        }
        switch (status) {
            case QUEUED:
                if (sUploading) {
                    fadeOutUploading();
                } else {
                    mLedUploadQueue.setVisibility(View.VISIBLE);
                    mLedUploadUploading.setVisibility(View.GONE);
                    mLedUploadError.setVisibility(View.GONE);
                }
                break;
            case UPLOADING:
                mLedUploadQueue.setVisibility(View.GONE);
                mLedUploadUploading.setVisibility(View.VISIBLE);
                mLedUploadError.setVisibility(View.GONE);
                sUploading = true;
                break;
            case ERROR:
                if (sUploading) {
                    fadeOutUploading();
                } else {
                    mLedUploadError.setVisibility(View.VISIBLE);
                }
                break;
            case EMPTY:
                if (sUploading) {
                    fadeOutUploading();
                } else {
                    mLedUploadQueue.setVisibility(View.GONE);
                    mLedUploadUploading.setVisibility(View.GONE);
                    mLedUploadError.setVisibility(View.GONE);
                }
        }
    }

    private void updateSound() {
        if (mSound.isOn()) {
            mBtnSoundOn.setVisibility(View.VISIBLE);
            mBtnSoundOff.setVisibility(View.GONE);
        } else {
            mBtnSoundOff.setVisibility(View.VISIBLE);
            mBtnSoundOn.setVisibility(View.GONE);
        }
    }

    private void startTracking() {
        mWTClient.turnTrackingOn();
    }

    private void stopTracking() {
        mWTClient.turnTrackingOff();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        GPSPermissionManager.handleOnRequestPermissionsResult(
                requestCode,
                mWTClient.gpsUpdatesManager,
                mWTClient.isTrackingOn());
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkNotificationPermission();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }
}
