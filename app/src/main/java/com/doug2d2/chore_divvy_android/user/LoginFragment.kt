package com.doug2d2.chore_divvy_android.user

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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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

        loginViewModel.username.observe(viewLifecycleOwner, Observer<String> { username ->
            if (username != "" && loginViewModel.password.value != null && loginViewModel.password.value != "") {
                binding.loginButton.isEnabled = true
            } else {
                binding.loginButton.isEnabled = false
            }
        })

        loginViewModel.password.observe(viewLifecycleOwner, Observer<String> { password ->
            if (password != "" && loginViewModel.username.value != null && loginViewModel.username.value != "") {
                binding.loginButton.isEnabled = true
            } else {
                binding.loginButton.isEnabled = false
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
                    findNavController().navigate(R.id.action_loginFragment_to_choreListFragment)
                    binding.errorText.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                }
                LoginStatus.INVALID_CREDENTIALS -> {
                    Timber.i("Incorrect username and/or password")
                    binding.errorText.text = "Incorrect username and/or password"
                    binding.errorText.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = true
                }
                LoginStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")
                    binding.errorText.text = "Connection Error"
                    binding.errorText.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = true
                }
                LoginStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")
                    binding.errorText.text = "Other Error"
                    binding.errorText.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = true
                }
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


        return binding.root
    }
}
