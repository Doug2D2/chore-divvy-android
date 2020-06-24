package com.doug2d2.chore_divvy_android.chore

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.MainActivity
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.databinding.FragmentChoreListBinding
import com.doug2d2.chore_divvy_android.setCheckboxImage
import com.doug2d2.chore_divvy_android.user.LoginFragmentDirections
import com.doug2d2.chore_divvy_android.user.LoginStatus
import kotlinx.android.synthetic.main.chore_item.view.*
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
            Timber.i("checkbox click")
            choreListViewModel.updateChore(chore)

            // Get index of chore and update the checkbox
            val idx = choreListViewModel.getChoreListItemIndex(chore)
            if (idx > -1) {
                 binding.choreList.adapter?.notifyItemChanged(idx)
            }
        }, choreListViewModel)

        choreListViewModel.navigateToAddChore.observe(viewLifecycleOwner, Observer<Boolean> { navigate ->
            if (navigate) {
                val navController = findNavController()
                navController.navigate(R.id.action_choreListFragment_to_addChoreFragment)
                choreListViewModel.onNavigatedToAddChore()
            }
        })

        choreListViewModel.updateChoreStatus.observe(viewLifecycleOwner, Observer { updateChoreStatus ->
            // enum class UpdateChoreStatus { LOADING, SUCCESS, UNAUTHORIZED, CONNECTION_ERROR, OTHER_ERROR }
        })

        choreListViewModel.deleteChoreStatus.observe(viewLifecycleOwner, Observer { deleteChoreStatus ->
            when (deleteChoreStatus) {
                ChoreStatus.LOADING -> {
                    Timber.i("Loading...")
                    //binding.errorText.visibility = View.GONE
                    //binding.loginButton.isEnabled = false
                    //binding.progressBar.visibility = View.VISIBLE
                }
                ChoreStatus.SUCCESS -> {
//                    binding.errorText.visibility = View.GONE
//                    binding.loginButton.isEnabled = true
//                    binding.progressBar.visibility = View.GONE

                    val idx = choreListViewModel.getChoreListItemIndex(choreListViewModel.choreToDelete)
                    if (idx > -1) {
                        binding.choreList.adapter?.notifyItemRemoved(idx)
                        binding.choreList.adapter?.notifyDataSetChanged()
                    }
                }
                ChoreStatus.UNAUTHORIZED -> {
                    Timber.i("Incorrect username and/or password")
//                    binding.errorText.text = "Incorrect username and/or password"
//                    binding.errorText.visibility = View.VISIBLE
//                    binding.loginButton.isEnabled = true
//                    binding.progressBar.visibility = View.GONE
                }
                ChoreStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")
//                    binding.errorText.text = "Error connecting to our servers, please try again."
//                    binding.errorText.visibility = View.VISIBLE
//                    binding.loginButton.isEnabled = true
//                    binding.progressBar.visibility = View.GONE
                }
                ChoreStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")
//                    binding.errorText.text = "An unknown error has occurred, please try again."
//                    binding.errorText.visibility = View.VISIBLE
//                    binding.loginButton.isEnabled = true
//                    binding.progressBar.visibility = View.GONE
                }
            }
        })

        // Sets the adapter of the RecyclerView
        binding.choreList.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.setLifecycleOwner(this)

        return binding.root
    }
}
