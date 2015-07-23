package pe.jota.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = PlaybackService.class.getSimpleName();

    // Actions defined for this Service
    private static final String ACTION_PLAY = "pe.jota.spotifystreamer.service.action.PLAY";
    private static final String ACTION_STOP = "pe.jota.spotifystreamer.service.action.STOP";
    private static final String ACTION_PAUSE = "pe.jota.spotifystreamer.service.action.PAUSE";
    private static final String ACTION_NEXT = "pe.jota.spotifystreamer.service.action.NEXT";
    private static final String ACTION_PREVIOUS = "pe.jota.spotifystreamer.service.action.PREVIOUS";

    // Parameters needed for this service intents:
    private static final String EXTRA_TRACK_ID = "pe.jota.spotifystreamer.service.extra.TRACK_ID";

    // MediaPlayer that will be used through all operations
    private MediaPlayer mMediaPlayer = null;
    private ArrayList<Track> mTrackList = null;
    private int mPosition;

    private final IBinder playbackBind = new PlaybackBinder();

    public PlaybackService(){}

    @Override
    public void onCreate() {
        super.onCreate();
        mPosition = 0;
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void setTrackList(ArrayList<Track> trackList) {
        mTrackList = trackList;
    }

    public void setTrack(int trackIndex) {
        mPosition = trackIndex;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*String action = intent.getAction();
        String songID= intent.getStringExtra(EXTRA_TRACK_ID);

        switch(action) {
            case ACTION_PLAY:
            case ACTION_STOP:
            case ACTION_PAUSE:
            case ACTION_NEXT:
            case ACTION_PREVIOUS:
            default:
        }*/

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return playbackBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    private void preparePlayback() {
        try {
            if (mTrackList == null) {
                throw new IOException("TrackList not present");
            }
            if (mTrackList.get(mPosition) == null) {
                throw new IOException("Track not found");
            }

            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(mTrackList.get(mPosition).preview_url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error starting Playback", e);
        }
    }

    public void playSong() {
        mMediaPlayer.reset();
        Track songPlay = mTrackList.get(mPosition);
        try {
            mMediaPlayer.setDataSource(songPlay.preview_url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error setting the data source", e);
        }
        mMediaPlayer.prepareAsync();
    }

    public Track getTrackFromList(String id) {
        Track result = null;

        for (Track track:mTrackList) {
            if(track.id.equals(id)) {
                result = track;
                break;
            }
        }

        return result;
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    /**
     * Binder Class used for the interaction between the app and the service
     */
    public class PlaybackBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }
}
