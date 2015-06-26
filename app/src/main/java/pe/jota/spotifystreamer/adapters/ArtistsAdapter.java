package pe.jota.spotifystreamer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
            convertView = inflater.inflate(R.layout.list_item_generic, parent, false);
            viewHolder.artistImage = (ImageView)convertView.findViewById(R.id.imgImage);
            viewHolder.artistName = (TextView)convertView.findViewById(R.id.tvMainTitle);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if (viewHolder.artistImage.getTag() != null) {

        }
        if (artist.images.size() > 0){
            String url = artist.images.get(0).url;
            Log.d(LOG_TAG, url);
            Picasso.with(getContext())
                    .load(url)
                    .placeholder(R.mipmap.ic_launcher)
                    .resizeDimen(R.dimen.image_width, R.dimen.image_height)
                    .centerCrop()
                    .into(viewHolder.artistImage);
        }

        viewHolder.artistName.setText(artist.name);

        return convertView;
    }

    private static class ViewHolder {
        ImageView artistImage;
        TextView artistName;
    }
}
