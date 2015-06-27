package pe.jota.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import pe.jota.spotifystreamer.adapters.TopTracksAdapter;

/**
 * A fragment representing a single Artist detail screen.
 * This fragment is either contained in a {@link ArtistListActivity}
 * in two-pane mode (on tablets) or a {@link TopTracksActivity}
 * on handsets.
 */
public class TopTracksFragment extends ListFragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ARTIST_ID = "artist_id";
    public static final String ARG_ARTIST_NAME = "artist_name";

    /**
     * The Artist id this fragment is presenting.
     */
    private String mArtistId;
    private String mArtistName;
    private TopTracksAdapter mTracksAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ARTIST_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mArtistId = getArguments().getString(ARG_ARTIST_ID);
        } else {
            mArtistId = "";
        }

        if (getArguments().containsKey(ARG_ARTIST_NAME)) {
            mArtistName = getArguments().getString(ARG_ARTIST_NAME);
        } else {
            mArtistName = "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        // Show the dummy content as text in a TextView.
//        if (mItem != null) {
//            ((TextView) rootView.findViewById(R.id.artist_detail)).setText(mItem.content);
//        }
        new FetchTopTracksTask().execute(mArtistId);

        return rootView;
    }

    private class FetchTopTracksTask extends AsyncTask<String, Void, Tracks> {
        private final String LOG_TAG = FetchTopTracksTask.class.getSimpleName();

        @Override
        protected Tracks doInBackground(String... params) {
            String searchId = params[0];

            Log.d(LOG_TAG, "id of Artist: " + searchId);

            // Options for searching the top 10 tracks
            final Map<String, Object> options = new HashMap<String, Object>();
            options.put(SpotifyService.OFFSET, 0);
            options.put(SpotifyService.LIMIT, 10);
            options.put(SpotifyService.COUNTRY, "US");

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Tracks result = spotify.getArtistTopTrack(searchId, options);

            return result;
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);
            ArrayList<Track> trackList = new ArrayList<Track>(tracks.tracks);
            mTracksAdapter = new TopTracksAdapter(getActivity(), trackList);

            setListAdapter(mTracksAdapter);

            if (tracks.tracks.size() == 0) {
                Toast.makeText(getActivity(), R.string.no_tracks_found, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
