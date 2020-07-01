package com.doug2d2.chore_divvy_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.doug2d2.chore_divvy_android.chore.ChoreListFragmentDirections
import com.doug2d2.chore_divvy_android.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import timber.log.Timber

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupNavigation()

        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    // onSupportNavigateUp is called when the menu or back icon is clicked
    // If on choreListFragment the menu is displayed, otherwise the app goes
    // back to the previous fragment
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)

        when(navController.currentDestination?.id) {
            R.id.choreListFragment -> {
                binding.drawerLayout.openDrawer(Gravity.LEFT)
                return true
            }
            else -> {
                return navigateUp(findNavController(R.id.nav_host_fragment), binding.drawerLayout)
            }
        }
    }

    // onNavigationItemSelected is called when a main menu item is clicked
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_out -> {
                Utils.logout(this)

                // Close menu
                binding.drawerLayout.closeDrawer(Gravity.LEFT)

                // Navigate to login
                findNavController(R.id.nav_host_fragment).navigate(ChoreListFragmentDirections.actionChoreListFragmentToLoginFragment())
            }
            else -> {
                Toast.makeText(this, "Action not implemented yet", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    // setupNavigation sets the tool bar title and icon based on the current destination
    private fun setupNavigation() {
        val navController = findNavController(R.id.nav_host_fragment)

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, binding.drawerLayout)
        binding.navigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination: NavDestination, _ ->
            val toolBar = supportActionBar ?: return@addOnDestinationChangedListener

            Timber.i("OnDestinationChangedListener")
            when(destination.id) {
                R.id.loginFragment -> {
                    toolBar.title = "Chore Divvy"
                    toolBar.setDisplayHomeAsUpEnabled(false)
                }
                R.id.forgotPasswordFragment -> {
                    toolBar.title = "Chore Divvy"
                    toolBar.setDisplayHomeAsUpEnabled(true)
                }
                R.id.signUpFragment -> {
                    toolBar.title = "Chore Divvy"
                    toolBar.setDisplayHomeAsUpEnabled(true)
                }
                R.id.choreListFragment -> {
                    toolBar.title = "Chores"
                    toolBar.setHomeAsUpIndicator(R.drawable.baseline_menu_white_18dp)
                }
                R.id.addChoreFragment -> {
                    toolBar.title = "Add Chore"
                    toolBar.setDisplayHomeAsUpEnabled(true)
                }
                R.id.editChoreFragment -> {
                    toolBar.title = "Edit Chore"
                    toolBar.setDisplayHomeAsUpEnabled(true)
                }
                R.id.choreDetailFragment -> {
                    // TODO: Change toolbar title
                    toolBar.title = "Chore Detail"
                    toolBar.setDisplayHomeAsUpEnabled(true)
                }
                else -> {
                    toolBar.setDisplayHomeAsUpEnabled(true)
                }
            }
        }
    }
}
