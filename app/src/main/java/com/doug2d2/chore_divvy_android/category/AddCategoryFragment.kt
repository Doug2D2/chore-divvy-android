package com.doug2d2.chore_divvy_android.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.AddStatus
import com.doug2d2.chore_divvy_android.MainActivity
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.databinding.FragmentAddCategoryBinding
import timber.log.Timber

class AddCategoryFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentAddCategoryBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_add_category, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = AddCategoryViewModelFactory(application)
        val addCategoryViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(AddCategoryViewModel::class.java)
        binding.viewModel = addCategoryViewModel

        // Observe categoryName
        addCategoryViewModel.categoryName.observe(viewLifecycleOwner, Observer<String> { name ->
            // Enable Add Category button if all required fields have a value
            if (!addCategoryViewModel.categoryName.value.isNullOrBlank()) {
                addCategoryViewModel.addCategoryButtonEnabled.value = true
            } else {
                addCategoryViewModel.addCategoryButtonEnabled.value = false
            }
        })

        // Observe addCategoryStatus
        addCategoryViewModel.addCategoryStatus.observe(viewLifecycleOwner, Observer<AddStatus> { addCategoryStatus ->
            Utils.hideKeyboard(activity)

            when (addCategoryStatus) {
                AddStatus.LOADING -> {
                    Timber.i("Loading...")
                    addCategoryViewModel.addCategoryButtonEnabled.value = false
                    binding.errorText.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
                AddStatus.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.GONE

                    Toast.makeText(this.requireContext(), "Category Added", Toast.LENGTH_SHORT).show()

                    // Get all categories and navigate to chore list
                    (activity as MainActivity?)?.binding?.viewModel?.getCategories()
                    findNavController().navigate(AddCategoryFragmentDirections.actionAddCategoryFragmentToChoreListFragment())
                }
                AddStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")

                    binding.errorText.text = "Error connecting to our servers, please try again."
                    binding.errorText.visibility = View.VISIBLE

                    addCategoryViewModel.addCategoryButtonEnabled.value = true

                    binding.progressBar.visibility = View.GONE
                }
                AddStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")

                    binding.errorText.text = "An unknown error has occurred, please try again."
                    binding.errorText.visibility = View.VISIBLE

                    addCategoryViewModel.addCategoryButtonEnabled.value = true

                    binding.progressBar.visibility = View.GONE
                }
            }
        })

        binding.setLifecycleOwner(this)

        return binding.root
    }
}
