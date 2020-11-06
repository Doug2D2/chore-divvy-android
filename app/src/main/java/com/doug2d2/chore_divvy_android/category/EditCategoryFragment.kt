package com.doug2d2.chore_divvy_android.category

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.view.children
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
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.databinding.FragmentEditCategoryBinding
import com.doug2d2.chore_divvy_android.databinding.FragmentEditChoreBinding
import kotlinx.android.synthetic.main.fragment_user_edit_text.view.*
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

        // Observe userEmails to set the current users in edit texts
        editCategoryViewModel.userEmails.observe(viewLifecycleOwner, Observer<List<String>>  { userEmails ->
            for (u in userEmails) {
                // Add edit text for adding a user
                val fmTrans = fragmentManager?.beginTransaction()
                val newUserEditText = UserEditTextFragment(u)
                fmTrans?.add(binding.userEditTextLayout.id, newUserEditText)
                fmTrans?.commit()
            }
        })

        // Observe addUserEditText to add a new edit text for adding a new user
        editCategoryViewModel.addUserEditText.observe(viewLifecycleOwner, Observer<Boolean> { addUserEditText ->
            if (addUserEditText) {
                // Add edit text for adding a user
                val fmTrans = fragmentManager?.beginTransaction()
                val newUserEditText = UserEditTextFragment()
                fmTrans?.add(binding.userEditTextLayout.id, newUserEditText)
                fmTrans?.commit()

                editCategoryViewModel.doneAddUserEditText()
            }
        })

        // Observe shouldSave
        editCategoryViewModel.shouldSave.observe(viewLifecycleOwner, Observer<Boolean> { shouldSave ->
            if (shouldSave) {
                // Get all user emails from user edit texts
                var users = mutableListOf<String>()
                for (c in binding.userEditTextLayout.children) {
                    val u = c.userEditText.text.toString().trim()
                    if (u.isNotEmpty()) {
                        users.add(c.userEditText.text.toString())
                    }
                }

                editCategoryViewModel.getUserIds(users)
                editCategoryViewModel.doneSave()
            }
        })

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
                ApiStatus.USER_BAD_FORMAT_ERROR -> {
                    Timber.i("User Bad Format Error")

                    binding.errorText.text = "Invalid format for one or more email addresses"
                    binding.errorText.visibility = View.VISIBLE

                    editCategoryViewModel.saveButtonEnabled.value = true

                    binding.progressBar.visibility = View.GONE
                }
                ApiStatus.USER_DOESNT_EXIST_ERROR -> {
                    Timber.i("User Doesn't Exist Error")

                    binding.errorText.text = "One or more users do not exist"
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

        // Call onSave if Enter is pressed from the categoryName edit text
        binding.categoryNameEditText.setOnKeyListener { v, keyCode, event ->
            if (event.action === KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        editCategoryViewModel.onSave()
                        true
                    }
                    else -> false
                }
            }
            false
        }

        binding.setLifecycleOwner(this)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
