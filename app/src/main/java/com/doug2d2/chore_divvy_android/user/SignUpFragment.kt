package com.doug2d2.chore_divvy_android.user

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.databinding.FragmentSignUpBinding
import timber.log.Timber
import kotlin.math.sin

class SignUpFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSignUpBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_sign_up, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = SignUpViewModelFactory(application)
        val signUpViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(SignUpViewModel::class.java)
        binding.viewModel = signUpViewModel

        signUpViewModel.firstName.observe(viewLifecycleOwner, Observer<String> { firstName ->
            if (!firstName.isNullOrBlank() && !signUpViewModel.lastName.value.isNullOrBlank() &&
                !signUpViewModel.username.value.isNullOrBlank() &&
                !signUpViewModel.password.value.isNullOrBlank()) {
                binding.signUpButton.isEnabled = true
            } else {
                binding.signUpButton.isEnabled = false
            }
        })

        signUpViewModel.lastName.observe(viewLifecycleOwner, Observer<String> { lastName ->
            if (!lastName.isNullOrBlank() && !signUpViewModel.firstName.value.isNullOrBlank() &&
                !signUpViewModel.username.value.isNullOrBlank() &&
                !signUpViewModel.password.value.isNullOrBlank()) {
                binding.signUpButton.isEnabled = true
            } else {
                binding.signUpButton.isEnabled = false
            }
        })

        signUpViewModel.username.observe(viewLifecycleOwner, Observer<String> { username ->
            if (!username.isNullOrBlank() && !signUpViewModel.firstName.value.isNullOrBlank() &&
                !signUpViewModel.lastName.value.isNullOrBlank() &&
                !signUpViewModel.password.value.isNullOrBlank()) {
                binding.signUpButton.isEnabled = true
            } else {
                binding.signUpButton.isEnabled = false
            }
        })

        signUpViewModel.password.observe(viewLifecycleOwner, Observer<String> { password ->
            if (!password.isNullOrBlank() && !signUpViewModel.firstName.value.isNullOrBlank() &&
                !signUpViewModel.lastName.value.isNullOrBlank() &&
                !signUpViewModel.username.value.isNullOrBlank()) {
                binding.signUpButton.isEnabled = true
            } else {
                binding.signUpButton.isEnabled = false
            }
        })

        // Call onSignUp if Enter is pressed from the password edit text
        binding.passwordEditText.setOnKeyListener { v, keyCode, event ->
            if (event.action === KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        signUpViewModel.onSignUp()
                        true
                    }
                    else -> false
                }
            }
            false
        }

        signUpViewModel.signUpStatus.observe(viewLifecycleOwner, Observer<SignUpStatus> { signUpStatus ->
            when (signUpStatus) {
                SignUpStatus.LOADING -> {
                    Timber.i("Loading...")
                    binding.errorText.visibility = View.GONE
                    binding.signUpButton.isEnabled = false
                }
                SignUpStatus.SUCCESS -> {
                    binding.errorText.visibility = View.GONE
                    binding.signUpButton.isEnabled = true

                    if (findNavController().currentDestination?.id == R.id.signUpFragment) {
                        findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToChoreListFragment())
                    }
                }
                SignUpStatus.INVALID_USERNAME -> {
                    Timber.i("Invalid username")
                    binding.errorText.text = "Invalid username"
                    binding.errorText.visibility = View.VISIBLE
                    binding.signUpButton.isEnabled = true
                }
                SignUpStatus.INVALID_PASSWORD -> {
                    Timber.i("Invalid password")
                    binding.errorText.text = "Invalid password"
                    binding.errorText.visibility = View.VISIBLE
                    binding.signUpButton.isEnabled = true
                }
                SignUpStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")
                    binding.errorText.text = "Connection Error"
                    binding.errorText.visibility = View.VISIBLE
                    binding.signUpButton.isEnabled = true
                }
                SignUpStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")
                    binding.errorText.text = "Other Error"
                    binding.errorText.visibility = View.VISIBLE
                    binding.signUpButton.isEnabled = true
                }
            }
        })

        return binding.root
    }
}
