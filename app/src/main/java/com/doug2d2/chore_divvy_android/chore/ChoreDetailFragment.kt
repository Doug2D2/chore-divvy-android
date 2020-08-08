package com.doug2d2.chore_divvy_android.chore

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Spanned
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.FullChore
import com.doug2d2.chore_divvy_android.databinding.FragmentChoreDetailBinding
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

class ChoreDetailFragment : Fragment() {
    lateinit var binding: FragmentChoreDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_chore_detail, container, false)

        val application = requireNotNull(this.activity).application
        val choreDetailViewModelFactory = ChoreDetailViewModelFactory(application)
        val choreDetailViewModel = ViewModelProviders.of(
            this, choreDetailViewModelFactory).get(ChoreDetailViewModel::class.java)
        binding.viewModel = choreDetailViewModel

        // Get choreToEdit from arguments and convert to type Chore
        val moshi: Moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<FullChore> = moshi.adapter(FullChore::class.java)
        choreDetailViewModel.choreDetailView.value = adapter.fromJson(arguments?.getSerializable("choreDetailView").toString())

        // Set all display texts
        val nameText = HtmlCompat.fromHtml(getString(R.string.choreName_detail_text, choreDetailViewModel.choreDetailView.value?.choreName?:""), HtmlCompat.FROM_HTML_MODE_LEGACY)
        val freqText = HtmlCompat.fromHtml(getString(R.string.frequency_detail_text, choreDetailViewModel.choreDetailView.value?.frequencyName?:""), HtmlCompat.FROM_HTML_MODE_LEGACY)
        val catText = HtmlCompat.fromHtml(getString(R.string.category_detail_text, choreDetailViewModel.choreDetailView.value?.categoryName?:""), HtmlCompat.FROM_HTML_MODE_LEGACY)
        val diffText = HtmlCompat.fromHtml(getString(R.string.difficulty_detail_text, choreDetailViewModel.choreDetailView.value?.difficulty?:""), HtmlCompat.FROM_HTML_MODE_LEGACY)
        val notesText = HtmlCompat.fromHtml(getString(R.string.notes_detail_text, choreDetailViewModel.choreDetailView.value?.notes?:""), HtmlCompat.FROM_HTML_MODE_LEGACY)

        // If current user is the assignee, show "me" otherwise show the assignee's first and last name
        if (choreDetailViewModel.choreDetailView.value?.assigneeId == Utils.getUserId(requireContext()) ) {
            binding.assignee.text = HtmlCompat.fromHtml(getString(R.string.assignee_detail_text, "me", ""), HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            binding.assignee.text = HtmlCompat.fromHtml(getString(R.string.assignee_detail_text, choreDetailViewModel.choreDetailView.value?.firstName?:"", choreDetailViewModel.choreDetailView.value?.lastName?:""), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        binding.choreName.text = nameText
        binding.frequency.text = freqText
        binding.category.text = catText
        binding.difficulty.text = diffText
        binding.notes.text = notesText

        // Observe changes to deleteChore (after Delete button is clicked)
        choreDetailViewModel.deleteChore.observe(viewLifecycleOwner, Observer { delete ->
            if (delete) {
                // Create Alert Dialog to ask user if they are sure that they want to delete chore
                val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(ContextThemeWrapper(this.requireView().context, R.style.CustomAlertDialog))
                lateinit var alert: AlertDialog
                val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            // Yes, delete chore
                            choreDetailViewModel.deleteChore(choreDetailViewModel.choreDetailView.value!!)
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            // No, don't delete chore
                            alert.cancel()
                        }
                    }
                }

                // Display Alert Dialog
                alert = alertBuilder.setTitle("Delete chore?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
            }
        })

        // Observe change to deleting a chore (After actually deleting chore through API)
        choreDetailViewModel.deleteChoreStatus.observe(viewLifecycleOwner, Observer { deleteChoreStatus ->
            when (deleteChoreStatus) {
                ApiStatus.SUCCESS -> {
                    val navController = findNavController()
                    navController.navigate(R.id.action_choreDetailFragment_to_choreListFragment)
                    choreDetailViewModel.onDeleteCompleted()
                }
                ApiStatus.UNAUTHORIZED -> {
                    Toast.makeText(this.activity, "You are not authorized to delete this chore", Toast.LENGTH_LONG).show()
                }
                ApiStatus.CONNECTION_ERROR -> {
                    Toast.makeText(this.activity, "Error connecting to our servers, please try again", Toast.LENGTH_LONG).show()
                }
                ApiStatus.OTHER_ERROR -> {
                    Toast.makeText(this.activity, "An unknown error has occurred, please try again", Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.setLifecycleOwner(this)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.chore_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_edit -> {
                val moshi: Moshi = Moshi.Builder().build()
                val adapter: JsonAdapter<FullChore> = moshi.adapter(FullChore::class.java)
                val choreJson = adapter.toJson(binding?.viewModel?.choreDetailView?.value)

                val navController = findNavController()
                val bundle = bundleOf("choreToEdit" to choreJson)
                navController.navigate(R.id.action_choreDetailFragment_to_editChoreFragment, bundle)

                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}
