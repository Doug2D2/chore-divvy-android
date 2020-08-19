package com.doug2d2.chore_divvy_android.chore

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.*
import com.doug2d2.chore_divvy_android.database.FullChore
import com.doug2d2.chore_divvy_android.databinding.FragmentChoreListBinding
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.chore_item.view.*
import kotlinx.android.synthetic.main.fragment_chore_detail.view.*
import timber.log.Timber

class ChoreListFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        if (Utils.getRefresh(context!!)) {
            val navController = findNavController()
            navController.navigate(R.id.action_choreListFragment_to_loginFragment)

            Utils.setRefresh(context!!, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentChoreListBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_chore_list, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = ChoreListViewModelFactory(application)
        val choreListViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(ChoreListViewModel::class.java)
        binding.viewModel = choreListViewModel

        // Chore checkbox is clicked
        val adapter = ChoreListAdapter(ChoreListClickListener(requireContext(), { chore ->
            choreListViewModel.updateChore(chore)
        }), choreListViewModel)

        // Observe changes to navigating to add chore fragment
        choreListViewModel.navigateToAddChore.observe(viewLifecycleOwner, Observer<Boolean> { navigate ->
            if (navigate) {
                val navController = findNavController()
                navController.navigate(R.id.action_choreListFragment_to_addChoreFragment)
                choreListViewModel.onNavigatedToAddChore()
            }
        })

        // Observe changes to navigating to edit chore fragment
        choreListViewModel.navigateToEditChore.observe(viewLifecycleOwner, Observer<Boolean> { navigate ->
            if (navigate) {
                val moshi: Moshi = Moshi.Builder().build()
                val adapter: JsonAdapter<FullChore> = moshi.adapter(FullChore::class.java)
                val choreJson = adapter.toJson(choreListViewModel.choreToEdit)

                val navController = findNavController()
                val bundle = bundleOf("choreToEdit" to choreJson)
                navController.navigate(R.id.action_choreListFragment_to_editChoreFragment, bundle)
                choreListViewModel.onNavigatedToEditChore()
            }
        })

        // Observe changes to navigating to chore detail fragment
        choreListViewModel.navigateToDetailView.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                val moshi: Moshi = Moshi.Builder().build()
                val adapter: JsonAdapter<FullChore> = moshi.adapter(FullChore::class.java)
                val choreJson = adapter.toJson(choreListViewModel.choreDetailView)

                val navController = findNavController()
                val bundle = bundleOf("choreDetailView" to choreJson)
                navController.navigate(R.id.action_choreListFragment_to_choreDetailFragment, bundle)
                choreListViewModel.onNavigatedToDetailView()
            }
        })

        // Observe change to getting chores
        choreListViewModel.getChoresStatus.observe(viewLifecycleOwner, Observer { updateChoreStatus ->
            Timber.i("Chore Status")
            when (updateChoreStatus) {
                ApiStatus.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorText.visibility = View.GONE
                }
                ApiStatus.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.GONE
                    binding.refreshLayout.isRefreshing = false
                }
                ApiStatus.UNAUTHORIZED -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = "You are not authorized to get these chores"
                }
                ApiStatus.CONNECTION_ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = "Error connecting to our servers, please try again"
                }
                ApiStatus.OTHER_ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = "An unknown error has occurred, please try again"
                }
            }
        })

        // Observe change to updating a chore
        choreListViewModel.updateChoreStatus.observe(viewLifecycleOwner, Observer { updateChoreStatus ->
            when (updateChoreStatus) {
                ApiStatus.SUCCESS -> {
                    // Get index of chore and update the checkbox
                    val idx = choreListViewModel.getChoreListItemIndex(choreListViewModel.choreToUpdate)
                    if (idx > -1) {
                        binding.choreList.adapter?.notifyItemChanged(idx)
                    }
                }
                ApiStatus.UNAUTHORIZED -> {
                    Toast.makeText(this.activity, "You are not authorized to update this chore", Toast.LENGTH_LONG).show()
                }
                ApiStatus.CONNECTION_ERROR -> {
                    Toast.makeText(this.activity, "Error connecting to our servers, please try again", Toast.LENGTH_LONG).show()
                }
                ApiStatus.OTHER_ERROR -> {
                    Toast.makeText(this.activity, "An unknown error has occurred, please try again", Toast.LENGTH_LONG).show()
                }
            }
        })

        // Observe change to deleting a chore
        choreListViewModel.deleteChoreStatus.observe(viewLifecycleOwner, Observer { deleteChoreStatus ->
            when (deleteChoreStatus) {
                ApiStatus.SUCCESS -> {
                    // Find index of chore being deleted and notify adapter
                    val idx = choreListViewModel.getChoreListItemIndex(choreListViewModel.choreToDelete)
                    if (idx > -1) {
                        binding.choreList.adapter?.notifyItemRemoved(idx)
                        binding.choreList.adapter?.notifyDataSetChanged()
                    }
                }
                ApiStatus.UNAUTHORIZED -> {
                    Toast.makeText(this.activity, "You are not authorized to delete this chore", Toast.LENGTH_LONG).show()
                }
                ApiStatus.CONNECTION_ERROR -> {
                    Toast.makeText(this.activity, "Error connecting to our servers, please try again", Toast.LENGTH_LONG).show()
                }
                ApiStatus.OTHER_ERROR -> {
                    Toast.makeText(this.activity, "An unknown error has occurred, please try again", Toast.LENGTH_LONG).show()
                }
            }
        })

        // Observe change to assigning a chore to current user
        choreListViewModel.assignChoreStatus.observe(viewLifecycleOwner, Observer { assignChoreStatus ->
            when (assignChoreStatus) {
                ApiStatus.SUCCESS -> {
                    // Get index of chore and update the assignee
                    val idx = choreListViewModel.getChoreListItemIndex(choreListViewModel.choreToUpdate)
                    if (idx > -1) {
                        binding.choreList.adapter?.notifyItemChanged(idx)
                        binding.choreList.adapter?.notifyDataSetChanged()
                    }
                }
                ApiStatus.OTHER_ERROR -> {
                    Toast.makeText(this.activity, "Unable to assign chore to you", Toast.LENGTH_LONG).show()
                }
            }
        })

        // Observe change to unassigning a chore to current user
        choreListViewModel.unassignChoreStatus.observe(viewLifecycleOwner, Observer { unassignChoreStatus ->
            when (unassignChoreStatus) {
                ApiStatus.SUCCESS -> {
                    // Get index of chore and update the assignee
                    val idx = choreListViewModel.getChoreListItemIndex(choreListViewModel.choreToUpdate)
                    if (idx > -1) {
                        binding.choreList.adapter?.notifyItemChanged(idx)
                        binding.choreList.adapter?.notifyDataSetChanged()
                    }
                }
                ApiStatus.OTHER_ERROR -> {
                    Toast.makeText(this.activity, "Unable to unassign chore", Toast.LENGTH_LONG).show()
                }
            }
        })

        // Observe change to deleting a category
        choreListViewModel.deleteCategoryStatus.observe(viewLifecycleOwner, Observer { deleteCategoryStatus ->
            when (deleteCategoryStatus) {
                ApiStatus.SUCCESS -> {
                    // Remove selected category
                    Utils.setSelectedCategory(context!!, -1)

                    // Get categories
                    choreListViewModel.getCategories()

                    // This will cause the chore list fragment to get chores for the newly selected
                    // category
                    Utils.setRefresh(context!!, true)

                    // Get chores for new category by navigating from chore list to log in
                    // Since user is still logged in, navigating to log in will take the user
                    // back to the chore list with the chores for the new category
                    findNavController().navigate(ChoreListFragmentDirections.actionChoreListFragmentToLoginFragment())
                }
                ApiStatus.UNAUTHORIZED -> {
                    Toast.makeText(context, "You are not authorized to delete this category", Toast.LENGTH_LONG).show()
                }
                ApiStatus.CONNECTION_ERROR -> {
                    Toast.makeText(context, "Error connecting to our servers, please try again", Toast.LENGTH_LONG).show()
                }
                ApiStatus.OTHER_ERROR -> {
                    Toast.makeText(context, "An unknown error has occurred, please try again", Toast.LENGTH_LONG).show()
                }
            }
        })

        // Observe change to chore filter
        choreListViewModel.choreFilter.observe(viewLifecycleOwner, Observer { choreFilter ->
            choreListViewModel.filterChores()
        })

        // When screen is pulled down, refresh chore list by calling getChores
        binding.refreshLayout.setOnRefreshListener {
            Timber.i("Refresh")
            choreListViewModel.getChores()
        }

        // Sets the adapter of the RecyclerView
        binding.choreList.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.setLifecycleOwner(this)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.chore_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val application = requireNotNull(this.activity).application
        val viewModelFactory = ChoreListViewModelFactory(application)
        val choreListViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(ChoreListViewModel::class.java)

        when(item.itemId) {

            R.id.action_category_menu -> {
                var popupMenu: android.widget.PopupMenu = android.widget.PopupMenu(context, this.activity?.findViewById<View>(R.id.action_category_menu))
                popupMenu.inflate(R.menu.category_menu)

                popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
                    when(item?.itemId) {
                        R.id.category_edit -> {
                            findNavController().navigate(R.id.action_choreListFragment_to_editCategoryFragment)
                        }
                        R.id.category_delete -> {
                            // Create Alert Dialog to ask user if they are sure that they want to delete category
                            val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialog))
                            lateinit var alert: AlertDialog
                            val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> {
                                        // Yes, delete category
                                        choreListViewModel.deleteCategory()
                                    }
                                    DialogInterface.BUTTON_NEGATIVE -> {
                                        // No, don't delete category
                                        alert.cancel()
                                    }
                                }
                            }

                            // Display Alert Dialog
                            alert = alertBuilder.setTitle("Delete category?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show()
                        }
                    }
                    true
                }

                popupMenu.show()

                return true
            }
            R.id.action_filter -> {
                var popupMenu: android.widget.PopupMenu = android.widget.PopupMenu(context, this.activity?.findViewById<View>(R.id.action_filter))
                popupMenu.inflate(R.menu.filter_menu)

                popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
                    when(item?.itemId) {
                        R.id.filter_all -> {
                            choreListViewModel.choreFilter.value = ChoreFilter.ALL
                            choreListViewModel.choreFilterText.value = "All Chores"
                        }
                        R.id.filter_mine -> {
                            choreListViewModel.choreFilter.value = ChoreFilter.MINE
                            choreListViewModel.choreFilterText.value = "My Chores"
                        }
                        R.id.filter_unassigned -> {
                            choreListViewModel.choreFilter.value = ChoreFilter.UNASSIGNED
                            choreListViewModel.choreFilterText.value = "Unassigned Chores"
                        }
                        R.id.filter_to_do -> {
                            choreListViewModel.choreFilter.value = ChoreFilter.TO_DO
                            choreListViewModel.choreFilterText.value = "To Do"
                        }
                        R.id.filter_in_progress -> {
                            choreListViewModel.choreFilter.value = ChoreFilter.IN_PROGRESS
                            choreListViewModel.choreFilterText.value = "Chores In Progress"
                        }
                        R.id.filter_completed -> {
                            choreListViewModel.choreFilter.value = ChoreFilter.COMPLETED
                            choreListViewModel.choreFilterText.value = "Completed Chores"
                        }
                    }
                    true
                }

                popupMenu.show()

                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}
