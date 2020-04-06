package com.doug2d2.chore_divvy_android.user

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.R
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

//        val sharedPrefs: SharedPreferences = this.requireContext().getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)
//        val editor = sharedPrefs.edit()
//        editor.putBoolean("loggedIn", true)
//        editor.apply()

        loginViewModel.username.observe(viewLifecycleOwner, Observer<String> { username ->
            if (!username.isNullOrBlank() && !loginViewModel.password.value.isNullOrBlank()) {
                binding.loginButton.isEnabled = true
            } else {
                binding.loginButton.isEnabled = false
            }
        })

        loginViewModel.password.observe(viewLifecycleOwner, Observer<String> { password ->
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

        loginViewModel.navigateToSignUp.observe(viewLifecycleOwner, Observer<Boolean> { navigate ->
            if (navigate) {
                if (findNavController().currentDestination?.id == R.id.loginFragment) {
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment())
                }
            }
        })

        loginViewModel.navigateToForgotPassword.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                if (findNavController().currentDestination?.id == R.id.loginFragment) {
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment())
                }
            }
        })

        loginViewModel.loginStatus.observe(viewLifecycleOwner, Observer<LoginStatus> { loginStatus ->
            when (loginStatus) {
                LoginStatus.LOADING -> {
                    Timber.i("Loading...")
                    binding.errorText.visibility = View.GONE
                    binding.loginButton.isEnabled = false
                }
                LoginStatus.SUCCESS -> {
                    binding.errorText.visibility = View.GONE
                    binding.loginButton.isEnabled = true

                    if (findNavController().currentDestination?.id == R.id.loginFragment) {
                        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToChoreListFragment())
                    }
                }
                LoginStatus.INVALID_CREDENTIALS -> {
                    Timber.i("Incorrect username and/or password")
                    binding.errorText.text = "Incorrect username and/or password"
                    binding.errorText.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = true
                }
                LoginStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")
                    binding.errorText.text = "Error connecting to our servers, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = true
                }
                LoginStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")
                    binding.errorText.text = "An unknown error has occurred, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = true
                }
            }
        })

//        if (sharedPrefs.getBoolean("loggedIn", false)) {
//            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToChoreListFragment())
//        }

        return binding.root
    }
}
