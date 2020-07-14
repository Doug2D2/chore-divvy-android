package com.doug2d2.chore_divvy_android.category

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.MainActivity
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.chore.EditChoreFragmentDirections
import com.doug2d2.chore_divvy_android.chore.EditChoreViewModel
import com.doug2d2.chore_divvy_android.chore.EditChoreViewModelFactory
import com.doug2d2.chore_divvy_android.databinding.FragmentEditCategoryBinding
import com.doug2d2.chore_divvy_android.databinding.FragmentEditChoreBinding
import timber.log.Timber

class EditCategoryFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentEditCategoryBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_edit_category, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = EditCategoryViewModelFactory(application)
        val editCategoryViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(EditCategoryViewModel::class.java)
        binding.viewModel = editCategoryViewModel

        // Observe editCategoryStatus
        editCategoryViewModel.editCategoryStatus.observe(viewLifecycleOwner, Observer<ApiStatus> { editCategoryStatus ->
            Utils.hideKeyboard(activity)

            when (editCategoryStatus) {
                ApiStatus.LOADING -> {
                    Timber.i("Loading...")
                    editCategoryViewModel.saveButtonEnabled.value = false
                    binding.errorText.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
                ApiStatus.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.GONE

                    Toast.makeText(this.requireContext(), "Category Updated", Toast.LENGTH_SHORT).show()

                    // Navigate to chore list
                    findNavController().navigate(EditCategoryFragmentDirections.actionEditCategoryFragmentToChoreListFragment())
                }
                ApiStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")
                    binding.errorText.text = "Error connecting to our servers, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    editCategoryViewModel.saveButtonEnabled.value = true
                    binding.progressBar.visibility = View.GONE
                }
                ApiStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")
                    binding.errorText.text = "An unknown error has occurred, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    editCategoryViewModel.saveButtonEnabled.value = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        })

        // Observe categoryName to enable/disable save button
        editCategoryViewModel.categoryName.observe(viewLifecycleOwner, Observer<String> {
            if (!editCategoryViewModel.categoryName.value.isNullOrBlank()) {
                editCategoryViewModel.saveButtonEnabled.value = true
            } else {
                editCategoryViewModel.saveButtonEnabled.value = false
            }
        })

        binding.setLifecycleOwner(this)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
