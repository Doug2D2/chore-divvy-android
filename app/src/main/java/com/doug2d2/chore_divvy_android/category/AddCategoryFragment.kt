package com.doug2d2.chore_divvy_android.category

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.ApiStatus
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

        // Call onAddCategory if Enter is pressed from the categoryName edit text
        binding.categoryNameEditText.setOnKeyListener { v, keyCode, event ->
            if (event.action === KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        addCategoryViewModel.onAddCategory()
                        true
                    }
                    else -> false
                }
            }
            false
        }

        // Observe addCategoryStatus
        addCategoryViewModel.apiCategoryStatus.observe(viewLifecycleOwner, Observer<ApiStatus> { addCategoryStatus ->
            Utils.hideKeyboard(activity)

            when (addCategoryStatus) {
                ApiStatus.LOADING -> {
                    Timber.i("Loading...")
                    addCategoryViewModel.addCategoryButtonEnabled.value = false
                    binding.errorText.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
                ApiStatus.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.GONE

                    // If there is a valid category id, set that as the selected category
                    if (addCategoryViewModel.newCategoryId != -1) {
                        Utils.setSelectedCategory(this.requireContext(), addCategoryViewModel.newCategoryId)
                    }

                    Toast.makeText(this.requireContext(), "Category Added", Toast.LENGTH_SHORT).show()

                    // Get all categories and navigate to chore list
                    (activity as MainActivity?)?.binding?.viewModel?.getCategories()
                    findNavController().navigate(AddCategoryFragmentDirections.actionAddCategoryFragmentToChoreListFragment())
                }
                ApiStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")

                    binding.errorText.text = "Error connecting to our servers, please try again."
                    binding.errorText.visibility = View.VISIBLE

                    addCategoryViewModel.addCategoryButtonEnabled.value = true

                    binding.progressBar.visibility = View.GONE
                }
                ApiStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")

                    binding.errorText.text = "An unknown error has occurred, please try again."
                    binding.errorText.visibility = View.VISIBLE

                    addCategoryViewModel.addCategoryButtonEnabled.value = true

                    binding.progressBar.visibility = View.GONE
                }
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
