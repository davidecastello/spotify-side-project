package io.moku.davide.spotify_side_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hanks.htextview.base.HTextView;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.moku.davide.spotify_side_project.network.NetworkManager;
import io.moku.davide.spotify_side_project.utils.preferences.PreferencesManager;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import retrofit.client.Response;

public class MainActivity extends Activity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    /* Constants */
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String CLIENT_ID = Constants.CLIENT_ID;
    private static final String REDIRECT_URI = "spotifySideProject://callback";
    // Request code that will be used to verify if the result comes from correct activity
    private static final int REQUEST_CODE = 1337;
    public static final int SECONDS = 1000;

    /* UI */
    @BindView(R.id.playButton) ImageButton playButton;
    @BindView(R.id.prevButton) ImageButton prevButton;
    @BindView(R.id.nextButton) ImageButton nextButton;
    @BindView(R.id.savedTracksRV) RecyclerView savedTracksRV;
    @BindView(R.id.homepageTitle) HTextView title_part_1;
    @BindView(R.id.homepageTitle2) HTextView title_part_2;
    @BindViews({R.id.playerLayout, R.id.line2}) List<View> playerViews;
    @BindView(R.id.bottom_navigation) BottomNavigationView bottomNavigationView;

    /* Fields */
    private Player mPlayer;
    private SavedTracksAdapter savedTracksAdapter;
    private boolean isPlaying = false;
    private boolean isPlayerVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // show title
        showPartialTitle();

        // disable player
        enablePlayer(false);

        // setup BottomNavigationView
        setupBottomNavigationView();

        String accessToken = PreferencesManager.getAccessToken(this);
        if (accessToken != null) {
            setupPlayer(accessToken);
        } else {
            openLogin();
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }



    /**
     *
     * UI
     *
     */

    public void showPartialTitle() {
        title_part_1.animateText(getString(R.string.homepage_title_part_1));
        new CountDownTimer(3 * SECONDS, SECONDS) {
            @Override
            public void onTick(long millisUntilFinished) {
                //
            }

            @Override
            public void onFinish() {
                showFullTitle();
            }
        }.start();
    }

    public void showFullTitle() {
        title_part_2.animateText(getString(R.string.homepage_title_part_2));
    }

    private void enablePlayer(boolean enable) {
        enableButton(playButton, enable);
        enableButton(prevButton, enable);
        enableButton(nextButton, enable);
    }

    private void enableButton(ImageButton button, boolean enable) {
        button.setEnabled(enable);
        button.setAlpha(enable ? 1.0f : 0.3f);
    }

    @OnClick({R.id.playButton, R.id.prevButton, R.id.nextButton})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playButton:
                playButtonPressed();
                break;
            case R.id.prevButton:
                playSong(savedTracksAdapter.prevSong().track.uri);
                break;
            case R.id.nextButton:
                playSong(savedTracksAdapter.nextSong().track.uri);
            default:
                break;
        }
    }

    public void showPlayer() {
        if (!isPlayerVisible) {
            for (View view : playerViews) {
                view.setVisibility(View.VISIBLE);
            }
            isPlayerVisible = true;
        }
    }

    public void setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.itemTracks:
                        // code
                        break;
                    case R.id.itemAlbum:
                        // code
                        break;
                    case R.id.itemPlaylist:
                        // code
                        break;
                }
                return true;
            }
        });
    }

    /**
     *
     * NETWORK
     *
     */

    public void tryToRetrieveSavedTracks() {
        HashMap<String, Object> options = new HashMap<>();
        options.put("limit", 20);
        NetworkManager.getService(this).getMySavedTracks(options, new SpotifyCallback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                savedTracksDownloaded(savedTrackPager.items);
            }

            @Override
            public void failure(SpotifyError error) {
                handleNetworkError(error);
            }
        });
    }

    public void savedTracksDownloaded(List<SavedTrack> savedTracks) {
        savedTracksAdapter = new SavedTracksAdapter(this, savedTracks);
        savedTracksRV.setLayoutManager(new LinearLayoutManager(this, LinearLayout.VERTICAL, false));
        savedTracksRV.setAdapter(savedTracksAdapter);
        enablePlayer(true);
    }

    public void handleNetworkError(SpotifyError error) {
        Log.e(TAG, error.getMessage());
        if (error.hasErrorDetails() && error.getErrorDetails().status == 401) {
            openLogin();
        }
    }


    /**
     *
     * PLAYER
     *
     */

    public void playButtonPressed() {
        if (isPlaying) {
            playButton.setImageDrawable(getDrawable(R.drawable.ic_play_circle));
            pause();
        } else {
            playButton.setImageDrawable(getDrawable(R.drawable.ic_pause_circle));
            resumeOrPlay();
        }
        isPlaying = !isPlaying;
    }

    public void playSong(String uri) {
        if (!isPlaying) {
            playButton.setImageDrawable(getDrawable(R.drawable.ic_pause_circle));
            isPlaying = !isPlaying;
        }
        _playSong(uri);
    }

    public void resumeOrPlay() {
        mPlayer.resume(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                if(!mPlayer.getPlaybackState().isPlaying) {
                    playSong();
                }
            }

            @Override
            public void onError(Error error) {
                Toast.makeText(MainActivity.this, "Error while trying to resume!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void playSong() {
        _playSong(savedTracksAdapter.nextSong().track.uri);
    }

    private void _playSong(String uri) {
        // show the player
        showPlayer();
        // This is the line that actually plays a song.
        mPlayer.playUri(null, uri, 0, 0);
    }

    public void pause() {
        mPlayer.pause(null);
    }

    private void setupPlayer(String accessToken) {
        // retrieve saved tracks
        tryToRetrieveSavedTracks();
        // retrieve player
        Config playerConfig = new Config(this, accessToken, CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                mPlayer = spotifyPlayer;
                mPlayer.addConnectionStateCallback(MainActivity.this);
                mPlayer.addNotificationCallback(MainActivity.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }


    /**
     *
     * LOGIN
     *
     */

    private void openLogin() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "user-library-read", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                String accessToken = response.getAccessToken();
                PreferencesManager.storeAccessToken(this, accessToken);
                setupPlayer(accessToken);
            }
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error var1) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }


    /**
     *
     * SPOTIFY CALLBACKS
     *
     */

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            case kSpPlaybackNotifyTrackDelivered:
                playButtonPressed();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

}