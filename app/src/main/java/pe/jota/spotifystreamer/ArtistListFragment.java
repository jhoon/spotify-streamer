package pe.jota.spotifystreamer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import pe.jota.spotifystreamer.adapters.ArtistsAdapter;
import retrofit.RetrofitError;

/**
 * A list fragment representing a list of Songs. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TopTracksFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ArtistListFragment extends ListFragment {

    private static final String LOG_TAG = ArtistListFragment.class.getSimpleName();
    private EditText mTxtSearch;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = activityCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private static final String SEARCH_TERM = "search_term";

    private String mSearchTerm;
    private ArtistsAdapter mArtistsAdapter;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(Artist artist);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks activityCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Artist artist) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            mSearchTerm = savedInstanceState.getString(SEARCH_TERM);
        }
    }

    private void searchArtist(String searchText) {
        if (!searchText.trim().equals("")) {
            FetchArtistsTask fetchTask = new FetchArtistsTask();
            fetchTask.execute(searchText);
        } else {
            Toast.makeText(getActivity(), R.string.enter_artist_name, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_list, container, false);

        mTxtSearch = (EditText)rootView.findViewById(R.id.txtSearch);
        mTxtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mSearchTerm = mTxtSearch.getText().toString();

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchArtist(mSearchTerm);
                    return true;
                }
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null &&
                ((StreamerApp)getActivity().getApplication()).getArtistList() != null) {
            mArtistsAdapter = new ArtistsAdapter(getActivity(),
                    ((StreamerApp)getActivity().getApplication()).getArtistList());
            setListAdapter(mArtistsAdapter);
        }

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = activityCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        Artist selectedArtist = (Artist)getListAdapter().getItem(position);

        setActivatedPosition(position);

        Log.d(LOG_TAG, "selected: " + selectedArtist.name);
        mCallbacks.onItemSelected(selectedArtist);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSearchTerm != null && !mSearchTerm.equals("")) {
            outState.putString(ArtistListFragment.SEARCH_TERM, mSearchTerm);
        }

        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private class FetchArtistsTask extends AsyncTask<String, Void, ArtistsPager> {
        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        @Override
        protected ArtistsPager doInBackground(String... params) {
            String searchText = params[0];

            Log.d(LOG_TAG, "searchTerm" + searchText);

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager result;
            try {
                result = spotify.searchArtists(searchText);
            } catch (RetrofitError e){
                Log.e(LOG_TAG, e.getMessage(), e);
                result = null;
            }

            Log.d(LOG_TAG, result != null ? result.toString() : "No results retrieved");

            return result;
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            super.onPostExecute(artistsPager);

            if (artistsPager != null) {
                ArrayList<Artist> artists = new ArrayList<>(artistsPager.artists.items);
                mArtistsAdapter = new ArtistsAdapter(getActivity(), artists);
                ((StreamerApp)getActivity().getApplication()).setArtistList(artists);

                setListAdapter(mArtistsAdapter);

                if (artistsPager.artists.total == 0) {
                    Toast.makeText(getActivity(), R.string.no_artists_found, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.error_getting_artists, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
