package com.doug2d2.chore_divvy_android

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.doug2d2.chore_divvy_android.chore.ChoreListFragment
import com.doug2d2.chore_divvy_android.chore.ChoreListFragmentDirections
import com.doug2d2.chore_divvy_android.chore.ChoreListViewModel
import com.doug2d2.chore_divvy_android.chore.ChoreListViewModelFactory
import com.doug2d2.chore_divvy_android.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import timber.log.Timber

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val application = requireNotNull(this).application
        val viewModelFactory = MainViewModelFactory(application)
        val mainViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(MainViewModel::class.java)
        binding.viewModel = mainViewModel
        binding.setLifecycleOwner(this)

        setupNavigation()

        // Observe changes to categories and add items to menu
        mainViewModel.categories.observe(binding.lifecycleOwner!!, Observer { categories ->
            val selectedCategorySet = Utils.isSelectedCategorySet(this)

            // Loop through all of user's categories and add a menu item for each one
            // Also add them to navigationViewMenuItems map to keep track of view ids for
            // click events
            categories.forEachIndexed { idx, c ->
                // Set first category as selected category if not set
                if (!selectedCategorySet && idx == 0) {
                    Utils.setSelectedCategory(this, c.id)
                }

                val viewId  = View.generateViewId()

                mainViewModel.navigationViewMenuItems.put(viewId,
                    NavViewMenuItem(categoryId = c.id, name = c.categoryName))

                binding.navigationView.menu.add(1, viewId, idx, c.categoryName)
            }
        })

        // Add sign out menu item and add it to the navigationViewMenuItems
        // map to keep track of view ids for click events
        val signOutViewId  = View.generateViewId()

        mainViewModel.navigationViewMenuItems.put(signOutViewId,
            NavViewMenuItem(categoryId = -1, name = "Sign out"))

        binding.navigationView.menu.add(2, signOutViewId, mainViewModel.navigationViewMenuItems.size+1, "Sign out")

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
        val catId = binding.viewModel?.navigationViewMenuItems?.get(item.itemId)?.categoryId
        when (catId) {
            // -1 is sign out
            -1 -> {
                Utils.logout(this)

                // Navigate to login
                findNavController(R.id.nav_host_fragment).navigate(ChoreListFragmentDirections.actionChoreListFragmentToLoginFragment())
            }
            else -> {
                // if catId is not -1 and catId is not the current selected category
                // update the selected category and get chores for that category
                if (!catId?.equals(-1)!! && !catId.equals(Utils.getSelectedCategory(this))!!) {
                    // Update selected category
                    Utils.setSelectedCategory(this, catId!!)

                    // Get chores for new category by navigating from chore list to log in
                    // Since user is still logged in, navigating to log in will take the user
                    // back to the chore list with the chores for the new category
                    findNavController(R.id.nav_host_fragment).navigate(ChoreListFragmentDirections.actionChoreListFragmentToLoginFragment())
                }
            }
        }

        // Close menu
        binding.drawerLayout.closeDrawer(Gravity.LEFT)

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
