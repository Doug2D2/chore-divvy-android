package com.doug2d2.chore_divvy_android.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.databinding.FragmentUserBinding

class UserFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentUserBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_user, container, false)

        val application = requireNotNull(this.activity).application

        val dataSource = ChoreDivvyDatabase.getInstance(application).userDao
        val viewModelFactory = UserViewModelFactory(dataSource, application)

        val userViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(UserViewModel::class.java)

        binding.setLifecycleOwner(this)
        binding.userViewModel = userViewModel

        return binding.root
    }
}
