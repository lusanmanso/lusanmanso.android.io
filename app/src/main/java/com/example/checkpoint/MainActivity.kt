// Author: Pair Programmer
// OS support: Android
// Description: Main activity hosting the navigation graph and bottom navigation.
package com.example.checkpoint

import android.os.Bundle
import android.util.Log // Import Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment // <-- Necesario para obtener el fragmento
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
// import androidx.navigation.findNavController // Ya no usamos esta importación aquí
import androidx.navigation.ui.NavigationUI
import com.example.checkpoint.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp // Mantener si no usas Initializer
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate START") // Log inicio onCreate

        // FirebaseApp.initializeApp(this) // Comentado si usas Initializer
        auth = FirebaseAuth.getInstance()

        binding = ActivityMainBinding.inflate(layoutInflater)
        Log.d("MainActivity", "Layout inflated")
        setContentView(binding.root)
        Log.d("MainActivity", "setContentView completed")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        Log.d("MainActivity", "Toolbar setup")

        // --- NavController Retrieval (VOLVEMOS AL MÉTODO ANTERIOR) ---
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment? // Cast opcional (?)

        if (navHostFragment == null) {
            Log.e("MainActivity", "ERROR: NavHostFragment not found! Check ID in activity_main.xml")
            // Puedes lanzar una excepción o manejar el error como prefieras si no se encuentra
            // throw IllegalStateException("NavHostFragment not found!")
            return // Salir si no se encuentra el fragmento
        }
        navController = navHostFragment.navController
        Log.d("MainActivity", "NavController obtained from NavHostFragment")
        // --- Fin Modificación ---

        // --- Setup Bottom Navigation ---
        val bottomNavView: BottomNavigationView = binding.bottomNavView
        NavigationUI.setupWithNavController(bottomNavView, navController)
        Log.d("MainActivity", "BottomNav setup")

        // Configuración AppBar
        val topLevelDestinations = setOf(
            R.id.loginFragment, R.id.homeFragment, R.id.favoritesFragment, R.id.profileFragment
        )
        appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        setupActionBarWithNavController(navController, appBarConfiguration)
        Log.d("MainActivity", "ActionBar setup")

        // Listener cambios destino
        navController.addOnDestinationChangedListener { _, destination, _ ->
            handleDestinationChange(destination)
        }
        Log.d("MainActivity", "Destination listener added")
        Log.d("MainActivity", "onCreate END")
    }

    // handleDestinationChange, signOut, onSupportNavigateUp (SIN CAMBIOS desde la versión anterior)
    private fun handleDestinationChange(destination: NavDestination) {
        val hideChromeDestinations = setOf(
            R.id.splashFragment, R.id.loginFragment, R.id.registerFragment
        )
        val shouldShowChrome = destination.id !in hideChromeDestinations
        binding.appBarLayout.isVisible = shouldShowChrome
        binding.bottomNavView.isVisible = shouldShowChrome
        val title = when (destination.id) {
            R.id.homeFragment -> getString(R.string.app_name)
            R.id.gameDetailFragment -> destination.label
            R.id.favoritesFragment -> getString(R.string.favorites_title)
            R.id.profileFragment -> getString(R.string.profile_title)
            else -> ""
        }
        binding.toolbarTitle.text = title
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
// --- End of MainActivity.kt ---