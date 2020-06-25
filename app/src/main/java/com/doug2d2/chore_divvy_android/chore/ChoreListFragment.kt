package com.doug2d2.chore_divvy_android.chore

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.MainActivity
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.databinding.FragmentChoreListBinding
import com.doug2d2.chore_divvy_android.setCheckboxImage
import com.doug2d2.chore_divvy_android.user.LoginFragmentDirections
import com.doug2d2.chore_divvy_android.user.LoginStatus
import kotlinx.android.synthetic.main.chore_item.view.*
import timber.log.Timber

class ChoreListFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val adapter = ChoreListAdapter(ChoreListClickListener { chore ->
            choreListViewModel.updateChore(chore)
        }, choreListViewModel)

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
                val navController = findNavController()
                navController.navigate(R.id.action_choreListFragment_to_editChoreFragment)
                choreListViewModel.onNavigatedToEditChore()
            }
        })

        // Observe change to getting chores
        choreListViewModel.getChoresStatus.observe(viewLifecycleOwner, Observer { updateChoreStatus ->
            when (updateChoreStatus) {
                ChoreStatus.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorText.visibility = View.GONE
                }
                ChoreStatus.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.GONE
                }
                ChoreStatus.UNAUTHORIZED -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = "You are not authorized to get these chores"
                }
                ChoreStatus.CONNECTION_ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = "Error connecting to our servers, please try again"
                }
                ChoreStatus.OTHER_ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = "An unknown error has occurred, please try again"
                }
            }
        })

        // Observe change to updating a chore
        choreListViewModel.updateChoreStatus.observe(viewLifecycleOwner, Observer { updateChoreStatus ->
            when (updateChoreStatus) {
                ChoreStatus.SUCCESS -> {
                    Timber.i("Chore updated")

                    // Get index of chore and update the checkbox
                    val idx = choreListViewModel.getChoreListItemIndex(choreListViewModel.choreToUpdate)
                    if (idx > -1) {
                        binding.choreList.adapter?.notifyItemChanged(idx)
                    }
                }
                ChoreStatus.UNAUTHORIZED -> {
                    Toast.makeText(this.activity, "You are not authorized to update this chore", Toast.LENGTH_LONG).show()
                }
                ChoreStatus.CONNECTION_ERROR -> {
                    Toast.makeText(this.activity, "Error connecting to our servers, please try again", Toast.LENGTH_LONG).show()
                }
                ChoreStatus.OTHER_ERROR -> {
                    Toast.makeText(this.activity, "An unknown error has occurred, please try again", Toast.LENGTH_LONG).show()
                }
            }
        })

        // Observe change to deleting a chore
        choreListViewModel.deleteChoreStatus.observe(viewLifecycleOwner, Observer { deleteChoreStatus ->
            when (deleteChoreStatus) {
                ChoreStatus.SUCCESS -> {
                    // Find index of chore being deleted and notify adapter
                    val idx = choreListViewModel.getChoreListItemIndex(choreListViewModel.choreToDelete)
                    if (idx > -1) {
                        binding.choreList.adapter?.notifyItemRemoved(idx)
                        binding.choreList.adapter?.notifyDataSetChanged()
                    }
                }
                ChoreStatus.UNAUTHORIZED -> {
                    Toast.makeText(this.activity, "You are not authorized to delete this chore", Toast.LENGTH_LONG).show()
                }
                ChoreStatus.CONNECTION_ERROR -> {
                    Toast.makeText(this.activity, "Error connecting to our servers, please try again", Toast.LENGTH_LONG).show()
                }
                ChoreStatus.OTHER_ERROR -> {
                    Toast.makeText(this.activity, "An unknown error has occurred, please try again", Toast.LENGTH_LONG).show()
                }
            }
        })

        // Sets the adapter of the RecyclerView
        binding.choreList.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.setLifecycleOwner(this)

        return binding.root
    }
}
