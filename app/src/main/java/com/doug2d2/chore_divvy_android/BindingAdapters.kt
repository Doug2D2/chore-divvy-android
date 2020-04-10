package com.doug2d2.chore_divvy_android

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doug2d2.chore_divvy_android.chore.ChoreListAdapter
import com.doug2d2.chore_divvy_android.database.Chore

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Chore>?) {
    val adapter = recyclerView.adapter as ChoreListAdapter
    adapter.submitList(data) {
        // scroll the list to the top after the diffs are calculated and posted
        recyclerView.scrollToPosition(0)
    }
}

/*
@BindingAdapter("showOnlyWhenEmpty")
fun View.showOnlyWhenEmpty(data: List<Chore>?) {
    visibility = when {
        data == null || data.isEmpty() -> View.VISIBLE
        else -> View.GONE
    }
}
*/
