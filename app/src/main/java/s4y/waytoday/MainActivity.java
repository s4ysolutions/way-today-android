package s4y.waytoday;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import s4y.waytoday.background.BackgroundService;
import s4y.waytoday.errors.ErrorsObservable;
import s4y.waytoday.idservice.IDService;
import s4y.waytoday.mainactivity.FrequencyGestureListener;
import s4y.waytoday.permissions.PermissionRequest;
import s4y.waytoday.permissions.PermissionRequestObservable;
import s4y.waytoday.preferences.PreferenceIsTracking;
import s4y.waytoday.preferences.PreferenceSound;
import s4y.waytoday.preferences.PreferenceTrackID;
import s4y.waytoday.preferences.PreferenceUpdateFrequency;
import s4y.waytoday.sound.MediaPlayerUtils;
import s4y.waytoday.upload.UploadJobService;

import static s4y.waytoday.upload.UploadJobService.uploadStatus;

public class MainActivity extends AppCompatActivity {
    private final static String LT = MainActivity.class.getSimpleName();
    @Inject
    PreferenceUpdateFrequency mUserStrategyFrequency;
    public static boolean sHasFocus = false;
    @Inject
    PreferenceIsTracking mIsActive;
    @Inject
    PreferenceTrackID mTrackID;
    @Inject
    PreferenceSound mSound;

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
    @BindView(R.id.textID)
    TextView mTextID;
    @BindView(R.id.btn_track_id)
    ImageView mBtnTrackID;
    @BindView(R.id.status_gps_wait)
    ImageView mLedGpsWait;
    @BindView(R.id.status_gps_new)
    ImageView mLedGpsNew;
    @BindView(R.id.status_upload_empty)
    ImageView mLedUploadEmpty;
    @BindView(R.id.status_upload_queue)
    ImageView mLedUploadQueue;
    @BindView(R.id.status_upload_uploading)
    ImageView mLedUploadUploading;
    @BindView(R.id.status_upload_error)
    ImageView mLedUploadError;
    @BindView(R.id.btn_sound_on)
    ImageView mBtnSoundOn;
    @BindView(R.id.btn_sound_off)
    ImageView mBtnSoundOff;
    @BindView(R.id.text_on)
    TextView mTextOn;
    @BindView(R.id.text_off)
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
    SparseArray<PermissionRequest> mPermissionRequests = new SparseArray<>(2);
    private BackgroundService mBackgroundService;

