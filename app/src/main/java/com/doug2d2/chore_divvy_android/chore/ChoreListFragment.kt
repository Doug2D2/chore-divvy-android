package com.doug2d2.chore_divvy_android.chore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.databinding.FragmentChoreListBinding
import com.doug2d2.chore_divvy_android.databinding.FragmentLoginBinding
import com.doug2d2.chore_divvy_android.user.LoginViewModel
import com.doug2d2.chore_divvy_android.user.LoginViewModelFactory
import timber.log.Timber

class ChoreListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentChoreListBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_chore_list, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = ChoreListViewModelFactory(application)
        val choreListViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(ChoreListViewModel::class.java)
        binding.viewModel = choreListViewModel

        val adapter = ChoreListAdapter(ChoreListClickListener { chore ->
//            val destination = Uri.parse(chapter.website)
//            startActivity(Intent(Intent.ACTION_VIEW, destination))
            Timber.i("Clicked")
        })

        // Sets the adapter of the RecyclerView
        binding.choreList.adapter = adapter

        return binding.root
    }
}
