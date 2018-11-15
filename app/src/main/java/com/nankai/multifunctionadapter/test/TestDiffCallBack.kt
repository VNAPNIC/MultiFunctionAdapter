package com.nankai.multifunctionadapter.test

import androidx.recyclerview.widget.DiffUtil
import com.nankai.multifunctionadapter.repository.Data

class TestDiffCallBack : DiffUtil.ItemCallback<Data>() {

    override fun areContentsTheSame(
            oldItem: Data,
            newItem: Data): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
            oldItem: Data,
            newItem: Data): Boolean {
        return oldItem.id == newItem.id
    }
}