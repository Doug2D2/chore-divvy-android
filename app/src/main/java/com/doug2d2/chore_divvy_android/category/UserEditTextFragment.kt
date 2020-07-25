package com.doug2d2.chore_divvy_android.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.databinding.FragmentUserEditTextBinding
import timber.log.Timber

class UserEditTextFragment(startingText: String = "") : Fragment() {
    private val user = startingText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentUserEditTextBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_user_edit_text, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = UserEditTextViewModelFactory(application)
        val userEditTextViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(UserEditTextViewModel::class.java)
        binding.viewModel = userEditTextViewModel
        userEditTextViewModel.user.value = user

        // Observe removeUserEditText
        userEditTextViewModel.removeUserEditText.observe(viewLifecycleOwner, Observer<Boolean> { removeUserEditText ->
            if (removeUserEditText) {
                // Remove current user edit text fragment
                val fmTrans = fragmentManager?.beginTransaction()
                fmTrans?.remove(this)
                fmTrans?.commit()

                userEditTextViewModel.doneRemoveUserEditText()
            }
        })

        binding.setLifecycleOwner(this)

        return binding.root
    }
}