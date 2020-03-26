package com.doug2d2.chore_divvy_android.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController

import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
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

        // LOADING, SUCCESS, INVALID_CREDENTIALS, CONNECTION_ERROR, OTHER_ERROR
        loginViewModel.loginStatus.observe(viewLifecycleOwner, Observer<LoginStatus> { loginStatus ->
            when (loginStatus) {
                LoginStatus.LOADING -> {
                    Timber.i("Loading...")
                    binding.errorText.visibility = View.INVISIBLE
                }
                LoginStatus.SUCCESS -> {
                    findNavController().navigate(R.id.action_loginFragment_to_choreListFragment)
                    binding.errorText.visibility = View.INVISIBLE
                }
                LoginStatus.INVALID_CREDENTIALS -> {
                    Timber.i("Incorrect username and/or password")
                    binding.errorText.text = "Incorrect username and/or password"
                    binding.errorText.visibility = View.VISIBLE
                }
                LoginStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")
                    binding.errorText.text = "Connection Error"
                    binding.errorText.visibility = View.VISIBLE
                }
                LoginStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")
                    binding.errorText.text = "Other Error"
                    binding.errorText.visibility = View.VISIBLE
                }
            }
        })

        return binding.root
    }
}
