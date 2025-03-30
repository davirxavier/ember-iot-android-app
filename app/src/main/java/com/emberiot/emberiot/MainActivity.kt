package com.emberiot.emberiot

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.emberiot.emberiot.databinding.ActivityMainBinding
import com.emberiot.emberiot.util.OnPrevCallback
import com.emberiot.emberiot.view_model.LoginViewModel
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_devices, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener { item ->
            val action = when(item.itemId) {
                R.id.nav_devices_item -> R.id.nav_devices
                R.id.nav_settings_item -> R.id.nav_settings
                else -> null
            }

            action?.let { navController.navigate(action, null, NavOptions.Builder().apply {
                setPopUpTo(action, true)
            }.build()) }

            drawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener true;
        }

        EmberIotApp.initCallback = {
            loginViewModel.currentUser.observe(this) {
                val emailText: TextView = navView.getHeaderView(0).findViewById(R.id.userEmail)
                emailText.text = it.user.email
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main).let {
            it?.childFragmentManager?.fragments?.get(0)?.let { frag ->
                if (frag is OnPrevCallback) {
                    frag.onPrev()
                }
            }
        }

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_add) {

            return true;
        }
        else {
            return super.onOptionsItemSelected(item)
        }
    }
}