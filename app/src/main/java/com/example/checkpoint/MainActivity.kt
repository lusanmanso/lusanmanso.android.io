package com.example.checkpoint

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.example.checkpoint.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Customize the top bar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        navController = findNavController(R.id.nav_host_fragment_content_main)

        // Configure which destinations are considered "top level" (no back button)
        val topLevelDestinations = setOf(
            R.id.loginFragment,
            R.id.homeFragment
        )

        appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Hide/show FAB depending on the current fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            handleDestinationChange(destination)
        }

        binding.fab.setOnClickListener {
            Snackbar.make(it, "Add this functionality in the next iteration!", Snackbar.LENGTH_LONG)
                .setAction("Close", null)
                .setAnchorView(R.id.fab).show()
        }
    }

    private fun handleDestinationChange(destination: NavDestination) {
        // Only show the FAB on the home screen
        binding.fab.isVisible = destination.id == R.id.homeFragment

        // Show/hide the toolbar based on the fragment
        val hideToolbarDestinations = setOf(R.id.loginFragment, R.id.registerFragment)
        binding.appBarLayout.isVisible = destination.id !in hideToolbarDestinations

        // Update title in the top bar according to the screen
        val title = when (destination.id) {
            R.id.homeFragment -> "Checkpoint"
            R.id.gameDetailFragment -> "Details"
            else -> ""
        }
        binding.toolbarTitle.text = title
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Snackbar.make(binding.root, "Settings not implemented yet", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout -> {
                signOut()
                true
            }
            R.id.action_filter -> {
                Snackbar.make(binding.root, "Filters not implemented yet", Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        auth.signOut()
        navController.navigate(R.id.loginFragment, null, androidx.navigation.NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build())
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
