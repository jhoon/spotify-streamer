package pe.jota.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;
import pe.jota.spotifystreamer.R;

/**
 * Created by jhoon on 6/26/15.
 */
public class TopTracksAdapter extends ArrayAdapter<Track> {
    private static final String LOG_TAG = TopTracksAdapter.class.getSimpleName();

    public TopTracksAdapter(Context context, ArrayList<Track> tracks) {
        super(context, 0, tracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Track track = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_generic, parent, false);
            viewHolder.albumImage = (ImageView)convertView.findViewById(R.id.imgImage);
            viewHolder.trackName = (TextView)convertView.findViewById(R.id.tvMainTitle);
            viewHolder.albumName = (TextView)convertView.findViewById(R.id.tvSubTitle);
            viewHolder.albumImage.setVisibility(View.VISIBLE);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.trackName.setText(track.name);
        viewHolder.albumName.setText(track.album.name);

        return convertView;
    }

    private static class ViewHolder {
        ImageView albumImage;
        TextView trackName;
        TextView albumName;
    }
}
