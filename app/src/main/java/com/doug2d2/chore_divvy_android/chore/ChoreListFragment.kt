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

        // Sets the adapter of the RecyclerView
        binding.choreList.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.setLifecycleOwner(this)

        return binding.root
    }
}
