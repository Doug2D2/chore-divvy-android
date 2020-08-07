package com.doug2d2.chore_divvy_android

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.doug2d2.chore_divvy_android.chore.*
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

        // Create menu with categories
        createMenu()

        binding.navigationView.setNavigationItemSelectedListener(this)

        // Observe changes to categories and re-create menu
        mainViewModel.categories.observe(binding.lifecycleOwner!!, Observer { categories ->
            createMenu()
        })

        // Observe changes to addCategoryStatus
        mainViewModel.apiCategoryStatus.observe(binding.lifecycleOwner!!, Observer<ApiStatus> { addCategoryStatus ->
            when (addCategoryStatus) {
                ApiStatus.SUCCESS -> {
                    mainViewModel.getCategories()
                }
            }
        })
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
                Utils.hideKeyboard(this)
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
            -2 -> {
                // Navigate to add category
                findNavController(R.id.nav_host_fragment).navigate(ChoreListFragmentDirections.actionChoreListFragmentToAddCategoryFragment())
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
                    binding?.viewModel?.getCategories()

                    val selectedCatId = Utils.getSelectedCategory(this)

                    // Set title to be selected category
                    toolBar.title = binding?.viewModel?.getCategoryNameById(selectedCatId)
                    toolBar.setHomeAsUpIndicator(R.drawable.baseline_menu_white_18dp)

                    // Update menu to highlight selected category
                    val viewId = binding?.viewModel?.getViewIdByCategoryId(selectedCatId)
                    binding?.navigationView?.menu?.findItem(viewId!!)?.setChecked(true)
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
                R.id.addCategoryFragment -> {
                    toolBar.title = "Add Category"
                    toolBar.setDisplayHomeAsUpEnabled(true)
                }
                R.id.editCategoryFragment -> {
                    toolBar.title = "Edit Category"
                    toolBar.setDisplayHomeAsUpEnabled(true)
                }
                else -> {
                    toolBar.setDisplayHomeAsUpEnabled(true)
                }
            }
        }
    }

    fun createMenu() {
        // Clear all menu items
        binding.viewModel?.navigationViewMenuItems?.clear()
        binding.navigationView.menu.clear()

        // Loop through all of user's categories and add a menu item for each one
        // Also add them to navigationViewMenuItems map to keep track of view ids for
        // click events
        var idx = 0
        val selectedCategorySet = Utils.isSelectedCategorySet(this)
        var selectedCategoryId = Utils.getSelectedCategory(this)
        binding.viewModel?.categories?.value?.forEach { c ->
            // Set first category as selected category if not set
            if (!selectedCategorySet && idx == 0) {
                Utils.setSelectedCategory(this, c.id)
                selectedCategoryId = Utils.getSelectedCategory(this)
            }

            val viewId  = View.generateViewId()

            binding.viewModel?.navigationViewMenuItems?.put(viewId,
                NavViewMenuItem(categoryId = c.id, name = c.categoryName))

            val m = binding.navigationView.menu.add(1, viewId, idx, c.categoryName)

            if (selectedCategoryId == c.id) {
                Timber.i("TRUE")
                m.setChecked(true)
            } else {
                m.setChecked(false)
            }

            idx++
        }

        // Add Add Category menu item and add it to the navigationViewMenuItems
        // map to keep track of view ids for click events
        val addCatViewId  = View.generateViewId()

        binding?.viewModel?.navigationViewMenuItems?.put(addCatViewId,
            NavViewMenuItem(categoryId = -2, name = "Add Category"))

        binding.navigationView.menu.add(2, addCatViewId, idx, "Add Category").
        setIcon(R.drawable.baseline_add_white_18dp)

        idx++

        // Add sign out menu item and add it to the navigationViewMenuItems
        // map to keep track of view ids for click events
        val signOutViewId  = View.generateViewId()

        binding?.viewModel?.navigationViewMenuItems?.put(signOutViewId,
            NavViewMenuItem(categoryId = -1, name = "Sign out"))

        binding.navigationView.menu.add(3, signOutViewId, idx, "Sign out")

        // Set toolBar title
        val toolBar = supportActionBar
        val selectedCatId = Utils.getSelectedCategory(this)
        toolBar?.title = binding?.viewModel?.getCategoryNameById(selectedCatId)
    }
}
