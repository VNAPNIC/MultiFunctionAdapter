package com.nankai.multifunctionadapter.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nankai.multifunctionadapter.R
import com.nankai.multifunctionadapter.adapter.LoadMoreView
import com.nankai.multifunctionadapter.adapter.MultiFunctionAdapter
import com.nankai.multifunctionadapter.repository.Data
import kotlinx.android.synthetic.main.item_test.view.*

class TestAdapter : MultiFunctionAdapter<Data, TestAdapter.TestViewHolder>() {

    override fun onInjectViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        return TestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_test, parent, false))
    }


    override fun onViewReady(holder: TestViewHolder, position: Int) {
        holder.bind(get(position), position)
    }

    override fun setLoadMoreView(): LoadMoreView {
        return LoadMoreTest()
    }

    class TestViewHolder constructor(view: View?) : MultiFunctionAdapter.MultiFunctionViewHolder(view) {
        fun bind(item: Data, position: Int) {
            itemView.title.text = item.name
        }
    }
}