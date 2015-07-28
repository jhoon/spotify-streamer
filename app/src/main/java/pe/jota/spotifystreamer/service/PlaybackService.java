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

    // MediaPlayer that will be used through all operations
    private MediaPlayer mMediaPlayer = null;
    private ArrayList<Track> mTrackList = null;
    private int mPosition;
    PlaybackCallbacks playerFragment;

    private final IBinder playbackBind = new PlaybackBinder();

    public PlaybackService(){}

    @Override
    public void onCreate() {
        super.onCreate();
        mPosition = 0;
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    /**
     * Initializes the Music Player
     */
    public void initMusicPlayer() {
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    /**
     * Updates the list of tracks to be used in the player
     * @param trackList the list of tracks that will replace the current one in the player
     */
    public void setTrackList(ArrayList<Track> trackList) {
        mTrackList = trackList;
    }

    /**
     * Sets the current track to be played
     * @param trackIndex the zero-based index of the song to be played. Plays nice
     *                   with the position from a ListView
     */
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

    /**
     * Starts playback of the song, given that a Position and a Track List have been registered
     */
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

    /**
     * Method to obtain the track being played
     * @return the track that is currently being played
     */
    public Track currentSong() {
        return mTrackList.get(mPosition);
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if ( mTrackList.size()-1 > mPosition) {
            playNext();
        } else {
            mp.stop();
        }
    }

    /**
     * Starts playback of the next song in the Track list. If the track is the last one,
     * nothing will happen.
     * @return the track that will start playing
     */
    public Track playNext() {
        mPosition++;
        if (mPosition < mTrackList.size()) {
            playSong();
        } else {
            mPosition--;
        }
        return mTrackList.get(mPosition);
    }

    /**
     * Starts playback of the previous song to the current one in the Track List. If the track
     * is the first one, nothing will happen.
     * @return the track that will start playing
     */
    public Track playPrevious() {
        mPosition--;
        if (mPosition >= 0) {
            playSong();
        } else {
            mPosition++;
        }
        return mTrackList.get(mPosition);
    }

    /**
     * Plays or pauses the song, relying on the current state of the MediaPlayer.
     */
    public void playPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }

    /**
     * Exposes the state of the isPlaying() method of the MediaPlayer
     * @return the isPlaying() state of the MediaPlayer instance in the service
     */
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    /**
     * Sets the current fragment in order to create a communication between this service
     * and the fragment that calls it
     * @param playerFragment the fragment (that implements PlaybackCallbacks) which is connecting
     *                       to this service.
     */
    public void registerClient(PlaybackCallbacks playerFragment) {
        this.playerFragment = playerFragment;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        if (playerFragment != null) {
            playerFragment.startPlaying(mp.getDuration(), mp.getCurrentPosition());
        }
    }

    /**
     * Binder Class used for the interaction between the app and the service
     */
    public class PlaybackBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    public interface PlaybackCallbacks {
        void startPlaying(int trackDuration, int start);
    }
}
