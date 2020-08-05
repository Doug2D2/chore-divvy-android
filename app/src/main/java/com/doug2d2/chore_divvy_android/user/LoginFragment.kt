package com.doug2d2.chore_divvy_android.user

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.databinding.FragmentLoginBinding
import timber.log.Timber


class LoginFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentLoginBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_login, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = LoginViewModelFactory(application)
        val loginViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(LoginViewModel::class.java)
        binding.viewModel = loginViewModel

        // If already logged in navigate to chore list
        if (Utils.isLoggedIn(this.requireContext()))  {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToChoreListFragment())
        }

        // Observe changes to username
        loginViewModel.username.observe(viewLifecycleOwner, Observer<String> { username ->
            binding.errorText.visibility = View.GONE

            // Enable log in button if all required fields have a value
            if (!username.isNullOrBlank() && !loginViewModel.password.value.isNullOrBlank()) {
                binding.loginButton.isEnabled = true
            } else {
                binding.loginButton.isEnabled = false
            }
        })

        // Observe changes to password
        loginViewModel.password.observe(viewLifecycleOwner, Observer<String> { password ->
            binding.errorText.visibility = View.GONE

            // Enable log in button if all required fields have a value
            if (!password.isNullOrBlank() && !loginViewModel.username.value.isNullOrBlank()) {
                binding.loginButton.isEnabled = true
            } else {
                binding.loginButton.isEnabled = false
            }
        })

        // Call onLogin if Enter is pressed from the password edit text
        binding.passwordEditText.setOnKeyListener { v, keyCode, event ->
            if (event.action === KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        loginViewModel.onLogin()
                        true
                    }
                    else -> false
                }
            }
            false
        }

        // Navigate to sign up fragment
        loginViewModel.navigateToSignUp.observe(viewLifecycleOwner, Observer<Boolean> { navigate ->
            if (navigate) {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment())
                loginViewModel.onNavigatedToSignUp()
            }
        })

        // Navigate to forgot password fragment
        loginViewModel.navigateToForgotPassword.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment())
                loginViewModel.onNavigatedToForgotPassword()
            }
        })

        // Observe changes to loginStatus
        loginViewModel.loginStatus.observe(viewLifecycleOwner, Observer<LoginStatus> { loginStatus ->
            Utils.hideKeyboard(activity)

            when (loginStatus) {
                LoginStatus.LOADING -> {
                    Timber.i("Loading...")
                    binding.errorText.visibility = View.GONE
                    binding.loginButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                LoginStatus.SUCCESS -> {
                    binding.errorText.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE

                    // Keeps user logged in on device
                    Utils.login(this.requireContext(), loginViewModel.userID)

                    // Navigate to chore list
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToChoreListFragment())
                }
                LoginStatus.INVALID_CREDENTIALS -> {
                    Timber.i("Incorrect username and/or password")
                    binding.errorText.text = "Incorrect username and/or password"
                    binding.errorText.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
                LoginStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")
                    binding.errorText.text = "Error connecting to our servers, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
                LoginStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")
                    binding.errorText.text = "An unknown error has occurred, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
