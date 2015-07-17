package pe.jota.spotifystreamer;

import android.app.Application;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by jhoon on 7/17/15.
 */
public class StreamerApp extends Application {
    private ArrayList<Track> trackList = null;

    protected void setTrackList(ArrayList<Track> trackList) {
        this.trackList = trackList;
    }

    protected Track getTrackForPlayback(String id){
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
