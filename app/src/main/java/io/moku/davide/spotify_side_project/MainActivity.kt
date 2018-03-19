package io.moku.davide.spotify_side_project

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast

import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import com.spotify.sdk.android.player.Config
import com.spotify.sdk.android.player.ConnectionStateCallback
import com.spotify.sdk.android.player.Error
import com.spotify.sdk.android.player.Player
import com.spotify.sdk.android.player.PlayerEvent
import com.spotify.sdk.android.player.Spotify
import com.spotify.sdk.android.player.SpotifyPlayer
import io.moku.davide.spotify_side_project.utils.fragments.CustomFragment

import io.moku.davide.spotify_side_project.utils.fragments.CustomTabbedFragment
import io.moku.davide.spotify_side_project.utils.preferences.PreferencesManager
import kaaes.spotify.webapi.android.models.TrackSimple

class MainActivity : AppCompatActivity(), Player.NotificationCallback, ConnectionStateCallback {

    companion object {
        val TAG = MainActivity::class.java.simpleName
        val SECONDS = 1000
        // Request code that will be used to verify if the result comes from correct activity
        private val REQUEST_CODE = 1337
    }

    /* Fields */
    var mPlayer: SpotifyPlayer? = null
    var isPlaying = false
    private var isNowPlayingFragmentVisible = false
    private var isLoginOpen = false