    @NonNull
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       @NonNull IBinder binder) {
            setBinder(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBackgroundService = null;
            updateLedBackground();
        }
    };

    // for mocking purposes
    protected void setBinder(IBinder binder) {
        mBackgroundService = ((BackgroundService.LocationsServiceBinder) binder).getService();
        if (sHasFocus)
            mBackgroundService.removeFromForeground();
        else
            mBackgroundService.putInForeground();
        updateLedBackground();
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
    }

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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userStrategy -> updateUserStrategyChooser()));
        resumeDisposables.add(mIsActive
                .subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userStrategy -> updateSwitch()));
        resumeDisposables.add(BackgroundService.sensorGPS
                .subjectTrackingState
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(state -> updateLedBackground()));
        resumeDisposables.add(BackgroundService.sensorGPS
                .subjectGPS
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(state -> updateLedGpsNew()));
        resumeDisposables.add(PermissionRequestObservable
                .subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onPermissionRequest));
        resumeDisposables.add(UploadJobService
                .subjectStatus
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> this.updateLedUploading()));
        resumeDisposables.add(mTrackID
                .subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(trackID -> {
                    updateTrackID();
                    MediaPlayerUtils.getInstance(this).playTrackID(this);
                }));
        resumeDisposables.add(ErrorsObservable
                .subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignored -> updateAllViews()));
        if (mTrackID.isNotSet()) {
            IDService.enqueueRetrieveId(this, "");
        }
        updateAllViews();
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

    private Animation.AnimationListener textOnOffAnimationListener = new Animation.AnimationListener() {
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

    private boolean mLedGpsNewAnymated = false;
    private static final float alphaIDinProgress = 0.3f;
    private static final float alphaIDnotInProgress = 0.9f;

    @OnClick(R.id.switch_on)
    public void switchOn(View view) {
        if (isSwitching) return;
        startService();
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
                mIsActive.set(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTextOff.setVisibility(View.GONE);
        mTextOn.setVisibility(View.VISIBLE);
        mTextOn.setAlpha(1);
        mTextOn.startAnimation(mTextOnAnimationFadeOut);
        WTApplication.faOn();
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

    private void updateLedBackground() {
        if (mBackgroundService == null) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "updateLedBackground: mBackgroundService=null");
            }
            mLedTrackingOff.setVisibility(View.GONE);
            mLedTrackingUnknown.setVisibility(View.VISIBLE);
            mLedTrackingSuspended.setVisibility(View.GONE);
            mLedTrackingOn.setVisibility(View.GONE);
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(LT,
                        String.format("updateLedBackground: mBackgroundService!=null, isUpdating=%b, isSuspended=%b",
                                BackgroundService.sensorGPS.isUpdating,
                                BackgroundService.sensorGPS.isSuspended
                        ));
            }
            if (BackgroundService.sensorGPS.isUpdating) {
                if (BackgroundService.sensorGPS.isSuspended) {
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

    @OnClick(R.id.switch_off)
    public void switchOff(View view) {
        if (isSwitching) return;
        stopService();
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
                mIsActive.set(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTextOff.setVisibility(View.VISIBLE);
        mTextOff.setAlpha(1);
        mTextOff.startAnimation(mTextOffAnimationFadeOut);
        WTApplication.faOff();
    }

    private void updateAllViews() {
        updateUserStrategyChooser();
        updateSwitch();
        updateLedBackground();
        updateTrackID();
        updateLedUploading();
        updateSound();
        hideOnOff();
    }

    @OnClick(R.id.btn_track_id)
    public void newTrackID(View view) {
        mBtnTrackID.setAlpha(alphaIDinProgress);
        mTextID.setAlpha(alphaIDinProgress);
        IDService.enqueueRetrieveId(this, mTrackID.get());
        WTApplication.faRequestID();
    }

    private void updateTrackID() {
        float alpha = (IDService.isProgress() && !IDService.sFailed)
                ? alphaIDinProgress : alphaIDnotInProgress;
        mBtnTrackID.setAlpha(alpha);
        mTextID.setAlpha(alpha);
        if (BuildConfig.DEBUG) {
            Log.d(LT,
                    "updateTrackID id=\"" + mTrackID.get() + "\" isSet=" + mTrackID.isSet() +
                            " failed=" + IDService.sFailed + " inProgress=" + IDService.isProgress() +
                            " alpha=" + alpha);
        }
        if (mTrackID.isSet()) {
            mTextID.setText(mTrackID.get());
        } else {
            if (IDService.sFailed) {
                mTextID.setText(R.string.id_request_failed);
            } else {
                mTextID.setText(" ... ");
            }
        }
    }

    private Animation.AnimationListener ledGpsNewAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mLedGpsNewAnymated = false;
            mLedGpsNew.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    private boolean sUploading;
    private Animation.AnimationListener ledUploadingAnimationListener = new Animation.AnimationListener() {
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

    private void updateLedGpsNew() {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "updateLedGpsNew " + mLedGpsNewAnymated);
        }
        if (mLedGpsNewAnymated)
            return;
        mLedGpsNew.setVisibility(View.VISIBLE);
        mLedGpsNew.setAlpha(1.0f);
        mLedGpsNewAnymated = true;
        mLedGpsNew.startAnimation(mLedGpsNewAnimationFadeOut);
    }

    private void fadeOutUploading() {
        mLedUploadUploading.startAnimation(mLedUploadingAnimationFadeOut);
    }

    private void updateLedUploading() {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "updateLedUploading " + sUploading + " " + uploadStatus().toString());
        }
        switch (uploadStatus()) {
            case QUEUED:
                if (sUploading) {
                    fadeOutUploading();
                } else {
                    mLedUploadQueue.setVisibility(View.VISIBLE);
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
            default:
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

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_sound_on)
    void onSoundOnClick(View v) {
        mSound.set(false);
        updateSound();
        WTApplication.faSoundOn();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_sound_off)
    void onSoundOffClick(View v) {
        mSound.set(true);
        updateSound();
        WTApplication.faSoundOff();
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
                    mPermissionRequests.put(code, request);
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, code);
                    WTApplication.faNoPermission();
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

    @OnClick(R.id.btn_share)
    public void onShareBtnClick(View v) {
        if (mTrackID.isNotSet()) return;
        String txt = String.format(getResources().getString(R.string.share_link), mTrackID.get());
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, txt);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subj));
        // sendIntent.setType("message/rfc822");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.share_title)));
        WTApplication.faShare();
    }

    @OnClick(R.id.btn_way_today)
    public void onWayToday(View v) {
        if (mTrackID.isNotSet()) return;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://way.today/#" + mTrackID.get()));
        startActivity(browserIntent);
        WTApplication.faVisit();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }
}
