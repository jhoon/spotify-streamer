package pe.jota.spotifystreamer;

import android.app.Application;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by jhoon on 7/17/15.
 */
public class StreamerApp extends Application {
    private ArrayList<Track> trackList = null;
    private ArrayList<Artist> artistList = null;

    protected void setTrackList(ArrayList<Track> trackList) {
        this.trackList = trackList;
    }

    protected void setArtistList(ArrayList<Artist> artistList) {
        this.artistList = artistList;
    }

    protected ArrayList<Track> getTrackList() {
        return trackList;
    }

    protected ArrayList<Artist> getArtistList() {
        return artistList;
    }

    protected Track getSelectedTrack(String id){
        Track forPlayback = null;

        for (Track track:trackList){
            if (track.id.equals(id)) {
                forPlayback = track;
                break;
            }
        }

        return forPlayback;
    }
}
