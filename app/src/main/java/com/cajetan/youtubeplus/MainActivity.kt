package com.cajetan.youtubeplus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cajetan.youtubeplus.fragments.FavouritesFragment
import com.cajetan.youtubeplus.fragments.VideoListFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName

    private lateinit var mMenu: Menu

    private var userIsInteracting = false

    ////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        setupBottomBar()
        handleIntent(intent)

        supportFragmentManager.beginTransaction()
                .add(R.id.mainContainer, VideoListFragment())
                .commit()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        userIsInteracting = true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_options_menu, menu)

        if (menu != null)
            mMenu = menu

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.search)?.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        userIsInteracting = false
        handleIntent(intent as Intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.mainContainer)
        when (fragment) {
            is Fragment -> fragment.onActivityResult(requestCode, resultCode, data)
            else -> Toast.makeText(this, "No fragment found", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Init
    ////////////////////////////////////////////////////////////////////////////////

    private fun setupBottomBar() {
        bottomBar.selectedItemId = R.id.action_start
        bottomBar.setOnNavigationItemSelectedListener {
            val searchView  = if (this::mMenu.isInitialized) {
                mMenu.findItem(R.id.search)?.actionView as SearchView
            } else {
                null
            }

            when (it.itemId) {
                R.id.action_start -> {
                    if (userIsInteracting) {
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.mainContainer, VideoListFragment())
                                .commit()
                    }

                    searchView?.visibility = View.VISIBLE

                    it.setChecked(true)
                    true
                }

                R.id.action_favourites -> {
                    if (userIsInteracting) {
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.mainContainer, FavouritesFragment())
                                .commit()
                    }

                    searchView?.queryHint = "Search in favourites"
                    searchView?.visibility = View.VISIBLE

                    it.setChecked(true)
                    true
                }

                R.id.action_others -> {
                    if (userIsInteracting) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.mainContainer)

                        if (fragment != null) {
                            supportFragmentManager.beginTransaction()
                                    .remove(fragment)
                                    .commit()
                        }

                        // Reset the search menu
                        searchView?.setQuery("", false)
                        searchView?.isIconified = true
                        searchView?.visibility = View.GONE
                    }

                    it.setChecked(true)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return

        val channel = NotificationChannel(getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW)
        channel.description = getString(R.string.notification_channel_description)

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            val query = intent.getStringExtra(SearchManager.QUERY)

            val fragment = supportFragmentManager.findFragmentById(R.id.mainContainer)
            when (fragment) {
                is VideoListFragment -> fragment.searchVideos(query)
                is FavouritesFragment -> fragment.filterVideos(query)
                else -> Toast.makeText(this, "No fragment found", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }
}
