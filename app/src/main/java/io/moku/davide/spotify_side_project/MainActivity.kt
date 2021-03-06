package io.moku.davide.spotify_side_project

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast

import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.player.Config
import com.spotify.sdk.android.player.ConnectionStateCallback
import com.spotify.sdk.android.player.Error
import com.spotify.sdk.android.player.Player
import com.spotify.sdk.android.player.PlayerEvent
import com.spotify.sdk.android.player.Spotify
import com.spotify.sdk.android.player.SpotifyPlayer
import io.moku.davide.spotify_side_project.nowPlaying.NowPlayingFragment
import io.moku.davide.spotify_side_project.utils.fragments.CustomFragment

import io.moku.davide.spotify_side_project.utils.preferences.PreferencesManager
import kaaes.spotify.webapi.android.models.Track

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

    var queue: ArrayList<Track> = arrayListOf()
    var currentTrack : Track? = null

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
        back()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                back()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun back() {
        if (isNowPlayingFragmentVisible) {
            toggleNowPlaying()
        } else {
            val main = currentFragment() as MainFragment
            if (main.canHandleBack()) {
                main.back()
            } else {
                super.onBackPressed()
            }
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
            isPlaying = !isPlaying
            currentFragment().updatePlayButton()
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

    fun prev(checkSeconds : Boolean) {
        if (checkSeconds && mPlayer?.playbackState?.positionMs!! > 2 * SECONDS) {
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

    fun addToQueue(tracks: List<Track>) {
        queue.addAll(tracks)
    }

    fun isTrackCurrentlyInQueue(trackUri : String) : Boolean = queue.count { it.uri == trackUri } > 0

    fun getTrackPositionInQueue(trackUri : String) : Int = queue.indexOf(queue.filter { it.uri == trackUri }.first())

    fun getCurrentTrackPositionInQueue() : Int = queue.indexOf(currentTrack)

    fun getTrackInQueue(trackUri : String) : Track = queue.filter { it.uri == trackUri }.first()

    fun clearAndAddToQueue(tracks: List<Track>) {
        clearQueue()
        addToQueue(tracks)
    }

    fun prevSong() : Track {
        val oldCurrentTrack = currentTrack

        //val oldCurrentPosition = findSong(true, queue.first())
        //val currentPosition : Int = queue.indexOf(currentTrack as Track)

        updateCurrentTrack(true, queue.first())
        currentFragment().notifySongs(oldCurrentTrack, currentTrack)

        return currentTrack as Track
    }

    fun nextSong() : Track {
        val oldCurrentTrack = currentTrack

        //val oldCurrentPosition = findSong(false, queue.last())
        //val currentPosition : Int = queue.indexOf(currentTrack as Track)

        updateCurrentTrack(false, queue.last())
        currentFragment().notifySongs(oldCurrentTrack, currentTrack)

        return currentTrack as Track
    }

    // updated the currentTrack property and returns the old current position
    fun findSong(prev : Boolean, comparisonTrack : Track) : Int {
        var oldCurrentPosition : Int = -1
        if (currentTrack == null)
            currentTrack = if (prev) queue.last() else queue.first()
        else if (currentTrack == comparisonTrack) {
            oldCurrentPosition = queue.indexOf(comparisonTrack)
            currentTrack = if (prev) queue.last() else queue.first()
        } else {
            oldCurrentPosition = queue.indexOf(currentTrack as Track)
            var p = queue.indexOf(currentTrack as Track)
            p = if (prev) p-1 else p+1
            currentTrack = queue.get(p)
        }
        return oldCurrentPosition
    }

    fun updateCurrentTrack(prev : Boolean, comparisonTrack : Track) {
        if (currentTrack == null || currentTrack == comparisonTrack)
            currentTrack = if (prev) queue.last() else queue.first()
        else {
            var p = queue.indexOf(currentTrack as Track)
            p = if (prev) p-1 else p+1
            currentTrack = queue.get(p)
        }
    }

    fun playTrack(track: Track) {
        val oldCurrent = currentTrack
        currentTrack = track
        //notifyItemsChanged(pos1 = queue.indexOf(oldCurrent), pos2 = queue.indexOf(currentTrack as Track))
        playSong(currentTrack?.uri)
    }

    fun setWhosPlaying(whosPlaying: WhosPlaying) {
        val current = currentFragment()
        if (current is MainFragment) {
            current.whosPlaying = whosPlaying
        }
    }

    fun getWhosPlaying() : WhosPlaying? {
        val current = currentFragment()
        if (current is MainFragment) {
            return current.whosPlaying
        } else {
            return null
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
            val builder = AuthorizationRequest.Builder(Constants.CLIENT_ID, AuthorizationResponse.Type.TOKEN, Constants.SPOTIFY_REDIRECT_URI)
            builder.setScopes(arrayOf("user-read-private", "user-library-read", "streaming"))
            val request = builder.build()
            AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)
        // UNLOCK LOGIN
        isLoginOpen = false

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            if (response.type == AuthorizationResponse.Type.TOKEN) {
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