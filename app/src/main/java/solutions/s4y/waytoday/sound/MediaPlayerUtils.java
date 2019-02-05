package solutions.s4y.waytoday.sound;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

import solutions.s4y.waytoday.errors.ErrorsObservable;

import static android.content.Context.AUDIO_SERVICE;

public class MediaPlayerUtils implements MediaPlayer.OnPreparedListener {
    private static MediaPlayerUtils instance;
    private final MediaPlayer mPlayer;

    private MediaPlayerUtils() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public static MediaPlayerUtils getInstance() {
        if (instance == null) {
            instance = new MediaPlayerUtils();
        }
        return instance;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    private void stop() {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayer.reset();
    }

    private void play(Context context, String file) {
        stop();
        AudioManager am = (AudioManager) context.getSystemService(AUDIO_SERVICE);

        if (am == null)
            return;
        if (am.getMode() != AudioManager.MODE_NORMAL) {
            return;
        }

        try (AssetFileDescriptor afd = context.getAssets().openFd(file)) {
            stop();
            mPlayer.setLooping(false);
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.prepareAsync();
        } catch (IOException e) {
            ErrorsObservable.notify(e, false);
        }
    }

    public void playSwitchSound(Context context) {
        play(context, "switch.mp3");
    }

    public void playTrackID(Context context) {
        play(context, "idok.wav");
    }
}
