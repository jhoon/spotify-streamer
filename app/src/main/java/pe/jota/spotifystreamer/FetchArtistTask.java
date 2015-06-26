package pe.jota.spotifystreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Created by jhoon on 6/25/15.
 */
public class FetchArtistTask extends AsyncTask<String, Void, ArtistsPager> {

    private final String LOG_TAG = FetchArtistTask.class.getSimpleName();
    private final Context mContext;

    public FetchArtistTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArtistsPager doInBackground(String... params) {

        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        ArtistsPager result = spotify.searchArtists("Coldplay");

        // TODO: actually use a string from user input

        Log.d(LOG_TAG, result.toString());

        return result;
    }

    @Override
    protected void onPostExecute(ArtistsPager result) {
        super.onPostExecute(result);
    }
}
