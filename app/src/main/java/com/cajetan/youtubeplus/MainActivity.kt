package com.cajetan.youtubeplus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cajetan.youtubeplus.fragments.FavouritesFragment
import com.cajetan.youtubeplus.fragments.PlaylistContentFragment
import com.cajetan.youtubeplus.fragments.VideoListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), PlaylistContentFragment.InteractionListener {
    private val TAG = this.javaClass.simpleName

    private lateinit var mMenu: Menu
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var userIsInteracting = false

    ////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        val darkMode = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.dark_mode_key), false)
        setTheme(if (darkMode) R.style.AppThemeDark else R.style.AppThemeLight)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController: NavController = findNavController(R.id.mainContainer)
        appBarConfiguration = AppBarConfiguration(navController.graph)

        findViewById<BottomNavigationView>(R.id.bottomBar)
                .setupWithNavController(navController)

        setupActionBarWithNavController(navController, appBarConfiguration)

        createNotificationChannel()
        handleIntent(intent)
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

        setupNavigation()

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.mainContainer).navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        userIsInteracting = false
        handleIntent(intent as Intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // TODO: find a better solution 1/2
        val fragment = supportFragmentManager.findFragmentById(R.id.mainContainer)!!
                .childFragmentManager.fragments[0]
        when (fragment) {
            is Fragment -> fragment.onActivityResult(requestCode, resultCode, data)
            else -> Toast.makeText(this,
                    getString(R.string.no_fragment_found), Toast.LENGTH_SHORT)
                    .show()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Init
    ////////////////////////////////////////////////////////////////////////////////

    private fun setupNavigation() {
        findNavController(R.id.mainContainer).addOnDestinationChangedListener { _, destination, _ ->
            val searchView = mMenu.findItem(R.id.search)?.actionView as SearchView?

            when (destination.id) {
                R.id.start -> {
                    searchView?.setQuery("", false)
                    searchView?.isIconified = true
                    searchView?.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }

                R.id.favourites -> {
                    searchView?.setQuery("", false)
                    searchView?.isIconified = true
                    searchView?.queryHint = getString(R.string.search_favourites)
                    searchView?.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }

                R.id.others -> {
                    searchView?.setQuery("", false)
                    searchView?.isIconified = true
                    searchView?.visibility = View.GONE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }

                R.id.library -> {
                    searchView?.setQuery("", false)
                    searchView?.isIconified = true
                    searchView?.visibility = View.GONE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }

                R.id.playlistContent -> {
                    searchView?.setQuery("", false)
                    searchView?.isIconified = true
                    searchView?.visibility = View.GONE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    supportActionBar?.title = "TESTT"
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

            // TODO: find a better solution 2/2
            val fragment = supportFragmentManager.findFragmentById(R.id.mainContainer)!!
                    .childFragmentManager.fragments[0]
            when (fragment) {
                is VideoListFragment -> fragment.searchVideos(query)
                is FavouritesFragment -> fragment.filterVideos(query)
                else -> Toast.makeText(this,
                        getString(R.string.no_fragment_found), Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Callbacks & others
    ////////////////////////////////////////////////////////////////////////////////

    override fun onChannelTitle(title: String) {
        supportActionBar?.title = title
    }
}
