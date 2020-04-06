package com.doug2d2.chore_divvy_android.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.databinding.FragmentForgotPasswordBinding
import timber.log.Timber

class ForgotPasswordFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentForgotPasswordBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_forgot_password, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = ForgotPasswordViewModelFactory(application)
        val forgotPasswordViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(ForgotPasswordViewModel::class.java)
        binding.viewModel = forgotPasswordViewModel

        forgotPasswordViewModel.username.observe(viewLifecycleOwner, Observer<String> { username ->
            if (!username.isNullOrBlank()) {
                binding.sendLinkButton.isEnabled = true
            } else {
                binding.sendLinkButton.isEnabled = false
            }
        })

        forgotPasswordViewModel.forgotPasswordStatus.observe(viewLifecycleOwner, Observer<ForgotPasswordStatus> { forgotPasswordStatus ->
            when(forgotPasswordStatus) {
                ForgotPasswordStatus.LOADING -> {
                    Timber.i("Loading...")
                    binding.errorText.visibility = View.GONE
                    binding.sendLinkButton.isEnabled = false
                }
                ForgotPasswordStatus.SUCCESS -> {
                    binding.errorText.visibility = View.GONE
                    binding.sendLinkButton.isEnabled = true
                    Toast.makeText(this.requireContext(), "An email has been sent to ${forgotPasswordViewModel.username.value} with your new password.", Toast.LENGTH_LONG).show()
                }
                ForgotPasswordStatus.INVALID_USERNAME -> {
                    Timber.i("Unknown username ${forgotPasswordViewModel.username.value}")
                    binding.errorText.text = "${forgotPasswordViewModel.username.value} does not have an account."
                    binding.errorText.visibility = View.VISIBLE
                    binding.sendLinkButton.isEnabled = true
                }
                ForgotPasswordStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")
                    binding.errorText.text = "Error connecting to our servers, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    binding.sendLinkButton.isEnabled = true
                }
                ForgotPasswordStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")
                    binding.errorText.text = "An unknown error has occurred, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    binding.sendLinkButton.isEnabled = true
                }
            }
        })

        return binding.root
    }
}
