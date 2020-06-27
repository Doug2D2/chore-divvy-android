package com.doug2d2.chore_divvy_android.chore

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.*
import android.widget.PopupMenu
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.databinding.ChoreItemBinding

class ChoreListAdapter(val clickListener: ChoreListClickListener, val choreListViewModel: ChoreListViewModel): ListAdapter<Chore, ChoreListAdapter.ChoreListViewHolder>(DiffCallback) {
    companion object DiffCallback: DiffUtil.ItemCallback<Chore>() {
        override fun areItemsTheSame(oldItem: Chore, newItem: Chore): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chore, newItem: Chore): Boolean {
            return oldItem == newItem
        }
    }

    class ChoreListViewHolder(private var binding: ChoreItemBinding, val choreListViewModel: ChoreListViewModel): RecyclerView.ViewHolder(binding.root), View.OnLongClickListener {
        init {
            binding.choreItem.setOnLongClickListener(this)
        }

        fun bind(listener: ChoreListClickListener, chore: Chore) {
            binding.chore = chore
            binding.clickListener = listener
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }

        // onLongClick is called when a chore is long clicked
        override fun onLongClick(v: View?): Boolean {
            // Create popup menu to show Edit and Delete chore options
            var popupMenu: PopupMenu = PopupMenu(v?.context, v)
            popupMenu.inflate(R.menu.chore_pop_up)
            popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
                when(item?.itemId) {
                    R.id.chore_edit -> {
                        choreListViewModel.onEditChore(binding.chore!!)
                    }
                    R.id.chore_delete -> {
                        // Create Alert Dialog to ask user if they are sure that they want to delete chore
                        val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(ContextThemeWrapper(v?.context, R.style.CustomAlertDialog))
                        lateinit var alert: AlertDialog
                        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    // Yes, delete chore
                                    choreListViewModel.deleteChore(binding.chore!!)
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
                }
                true
            }

            popupMenu.show()

            return true
        }

        companion object {
            fun from(parent: ViewGroup, choreListViewModel: ChoreListViewModel): ChoreListViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ChoreItemBinding.inflate(layoutInflater, parent, false)
                return ChoreListViewHolder(binding, choreListViewModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoreListViewHolder {
        return ChoreListViewHolder.from(parent,choreListViewModel)
    }

    /**
     * Part of the RecyclerView adapter, called when RecyclerView needs to show an item.
     *
     * The ViewHolder passed may be recycled, so make sure that this sets any properties that
     * may have been set previously.
     */
    override fun onBindViewHolder(holder: ChoreListViewHolder, position: Int) {
        holder.bind(clickListener, getItem(position))
    }
}

class ChoreListClickListener(val clickListener: (chore: Chore) -> Unit) {
    fun onClick(chore: Chore) = clickListener(chore)
}
