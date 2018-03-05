package io.moku.davide.spotify_side_project

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
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

import io.moku.davide.spotify_side_project.album.AlbumFragment
import io.moku.davide.spotify_side_project.playlist.PlaylistFragment
import io.moku.davide.spotify_side_project.tracks.SavedTracksAdapter
import io.moku.davide.spotify_side_project.tracks.TracksFragment
import io.moku.davide.spotify_side_project.utils.preferences.PreferencesManager
import kaaes.spotify.webapi.android.models.Track
import kaaes.spotify.webapi.android.models.TrackSimple

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), Player.NotificationCallback, ConnectionStateCallback {

    companion object {
        /* Constants */
        val TAG = MainActivity::class.java.simpleName
        // Request code that will be used to verify if the result comes from correct activity
        private val REQUEST_CODE = 1337
        val SECONDS = 1000
    }

    /* Fields */
    var mPlayer: SpotifyPlayer? = null
    private var isPlaying = false
    private var isPlayerVisible = false
    private var isLoginOpen = false
    private var prevMenuItem : MenuItem? = null
    var queue: ArrayList<TrackSimple> = arrayListOf()
    var currentTrack : TrackSimple? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // show title
        showPartialTitle()
        // disable player
        enablePlayer(false)
        // setup view pager
        setupViewPager()
        // listeners
        setListeners()
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

    fun setupViewPager() {
        val adapter = MainFragmentPagerAdapter(supportFragmentManager)
        adapter.addFragments(listOf(
                TracksFragment.newInstance(),
                AlbumFragment.newInstance(),
                PlaylistFragment.newInstance()))
        viewpager.adapter = adapter
        viewpager.setCurrentItem(0)
    }

    fun setListeners() {
        playButton.setOnClickListener({ playButtonPressed() })
        prevButton.setOnClickListener({
            if (mPlayer?.playbackState?.positionMs!! < 1 * SECONDS) {
                mPlayer?.seekToPosition(null, 0)
            } else {
                playTrack(prevSong())
            }
        })
        nextButton.setOnClickListener({ playTrack(nextSong()) })
        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemTracks -> {
                    viewpager.setCurrentItem(0)
                }
                R.id.itemAlbum -> {
                    viewpager.setCurrentItem(1)
                }
                R.id.itemPlaylist -> {
                    viewpager.setCurrentItem(2)
                }
            }
            false
        }
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //
            }
            override fun onPageScrollStateChanged(state: Int) {
                //
            }
            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    (prevMenuItem as MenuItem).setChecked(true)
                } else {
                    bottom_navigation.menu.getItem(0).setChecked(false)
                }
                Log.d(TAG, "onPageSelected: $position")
                bottom_navigation.menu.getItem(position).setChecked(true)
                prevMenuItem = bottom_navigation.menu.getItem(position)
            }
        })
    }


    /**
     *
     * UI
     *
     */

    fun showPartialTitle() {
        homepageTitle.animateText(getString(R.string.homepage_title_part_1))
        object : CountDownTimer((3 * SECONDS).toLong(), SECONDS.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                //
            }

            override fun onFinish() {
                showFullTitle()
            }
        }.start()
    }

    fun showFullTitle() {
        homepageTitle2.animateText(getString(R.string.homepage_title_part_2))
    }

    fun enablePlayer(enable: Boolean) {
        enableButton(playButton, enable)
        enableButton(prevButton, enable)
        enableButton(nextButton, enable)
    }

    private fun enableButton(button: ImageButton?, enable: Boolean) {
        button?.isEnabled = enable
        button?.alpha = if (enable) 1.0f else 0.3f
    }

    fun showPlayer() {
        if (!isPlayerVisible) {
            playerLayout.visibility = View.VISIBLE
            line2.visibility = View.VISIBLE
            isPlayerVisible = true
        }
    }

    fun updateFragments() {
        (viewpager.adapter as MainFragmentPagerAdapter).updateFragments()
    }


    /**
     *
     * PLAYER
     *
     */

    fun playButtonPressed() {
        if (isPlaying) {
            playButton.setImageDrawable(getDrawable(R.drawable.ic_play_circle))
            pause()
        } else {
            playButton.setImageDrawable(getDrawable(R.drawable.ic_pause_circle))
            resumeOrPlay()
        }
        isPlaying = !isPlaying
    }

    fun playSong(uri: String?) {
        if (!isPlaying) {
            playButton.setImageDrawable(getDrawable(R.drawable.ic_pause_circle))
            isPlaying = !isPlaying
        }
        _playSong(uri)
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

    private fun _playSong(uri: String?) {
        // show the player
        showPlayer()
        // This is the line that actually plays a song.
        mPlayer?.playUri(null, uri, 0, 0)
    }

    fun pause() {
        mPlayer?.pause(null)
    }

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

        val oldCurrentPosition = findSong(true, queue.first())
        val currentPosition : Int = queue.indexOf(currentTrack as TrackSimple)
        //TODO updateUI con qualcosa come notifyItemsChanged(oldCurrentPosition, currentPosition)

        return currentTrack as TrackSimple
    }

    fun nextSong() : TrackSimple {

        val oldCurrentPosition = findSong(false, queue.last())
        val currentPosition : Int = queue.indexOf(currentTrack as TrackSimple)
        //TODO updateUI con qualcosa come notifyItemsChanged(oldCurrentPosition, currentPosition)

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

    fun playTrack(track: TrackSimple) {
        val oldCurrent = currentTrack
        currentTrack = track
        //notifyItemsChanged(pos1 = queue.indexOf(oldCurrent), pos2 = queue.indexOf(currentTrack as TrackSimple))
        playSong(currentTrack?.uri)
    }

    /**
     *
     * LOGIN
     *
     */

    public fun openLogin() {
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
                updateFragments()
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
            PlayerEvent.kSpPlaybackNotifyTrackDelivered -> playButtonPressed()
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