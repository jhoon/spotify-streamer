package pe.jota.spotifystreamer;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;
import pe.jota.spotifystreamer.service.PlaybackService;
import pe.jota.spotifystreamer.service.PlaybackService.PlaybackBinder;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment {
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

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

    // Controls used in the Fragment UI
    private TextView txtSong;
    private TextView txtArtist;
    private TextView txtAlbum;
    private ImageView imgAlbum;
    private ImageButton btnPrevious;
    private ImageButton btnPlayPause;
    private ImageButton btnNext;

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
            playbackBound = true;
            if (!playbackService.isPlaying()) {
                playbackService.setTrack(mPosition);
                playbackService.playSong();
            } else {
                mTrack = playbackService.currentSong();
                showSongData();
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
        if (null == playbackIntent) {
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
        imgAlbum = (ImageView)rootView.findViewById(R.id.imgAlbum);
        btnPrevious = (ImageButton)rootView.findViewById(R.id.btnPrevious);
        btnPlayPause = (ImageButton)rootView.findViewById(R.id.btnPlayPause);
        btnNext = (ImageButton)rootView.findViewById(R.id.btnNext);

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
            } else {
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
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
                    mTrack = playbackService.playNext();
                    showSongData();
                } else {
                    Log.e(LOG_TAG, "PlaybackService is not bound yet");
                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playbackService != null && playbackBound) {
                    mTrack = playbackService.playPrevious();
                    showSongData();
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
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (playbackBound) {
            playbackService.setTrack(mPosition);
            playbackService.playSong();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(playbackIntent);
        playbackService = null;
        super.onDestroy();
    }
}
