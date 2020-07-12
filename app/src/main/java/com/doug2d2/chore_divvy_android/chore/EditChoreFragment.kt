package com.doug2d2.chore_divvy_android.chore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.Frequency
import com.doug2d2.chore_divvy_android.databinding.FragmentEditChoreBinding
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import fr.ganfra.materialspinner.MaterialSpinner
import timber.log.Timber

class EditChoreFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentEditChoreBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_edit_chore, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = EditChoreViewModelFactory(application)
        val editChoreViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(EditChoreViewModel::class.java)
        binding.viewModel = editChoreViewModel

        // Get choreToEdit from arguments and convert to type Chore
        val moshi: Moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<Chore> = moshi.adapter(Chore::class.java)
        editChoreViewModel.choreToEdit.value = adapter.fromJson(arguments?.getSerializable("choreToEdit").toString())

        // Frequency drop down
        editChoreViewModel.freqs.observe(viewLifecycleOwner, Observer<List<Frequency>> { freqs ->
            // Set frequency names as values in frequency spinner
            val freqSpinner: MaterialSpinner = binding.frequencyDropDown
            val freqNames = freqs.map { f -> f.frequencyName }
            val freqAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, freqNames)
            freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            freqSpinner.adapter = freqAdapter
            freqSpinner.onItemSelectedListener = editChoreViewModel

            // Set current value
            for ((idx, f) in freqs.withIndex()) {
                if (f.id == editChoreViewModel.choreToEdit.value?.frequencyId) {
                    Timber.i("SELECTION " + idx + " F " + f)
                    freqSpinner.setSelection(idx + 1)
                    break
                }
            }
        })

        // Category drop down
        editChoreViewModel.cats.observe(viewLifecycleOwner, Observer<List<Category>> { cats ->
            // Set category names as values in category spinner
            val catSpinner: MaterialSpinner = binding.categoryDropDown
            val catNames = cats.map { c -> c.categoryName }
            val catAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, catNames)
            catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            catSpinner.adapter = catAdapter
            catSpinner.onItemSelectedListener = editChoreViewModel

            // Set current value
            for ((idx, c) in cats.withIndex()) {
                if (c.id == editChoreViewModel.choreToEdit.value?.categoryId) {
                    Timber.i("SELECTION " + idx + " C " + c)
                    catSpinner.setSelection(idx + 1)
                    break
                }
            }
        })

        // Difficulty drop down
        editChoreViewModel.diffs.observe(viewLifecycleOwner, Observer<List<String>> { diffs ->
            val diffSpinner: MaterialSpinner = binding.difficultyDropDown
            val diffAdapter: ArrayAdapter<String> = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, diffs )
            diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            diffSpinner.adapter = diffAdapter
            diffSpinner.onItemSelectedListener = editChoreViewModel

            // Set current value
            for ((idx, d) in diffs.withIndex()) {
                if (d == editChoreViewModel.choreToEdit.value?.difficulty) {
                    Timber.i("SELECTION " + idx + " D " + d)
                    diffSpinner.setSelection(idx + 1)
                    break
                }
            }
        })

        // Observe editChoreStatus
        editChoreViewModel.editChoreStatus.observe(viewLifecycleOwner, Observer<ApiStatus> { editChoreStatus ->
            Utils.hideKeyboard(activity)

            when (editChoreStatus) {
                ApiStatus.LOADING -> {
                    Timber.i("Loading...")
                    editChoreViewModel.saveButtonEnabled.value = false
                    binding.errorText.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
                ApiStatus.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.GONE

                    Toast.makeText(this.requireContext(), "Chore Updated", Toast.LENGTH_SHORT).show()

                    // Navigate to chore list
                    findNavController().navigate(EditChoreFragmentDirections.actionEditChoreFragmentToChoreListFragment())
                }
                ApiStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")
                    binding.errorText.text = "Error connecting to our servers, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    editChoreViewModel.saveButtonEnabled.value = true
                    binding.progressBar.visibility = View.GONE
                }
                ApiStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")
                    binding.errorText.text = "An unknown error has occurred, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    editChoreViewModel.saveButtonEnabled.value = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        })

        binding.setLifecycleOwner(this)

        return binding.root
    }
}