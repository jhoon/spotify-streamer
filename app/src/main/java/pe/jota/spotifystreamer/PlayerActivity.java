package pe.jota.spotifystreamer;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {
            // Create the Fragment for the player and adding it to the
            // activity using a fragment transaction, along with the track_id
            String trackId = getIntent().getStringExtra(PlayerFragment.ARG_TRACK_ID);

            PlayerFragment fragment = PlayerFragment.newInstance(trackId);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.player_container, fragment)
                    .commit();
        }
    }
}
