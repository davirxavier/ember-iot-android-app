package com.emberiot.emberiot

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.emberiot.emberiot.databinding.ActivityMainBinding
import com.emberiot.emberiot.device_view.DeviceViewFragment
import com.emberiot.emberiot.devices.DeviceListFragment
import com.emberiot.emberiot.devices.NewDeviceFragment
import com.emberiot.emberiot.util.OnActionClick
import com.emberiot.emberiot.util.OnPrevCallback
import com.emberiot.emberiot.util.UiUtils
import com.emberiot.emberiot.view_model.LoginViewModel
import com.google.android.material.navigation.NavigationView
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack

class MainActivity : AppCompatActivity(), IconDialog.Callback {

    override val iconDialogIconPack: IconPack?
        get() = EmberIotApp.iconPack

    override fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>) {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main).let {
            it?.childFragmentManager?.fragments?.get(0)?.let { frag ->
                if (frag is NewDeviceFragment) {
                    frag.onIconSelected(dialog, icons)
                }
            }
        }
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_devices, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, _ ->
            invalidateOptionsMenu()
        }

        navView.setNavigationItemSelectedListener { item ->
            val action = when (item.itemId) {
                R.id.nav_devices_item -> R.id.nav_devices
                R.id.nav_settings_item -> R.id.nav_settings
                else -> null
            }

            action?.let {
                navController.navigate(action, null, NavOptions.Builder().apply {
                    setPopUpTo(action, true)
                }.build())
            }

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
        val id = navController.currentDestination?.id

        binding.root.post {
            UiUtils.getCurrentFragment(supportFragmentManager)?.let { frag ->
                if (frag is DeviceViewFragment && frag.editMode) {
                    menuInflater.inflate(R.menu.view_editor_menu, menu)
                    return@post
                }
            }

            menuInflater.inflate(
                when (id) {
                    R.id.nav_devices -> R.menu.device_list
                    R.id.newDeviceFragment -> R.menu.new_device
                    R.id.viewDeviceFragment -> R.menu.device_edit_view
                    R.id.uiObjectConfigFragment -> R.menu.new_device
                    else -> R.menu.no_actions
                }, menu
            )
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        UiUtils.getCurrentFragment(supportFragmentManager)?.let { frag ->
            if (frag is OnPrevCallback) {
                frag.onPrev()
            } else if (frag is DeviceViewFragment && frag.editMode) {
                frag.onExit()
                return false
            }
        }

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return UiUtils.getCurrentFragment(supportFragmentManager)
            ?.let { if (it is OnActionClick) it.onActionClick(item.itemId) else false } ?: false
    }
}