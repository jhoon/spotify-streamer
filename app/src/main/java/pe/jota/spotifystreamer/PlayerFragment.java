package pe.jota.spotifystreamer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Track;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment {
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    static final String ARG_TRACK_ID = "track_id";
    private Track mTrack;
    private MediaPlayer mediaPlayer;


    static PlayerFragment newInstance(String trackId){
        PlayerFragment playerFragment = new PlayerFragment();

        // Setting the track id
        Bundle args = new Bundle();
        args.putString(ARG_TRACK_ID, trackId);
        playerFragment.setArguments(args);

        return playerFragment;
    }

    public PlayerFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        String trackId = getArguments().getString(ARG_TRACK_ID);

        mTrack = ((StreamerApp)getActivity().getApplication()).getTrackForPlayback(trackId);

        TextView txtSong = (TextView)rootView.findViewById(R.id.txtSong);
        txtSong.setText(mTrack.name);

        TextView txtArtist = (TextView)rootView.findViewById(R.id.txtArtist);
        txtArtist.setText(mTrack.artists.get(0).name);

        TextView txtAlbum = (TextView)rootView.findViewById(R.id.txtAlbum);
        txtAlbum.setText(mTrack.album.name);

        ImageView imgAlbum = (ImageView)rootView.findViewById(R.id.imgAlbum);
        if (mTrack.album.images.size() > 0) {
            String url = mTrack.album.images.get(0).url;
            Picasso.with(getActivity())
                    .load(url)
                    .resizeDimen(R.dimen.album_image_width, R.dimen.album_image_height)
                    .centerCrop()
                    .into(imgAlbum);
        }

        preparePlayback();

        return rootView;
    }

    void preparePlayback() {
        try {
            if (mTrack != null){
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }

                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(mTrack.preview_url);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
                mediaPlayer.prepareAsync();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error when starting  playback", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // TODO: Stop Playback when exiting the dialog
    }
}
