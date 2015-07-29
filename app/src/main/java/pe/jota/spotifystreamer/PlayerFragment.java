package pe.jota.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.models.Track;
import pe.jota.spotifystreamer.service.PlaybackService;
import pe.jota.spotifystreamer.service.PlaybackService.PlaybackBinder;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment implements PlaybackService.PlaybackCallbacks {
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    /**
     * The current progress, useful when restoring the fragment from rotation
     */
    static final String CURRENT_PROGRESS = "current_progress";
    /**
     * The Track id this fragment is presenting.
     */
    static final String ARG_TRACK_ID = "track_id";
    /**
     * The Track position that this fragment is presenting.
     */
    static final String ARG_TRACK_POSITION = "track_pos";
    private Track mTrack;
    private int mPosition;

    /**
     * The playback Service for playing songs
     */
    private PlaybackService playbackService;

    /**
     * The playback intent used to interact with the service
     */
    private Intent playbackIntent;

    /**
     * Flag to know if the service is bound
     */
    private boolean playbackBound = false;

    /**
     * Flag to indicate whether playback should continue or a new song should be set
     */
    private boolean continuePlayback = false;

    /**
     * Handler to update the seekBar
     */
    private Handler mHandler = new Handler();

    // Controls used in the Fragment UI
    private TextView txtSong;
    private TextView txtArtist;
    private TextView txtAlbum;
    private TextView txtStartTime;
    private TextView txtEndTime;
    private ImageView imgAlbum;
    private ImageButton btnPrevious;
    private ImageButton btnPlayPause;
    private ImageButton btnNext;
    private SeekBar seekSong;

    // Runnable to perform progress in the seekBar
    Runnable mProgress = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(this, 1000);
            seekSong.setProgress(seekSong.getProgress() + 1000);
            txtStartTime.setText(formatProgress(seekSong.getProgress()));
        }
    };

    /**
     * Method to obtain a new fragment with the corresponding trackID. Particularly
     * useful when creating a Fragment to be shown in a Dialog.
     * @param trackId parameter to set the Track ID shown in this fragment
     * @return a new instance of the playerFragment with the Track ID correctly set.
     */
    static PlayerFragment newInstance(String trackId, int trackPosition){
        PlayerFragment playerFragment = new PlayerFragment();

        // Setting the track id
        Bundle args = new Bundle();
        args.putString(ARG_TRACK_ID, trackId);
        args.putInt(ARG_TRACK_POSITION, trackPosition);
        playerFragment.setArguments(args);

        return playerFragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerFragment() {

    }

    /**
     * Service connection for controlling the PlaybackService
     */
    private ServiceConnection playbackConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackBinder binder = (PlaybackBinder)service;
            ArrayList<Track> trackList = ((StreamerApp)getActivity().getApplication()).getTrackList();
            //  obtaining the service
            playbackService = binder.getService();
            // setting the list of songs in the service
            playbackService.setTrackList(trackList);
            // setting this fragment as the client
            playbackService.registerClient(PlayerFragment.this);
            playbackBound = true;

            if (continuePlayback) {
                mTrack = playbackService.currentSong();
                showSongData();
            } else {
                playbackService.setTrack(mPosition);
                playbackService.playSong();
            }
            updatePlayPauseButton();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playbackBound = false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(null == playbackIntent) {
            playbackIntent = new Intent(getActivity(), PlaybackService.class);
            getActivity().bindService(playbackIntent, playbackConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playbackIntent);
        }

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        String trackId = getArguments().getString(ARG_TRACK_ID);

        mPosition =  getArguments().getInt(ARG_TRACK_POSITION);
        mTrack = ((StreamerApp)getActivity().getApplication()).getSelectedTrack(trackId);

        txtSong = (TextView)rootView.findViewById(R.id.txtSong);
        txtArtist = (TextView)rootView.findViewById(R.id.txtArtist);
        txtAlbum = (TextView)rootView.findViewById(R.id.txtAlbum);
        txtStartTime = (TextView)rootView.findViewById(R.id.txtStartTime);
        txtEndTime = (TextView)rootView.findViewById(R.id.txtEndTime);
        imgAlbum = (ImageView)rootView.findViewById(R.id.imgAlbum);
        btnPrevious = (ImageButton)rootView.findViewById(R.id.btnPrevious);
        btnPlayPause = (ImageButton)rootView.findViewById(R.id.btnPlayPause);
        btnNext = (ImageButton)rootView.findViewById(R.id.btnNext);
        seekSong = (SeekBar)rootView.findViewById(R.id.seekSong);

        if (savedInstanceState != null) {
            continuePlayback = true;
        }

        showSongData();
        updatePlayPauseButton();
        setListeners();

        return rootView;
    }

    /**
     * Shows all the data from the current song in the View
     */
    private void showSongData() {
        txtSong.setText(mTrack.name);
        txtArtist.setText(mTrack.artists.get(0).name);
        txtAlbum.setText(mTrack.album.name);
        if (mTrack.album.images.size() > 0) {
            String url = mTrack.album.images.get(0).url;
            Picasso.with(getActivity())
                    .load(url)
                    .resizeDimen(R.dimen.album_image_width, R.dimen.album_image_height)
                    .centerCrop()
                    .into(imgAlbum);
        }
        if (playbackService != null) {
            seekSong.setMax(playbackService.getDuration());
            seekSong.setProgress(playbackService.getCurrentPosition());
            txtEndTime.setText(formatProgress(playbackService.getDuration()));
            txtStartTime.setText(formatProgress(playbackService.getCurrentPosition()));
        }
    }

    /**
     * Executes the Play / Pause action to the service, calls updatePlayPauseButton
     * to update the UI
     */
    private void playPause() {
        if (playbackService != null && playbackBound) {
            playbackService.playPause();
            updatePlayPauseButton();
        } else {
            Log.e(LOG_TAG, "PlaybackService is not bound yet");
        }
    }

    /**
     * In charge of updating the UI button for Play/Pause
     */
    private void updatePlayPauseButton() {
        if (playbackService != null && playbackBound) {
            if (playbackService.isPlaying()) {
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                mHandler.removeCallbacks(mProgress);
                mProgress.run();
            } else {
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                mHandler.removeCallbacks(mProgress);
            }
        } else {
            Log.e(LOG_TAG, "PlaybackService is not bound yet");
        }
    }

    /**
     * Setting the listeners for the buttons in the UI
     */
    private void setListeners() {
        // Setting the listeners for the buttons
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playbackService != null && playbackBound) {
                    mHandler.removeCallbacks(mProgress);
                    mTrack = playbackService.playNext();
                } else {
                    Log.e(LOG_TAG, "PlaybackService is not bound yet");
                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playbackService != null && playbackBound) {
                    mHandler.removeCallbacks(mProgress);
                    mTrack = playbackService.playPrevious();
                } else {
                    Log.e(LOG_TAG, "PlaybackService is not bound yet");
                }
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });

        seekSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && playbackBound && playbackService != null) {
                    mHandler.removeCallbacks(mProgress);
                    playbackService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * Returns a String that can be shown as the song progress or the Song Duration
     * @param progress the progress as received from the seekBar
     * @return a String that shows the progress in the format 0:00
     */
    private String formatProgress(int progress) {
        return  String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(progress),
                TimeUnit.MILLISECONDS.toSeconds(progress) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(playbackIntent);
//        getActivity().unbindService(playbackConnection);
        playbackService = null;
        super.onDestroy();
    }

    @Override
    public void startPlaying(int trackDuration, int start) {
        seekSong.setMax(trackDuration);
        txtEndTime.setText(formatProgress(trackDuration));
        seekSong.setProgress(start);
        mTrack = playbackService.currentSong();
        showSongData();
        updatePlayPauseButton();
    }

    @Override
    public void setProgress(int progress) {
        Log.d(LOG_TAG, "progress: " + progress + " seekBarMax: " + seekSong.getMax());
        seekSong.setProgress(progress);
        txtStartTime.setText(formatProgress(progress));
        updatePlayPauseButton();
    }
}
