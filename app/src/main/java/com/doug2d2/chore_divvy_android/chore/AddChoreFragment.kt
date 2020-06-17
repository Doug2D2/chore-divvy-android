package com.doug2d2.chore_divvy_android.chore

import com.doug2d2.chore_divvy_android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.Frequency
import com.doug2d2.chore_divvy_android.databinding.FragmentAddChoreBinding
import fr.ganfra.materialspinner.MaterialSpinner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class AddChoreFragment : Fragment()/*, AdapterView.OnItemSelectedListener*/ {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentAddChoreBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_add_chore, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = AddChoreViewModelFactory(application)
        val addChoreViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(AddChoreViewModel::class.java)
        binding.viewModel = addChoreViewModel

        // Frequency drop down
        addChoreViewModel.freqs.observe(viewLifecycleOwner, Observer<List<Frequency>> {freqs ->
            val freqSpinner: MaterialSpinner = binding.frequencyDropDown
            val freqNames = freqs.map { f -> f.frequencyName }
            val freqAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, freqNames)
            freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            freqSpinner.adapter = freqAdapter
            freqSpinner.onItemSelectedListener = addChoreViewModel
        })

        // Category drop down
        addChoreViewModel.cats.observe(viewLifecycleOwner, Observer<List<Category>> { cats ->
            val catSpinner: MaterialSpinner = binding.categoryDropDown
            val catNames = cats.map { c -> c.categoryName }
            val catAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, catNames)
            catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            catSpinner.adapter = catAdapter
            catSpinner.onItemSelectedListener = addChoreViewModel
        })

        // Difficulty drop down
        addChoreViewModel.diffs.observe(viewLifecycleOwner, Observer<List<String>> { diffs ->
            val diffSpinner: MaterialSpinner = binding.difficultyDropDown
            val diffAdapter: ArrayAdapter<String> = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, diffs )
            diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            diffSpinner.adapter = diffAdapter
            diffSpinner.onItemSelectedListener = addChoreViewModel
        })

        addChoreViewModel.choreName.observe(viewLifecycleOwner, Observer<String> { name ->
            // Enable Add Chore button if all required fields have a value
            addChoreViewModel.checkEnableAddChoreButton()
        })

        binding.setLifecycleOwner(this)

        return binding.root
    }

//    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//
//        Timber.i("Item Click: " + (parent?.id == R.id.frequencyDropDown))
//        // position is List index
//    }
//
//    override fun onNothingSelected(parent: AdapterView<*>?) {
//        Timber.i("Nothing Selected")
//    }

}
