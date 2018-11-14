package com.nankai.multifunctionadapter.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nankai.multifunctionadapter.R
import com.nankai.multifunctionadapter.adapter.MultiFunctionAdapter
import com.nankai.multifunctionadapter.repository.Data

class TestAdapter : MultiFunctionAdapter<Data, TestAdapter.Companion.TestViewHolder>(TestDiffCallBack()) {

    override fun onInjectViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        return TestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_test, parent, false))
    }

    override fun onViewReady(holder: TestViewHolder, item: Data, adjPosition: Int) {
        holder.bind(item)
    }

    companion object {
        class TestViewHolder internal constructor(view: View?) : MultiFunctionAdapter.Companion.MultiFunctionViewHolder(view) {
            fun bind(item: Data) {
                itemView.findViewById<TextView>(R.id.title).text = item.name
            }
        }
    }
}