package com.doug2d2.chore_divvy_android.chore

import com.doug2d2.chore_divvy_android.R
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.Frequency
import com.doug2d2.chore_divvy_android.databinding.FragmentAddChoreBinding
import fr.ganfra.materialspinner.MaterialSpinner
import timber.log.Timber


class AddChoreFragment : Fragment() {
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
            // Set frequency names as values in frequency spinner
            val freqSpinner: MaterialSpinner = binding.frequencyDropDown
            val freqNames = freqs.map { f -> f.frequencyName }
            val freqAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, freqNames)
            freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            freqSpinner.adapter = freqAdapter
            freqSpinner.onItemSelectedListener = addChoreViewModel
        })

        // Category drop down
        addChoreViewModel.cats.observe(viewLifecycleOwner, Observer<List<Category>> { cats ->
            val selectedCatId = Utils.getSelectedCategory(requireContext())
            var defaultIdx = -1

            // Set category names as values in category spinner
            val catSpinner: MaterialSpinner = binding.categoryDropDown
            val catNames = cats.mapIndexed { idx, c ->
                // If id equals selected category id, store the index
                if (c.id == selectedCatId) {
                    defaultIdx = idx
                }
                c.categoryName
            }

            val catAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, catNames)
            catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            catSpinner.adapter = catAdapter
            // Set category spinner to current selected category (index + 1)
            catSpinner.setSelection(++defaultIdx)
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

        // Observe choreName
        addChoreViewModel.choreName.observe(viewLifecycleOwner, Observer<String> { name ->
            // Enable Add Chore button if all required fields have a value
            addChoreViewModel.checkEnableAddChoreButton()
        })

        // Call onAddChore if Enter is pressed from the notes edit text
        binding.notesEditText.setOnKeyListener { v, keyCode, event ->
            if (event.action === KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        addChoreViewModel.onAddChore()
                        true
                    }
                    else -> false
                }
            }
            false
        }

        // Observe addChoreStatus
        addChoreViewModel.apiChoreStatus.observe(viewLifecycleOwner, Observer<ApiStatus> { addChoreStatus ->
            Utils.hideKeyboard(activity)

            when (addChoreStatus) {
                ApiStatus.LOADING -> {
                    Timber.i("Loading...")
                    addChoreViewModel.addChoreButtonEnabled.value = false
                    binding.errorText.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
                ApiStatus.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.GONE

                    Toast.makeText(this.requireContext(), "Chore Added", Toast.LENGTH_SHORT).show()

                    // Navigate to chore list
                    findNavController().navigate(AddChoreFragmentDirections.actionAddChoreFragmentToChoreListFragment())
                }
                ApiStatus.CONNECTION_ERROR -> {
                    Timber.i("Connection Error")
                    binding.errorText.text = "Error connecting to our servers, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    addChoreViewModel.addChoreButtonEnabled.value = true
                    binding.progressBar.visibility = View.GONE
                }
                ApiStatus.OTHER_ERROR -> {
                    Timber.i("Other Error")
                    binding.errorText.text = "An unknown error has occurred, please try again."
                    binding.errorText.visibility = View.VISIBLE
                    addChoreViewModel.addChoreButtonEnabled.value = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        })

        binding.setLifecycleOwner(this)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
