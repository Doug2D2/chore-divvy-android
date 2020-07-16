package com.doug2d2.chore_divvy_android

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doug2d2.chore_divvy_android.chore.ChoreListAdapter
import com.doug2d2.chore_divvy_android.database.FullChore

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<FullChore>?) {
    val adapter = recyclerView.adapter as ChoreListAdapter
    adapter.submitList(data) {
        // scroll the list to the top after the diffs are calculated and posted
        recyclerView.scrollToPosition(0)
    }
}

@BindingAdapter("checkboxImage")
fun ImageView.setCheckboxImage(chore: FullChore) {
    setImageResource(when (chore.status) {
        "Completed" -> R.drawable.baseline_check_box_black_18dp
        else -> R.drawable.baseline_check_box_outline_blank_black_18dp
    })
}
