package s4y.waytoday.sound;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import s4y.waytoday.errors.ErrorsObservable;
import s4y.waytoday.preferences.PreferenceSound;

import static android.content.Context.AUDIO_SERVICE;

public class MediaPlayerUtils {
    private static MediaPlayerUtils instance;
    //    private final MediaPlayer mPlayer;
    private final SoundPool mSoundPool;
    private final Set<Integer> played = new HashSet<>();
    private int streamSwitch;
    private int streamIdOk;
    private int streamGpsOk;
    private int streamUploadOk;
    private int streamUploadFail;
    static private PreferenceSound preferenceSound = null;

    public static void setPreferenceSound(PreferenceSound preferenceSound) {
        MediaPlayerUtils.preferenceSound = preferenceSound;
    }

    private MediaPlayerUtils(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(7)
                    .setAudioAttributes(new AudioAttributes
                            .Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .build();
        } else {
            mSoundPool = new SoundPool(7, AudioManager.STREAM_NOTIFICATION, 0);
        }

        try (AssetFileDescriptor afd = context.getAssets().openFd("switch.mp3")) {
            streamSwitch = mSoundPool.load(afd, 1);
        } catch (IOException e) {
            ErrorsObservable.notify(e, false);
        }

        try (AssetFileDescriptor afd = context.getAssets().openFd("idok.wav")) {
            streamIdOk = mSoundPool.load(afd, 1);
        } catch (IOException e) {
            ErrorsObservable.notify(e, false);
        }

        try (AssetFileDescriptor afd = context.getAssets().openFd("gpsok.wav")) {
            streamGpsOk = mSoundPool.load(afd, 1);
        } catch (IOException e) {
            ErrorsObservable.notify(e, false);
        }

        try (AssetFileDescriptor afd = context.getAssets().openFd("uploadok.mp3")) {
            streamUploadOk = mSoundPool.load(afd, 1);
        } catch (IOException e) {
            ErrorsObservable.notify(e, false);
        }

        try (AssetFileDescriptor afd = context.getAssets().openFd("uploadfail.mp3")) {
            streamUploadFail = mSoundPool.load(afd, 1);
        } catch (IOException e) {
            ErrorsObservable.notify(e, false);
        }
    }

    public static MediaPlayerUtils getInstance(Context context) {
        if (instance == null) {
            instance = new MediaPlayerUtils(context);
        }
        return instance;
    }

    private void stopAll() {
        while (!played.isEmpty()) {
            Integer id = played.iterator().next();
            mSoundPool.stop(id);
            played.remove(id);
        }
    }

    private void play(Context context, int streamID) {
        stopAll();
        if (preferenceSound == null)
            return;
        if (preferenceSound.isOff()) {
            return;
        }
        AudioManager am = (AudioManager) context.getSystemService(AUDIO_SERVICE);

        if (am == null)
            return;
        if (am.getMode() != AudioManager.MODE_NORMAL) {
            return;
        }

        played.add(mSoundPool.play(streamID, 1, 1, 1, 0, 1));
    }

    public void playSwitchSound(Context context) {
        play(context, streamSwitch);
    }

    public void playTrackID(Context context) {
        play(context, streamIdOk);
    }

    public void playGpsOk(Context context) {
        play(context, streamGpsOk);
    }

    public void playUploadOk(Context context) {
        play(context, streamUploadOk);
    }

    public void playUploadFail(Context context) {
        play(context, streamUploadFail);
    }

}
