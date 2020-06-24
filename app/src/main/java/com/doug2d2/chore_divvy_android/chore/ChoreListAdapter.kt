package com.doug2d2.chore_divvy_android.chore

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.databinding.ChoreItemBinding
import kotlinx.android.synthetic.main.chore_item.view.*
import timber.log.Timber

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

        override fun onLongClick(v: View?): Boolean {
            var popupMenu: PopupMenu = PopupMenu(v?.context, v)
            popupMenu.inflate(R.menu.chore_pop_up)
            popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
                when(item?.itemId) {
                    R.id.chore_edit -> Timber.i("Edit chore")
                    R.id.chore_delete -> {
                        choreListViewModel.deleteChore(binding.chore!!)
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
