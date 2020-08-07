package com.doug2d2.chore_divvy_android.user

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.databinding.FragmentSignUpBinding
import timber.log.Timber

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

        // Observe changes to firstName
        signUpViewModel.firstName.observe(viewLifecycleOwner, Observer<String> { firstName ->
            binding.errorText.visibility = View.GONE

            // Enable sign up button if all required fields have a value
            if (!firstName.isNullOrBlank() && !signUpViewModel.lastName.value.isNullOrBlank() &&
                !signUpViewModel.username.value.isNullOrBlank() &&
                !signUpViewModel.password.value.isNullOrBlank()) {
                binding.signUpButton.isEnabled = true
            } else {
                binding.signUpButton.isEnabled = false
            }
        })

        // Observe changes to lastName
        signUpViewModel.lastName.observe(viewLifecycleOwner, Observer<String> { lastName ->
            binding.errorText.visibility = View.GONE

            // Enable sign up button if all required fields have a value
            if (!lastName.isNullOrBlank() && !signUpViewModel.firstName.value.isNullOrBlank() &&
                !signUpViewModel.username.value.isNullOrBlank() &&
                !signUpViewModel.password.value.isNullOrBlank()) {
                binding.signUpButton.isEnabled = true
            } else {
                binding.signUpButton.isEnabled = false
            }
        })

        // Observe changes to username
        signUpViewModel.username.observe(viewLifecycleOwner, Observer<String> { username ->
            binding.errorText.visibility = View.GONE

            // Enable sign up button if all required fields have a value
            if (!username.isNullOrBlank() && !signUpViewModel.firstName.value.isNullOrBlank() &&
                !signUpViewModel.lastName.value.isNullOrBlank() &&
                !signUpViewModel.password.value.isNullOrBlank()) {
                binding.signUpButton.isEnabled = true
            } else {
                binding.signUpButton.isEnabled = false
            }
        })

        // Observe changes to password
        signUpViewModel.password.observe(viewLifecycleOwner, Observer<String> { password ->
            binding.errorText.visibility = View.GONE

            // Enable sign up button if all required fields have a value
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

        // Observe changes to signUpStatus
        signUpViewModel.signUpStatus.observe(viewLifecycleOwner, Observer<SignUpStatus> { signUpStatus ->
            Utils.hideKeyboard(activity)

            when (signUpStatus) {
                SignUpStatus.LOADING -> {
                    Timber.i("Loading...")
                    binding.errorText.visibility = View.GONE
                    binding.signUpButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                SignUpStatus.SUCCESS -> {
                    binding.errorText.visibility = View.GONE
                    binding.signUpButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE

                    Utils.login(this.requireContext(), signUpViewModel.userID)

                    // Navigate to chore list
                    findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToChoreListFragment())
                }
                SignUpStatus.USERNAME_ALREADY_EXISTS -> {
                    Timber.i("Username already exists")
                    binding.errorText.text = "Account ${signUpViewModel.username.value} already exists"
                    binding.errorText.visibility = View.VISIBLE
                    binding.signUpButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
                SignUpStatus.USERNAME_INVALID_FORMAT -> {
                    Timber.i("Username is not a valid email address")
                    binding.errorText.text = "Email address invalid"
                    binding.errorText.visibility = View.VISIBLE
                    binding.signUpButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
                SignUpStatus.PASSWORD_TOO_SHORT -> {
                    Timber.i("Password too short")
                    binding.errorText.text = "Password must be at least 8 characters."
                    binding.errorText.visibility = View.VISIBLE
                    binding.signUpButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
                SignUpStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")
                    binding.errorText.text = "Error connecting to our servers, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    binding.signUpButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
                SignUpStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")
                    binding.errorText.text = "An unknown error has occurred, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    binding.signUpButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        })

        return binding.root
    }
}