    var queue: ArrayList<TrackSimple> = arrayListOf()
    var currentTrack : TrackSimple? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load MainFragment
        loadMainFragment()
    }

    override fun onResume() {
        super.onResume()

        // check token
        val accessToken = PreferencesManager.getAccessToken(this)
        if (accessToken != null) {
            setupPlayer(accessToken)
        } else {
            openLogin()
        }
    }

    override fun onDestroy() {
        Spotify.destroyPlayer(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (isNowPlayingFragmentVisible) {
            toggleNowPlaying()
        } else {
            super.onBackPressed()
        }
    }

    fun updateView() {
        val currentFragment = currentFragment()
        if (currentFragment.isAdded && !currentFragment.isHidden) {
            currentFragment.updateView()
        }
    }


    /**
     *
     * FRAGMENTS
     *
     */

    fun currentFragment() : CustomFragment = supportFragmentManager.findFragmentByTag(currentTag()) as CustomFragment

    fun currentTag() = if (isNowPlayingFragmentVisible) NowPlayingFragment.TAG else MainFragment.TAG

    fun loadMainFragment() {
        supportFragmentManager.beginTransaction()
                .add(R.id.container, MainFragment.newInstance(), MainFragment.TAG)
                .commit()
    }

    fun showNowPlayingFragment() {
        // Hide MainFragment
        val mainFragment = supportFragmentManager.findFragmentByTag(MainFragment.TAG)
        if (mainFragment.isAdded && !mainFragment.isHidden) {
            supportFragmentManager.beginTransaction().hide(mainFragment).commit()
        }
        // Show NowPlayingFragment
        supportFragmentManager.beginTransaction()
                .add(R.id.container, NowPlayingFragment.newInstance(), NowPlayingFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    fun hideNowPlayingFragment() {
        // Hide NowPlayingFragment
        supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentByTag(NowPlayingFragment.TAG)).commit()
        // Show MainFragment
        val mainFragment = supportFragmentManager.findFragmentByTag(MainFragment.TAG)
        if (mainFragment.isAdded && mainFragment.isHidden) {
            supportFragmentManager.beginTransaction().show(mainFragment).commit()
            (mainFragment as CustomFragment).updateView()
        }
    }

    fun toggleNowPlaying() {
        if (isNowPlayingFragmentVisible) hideNowPlayingFragment() else showNowPlayingFragment()
        isNowPlayingFragmentVisible = !isNowPlayingFragmentVisible
    }

    /**
     *
     * PLAYER
     *
     */

    fun _playButtonPressed() {
        if (isPlaying) {
            pause()
        } else {
            resumeOrPlay()
        }
        isPlaying = !isPlaying
    }

    fun playSong(uri: String?) {
        if (!isPlaying) {
            currentFragment().updatePlayButton()
            isPlaying = !isPlaying
        }
        _playSong(uri)
    }

    private fun _playSong(uri: String?) {
        // update current track on player
        currentFragment().updatePlayerInfo()
        // This is the line that actually plays a song.
        mPlayer?.playUri(null, uri, 0, 0)
    }

    fun resumeOrPlay() {
        mPlayer?.resume(object : Player.OperationCallback {
            override fun onSuccess() {
                if (!mPlayer!!.playbackState.isPlaying) {
                    playSong()
                }
            }

            override fun onError(error: Error) {
                Toast.makeText(this@MainActivity, "Error while trying to resume!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun playSong() {
        mPlayer?.skipToNext(null)
    }

    fun pause() {
        mPlayer?.pause(null)
    }

    fun prev() {
        if (mPlayer?.playbackState?.positionMs!! < 1 * SECONDS) {
            mPlayer?.seekToPosition(null, 0)
        } else {
            playTrack(prevSong())
        }
    }

    fun next() = playTrack(nextSong())

    private fun setupPlayer(accessToken: String) {
        // retrieve player
        val playerConfig = Config(this, accessToken, Constants.CLIENT_ID)
        Spotify.getPlayer(playerConfig, this, object : SpotifyPlayer.InitializationObserver {
            override fun onInitialized(spotifyPlayer: SpotifyPlayer) {
                mPlayer = spotifyPlayer
                mPlayer?.addConnectionStateCallback(this@MainActivity)
                mPlayer?.addNotificationCallback(this@MainActivity)
            }

            override fun onError(throwable: Throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.message)
            }
        })
    }

    fun clearQueue() = queue.clear()

    fun addToQueue(tracks: List<TrackSimple>) {
        queue.addAll(tracks)
    }

    fun isTrackCurrentlyInQueue(trackUri : String) : Boolean = queue.count { it.uri == trackUri } > 0

    fun getTrackPositionInQueue(trackUri : String) : Int = queue.indexOf(queue.filter { it.uri == trackUri }.first())

    fun getTrackInQueue(trackUri : String) : TrackSimple = queue.filter { it.uri == trackUri }.first()

    fun clearAndAddToQueue(tracks: List<TrackSimple>) {
        clearQueue()
        addToQueue(tracks)
    }

    fun prevSong() : TrackSimple {
        val oldCurrentTrack = currentTrack

        //val oldCurrentPosition = findSong(true, queue.first())
        //val currentPosition : Int = queue.indexOf(currentTrack as TrackSimple)

        updateCurrentTrack(true, queue.first())
        currentFragment().notifySongs(oldCurrentTrack, currentTrack)

        return currentTrack as TrackSimple
    }

    fun nextSong() : TrackSimple {
        val oldCurrentTrack = currentTrack

        //val oldCurrentPosition = findSong(false, queue.last())
        //val currentPosition : Int = queue.indexOf(currentTrack as TrackSimple)

        updateCurrentTrack(false, queue.last())
        currentFragment().notifySongs(oldCurrentTrack, currentTrack)

        return currentTrack as TrackSimple
    }

    // updated the currentTrack property and returns the old current position
    fun findSong(prev : Boolean, comparisonTrack : TrackSimple) : Int {
        var oldCurrentPosition : Int = -1
        if (currentTrack == null)
            currentTrack = if (prev) queue.last() else queue.first()
        else if (currentTrack == comparisonTrack) {
            oldCurrentPosition = queue.indexOf(comparisonTrack)
            currentTrack = if (prev) queue.last() else queue.first()
        } else {
            oldCurrentPosition = queue.indexOf(currentTrack as TrackSimple)
            var p = queue.indexOf(currentTrack as TrackSimple)
            p = if (prev) p-1 else p+1
            currentTrack = queue.get(p)
        }
        return oldCurrentPosition
    }

    fun updateCurrentTrack(prev : Boolean, comparisonTrack : TrackSimple) {
        if (currentTrack == null || currentTrack == comparisonTrack)
            currentTrack = if (prev) queue.last() else queue.first()
        else {
            var p = queue.indexOf(currentTrack as TrackSimple)
            p = if (prev) p-1 else p+1
            currentTrack = queue.get(p)
        }
    }

    fun playTrack(track: TrackSimple) {
        val oldCurrent = currentTrack
        currentTrack = track
        //notifyItemsChanged(pos1 = queue.indexOf(oldCurrent), pos2 = queue.indexOf(currentTrack as TrackSimple))
        playSong(currentTrack?.uri)
    }

    fun enablePlayer(enable: Boolean) = currentFragment().enablePlayer(enable)

    fun setWhosPlaying(whosPlaying: WhosPlaying) {
        val current = currentFragment()
        if (current is MainFragment) {
            current.whosPlaying = whosPlaying
        }
    }



    /**
     *
     * LOGIN
     *
     */

    fun openLogin() {
        if(!isLoginOpen) {
            // LOCK LOGIN
            isLoginOpen = true
            // Open Login
            val builder = AuthenticationRequest.Builder(Constants.CLIENT_ID, AuthenticationResponse.Type.TOKEN, Constants.SPOTIFY_REDIRECT_URI)
            builder.setScopes(arrayOf("user-read-private", "user-library-read", "streaming"))
            val request = builder.build()
            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)
        // UNLOCK LOGIN
        isLoginOpen = false

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthenticationClient.getResponse(resultCode, intent)
            if (response.type == AuthenticationResponse.Type.TOKEN) {
                val accessToken = response.accessToken
                PreferencesManager.storeAccessToken(this, accessToken)
                setupPlayer(accessToken)
                updateView()
            }
        }
    }

    override fun onLoggedIn() {
        Log.d("MainActivity", "User logged in")
    }

    override fun onLoggedOut() {
        Log.d("MainActivity", "User logged out")
    }

    override fun onLoginFailed(var1: Error) {
        Log.d("MainActivity", "Login failed")
    }

    override fun onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred")
    }

    override fun onConnectionMessage(message: String) {
        Log.d("MainActivity", "Received connection message: " + message)
    }


    /**
     *
     * SPOTIFY CALLBACKS
     *
     */

    override fun onPlaybackEvent(playerEvent: PlayerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name)
        when (playerEvent) {
        // Handle event type as necessary
            PlayerEvent.kSpPlaybackNotifyTrackDelivered -> next()
            else -> {
            }
        }
    }

    override fun onPlaybackError(error: Error) {
        Log.d("MainActivity", "Playback error received: " + error.name)
        /*when (error) {
        // Handle error type as necessary
            else -> {
            }
        }*/
    }

}

enum class WhosPlaying {
    MY_TRACKS,
    MY_ALBUMS,
    MY_PLAYLISTS
}