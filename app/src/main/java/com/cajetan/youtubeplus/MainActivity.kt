package com.cajetan.youtubeplus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.SearchManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cajetan.youtubeplus.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity(), PlaylistContentFragment.InteractionListener {
    private val TAG = this.javaClass.simpleName

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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
            else -> throw IllegalStateException("No fragment to receive the result")
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Init
    ////////////////////////////////////////////////////////////////////////////////

    private fun setupNavigation() {
        findNavController(R.id.mainContainer).addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {
                R.id.searchFragment -> {
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }

                R.id.playlistContent, R.id.settings -> {
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }

                else -> {
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
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
                is StartFragment -> findNavController(R.id.mainContainer)
                        .navigate(R.id.action_start_to_searchFragment,
                        bundleOf("search_query" to query))
                is SearchFragment -> fragment.searchVideos(query)
                is FavouritesFragment -> fragment.filterVideos(query)
                else -> throw IllegalStateException("No fragment to receive the result")
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
