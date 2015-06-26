package pe.jota.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;
import pe.jota.spotifystreamer.R;

/**
 * Created by jhoon on 6/26/15.
 */
public class ArtistsAdapter extends ArrayAdapter<Artist>{
    private final static String LOG_TAG = ArtistsAdapter.class.getSimpleName();

    public ArtistsAdapter(Context context, ArrayList<Artist> artists) {
        super(context, 0, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Artist artist = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
        }

        TextView tvTitle = (TextView)convertView.findViewById(R.id.txtMainTitle);
        tvTitle.setText(artist.name);

        return convertView;
    }
}
