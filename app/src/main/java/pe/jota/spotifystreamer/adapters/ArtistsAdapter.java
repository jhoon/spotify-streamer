package pe.jota.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_artist, parent, false);
            viewHolder.artistImage = (ImageView)convertView.findViewById(R.id.imgImage);
            viewHolder.artistName = (TextView)convertView.findViewById(R.id.tvMainTitle);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.artistName.setText(artist.name);

        return convertView;
    }

    private static class ViewHolder {
        ImageView artistImage;
        TextView artistName;
    }
}
