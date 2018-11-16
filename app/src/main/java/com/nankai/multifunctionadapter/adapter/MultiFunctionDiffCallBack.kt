package com.nankai.multifunctionadapter.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil

open class MultiFunctionDiffCallBack<E>(var newList: List<E>?, var oldList: List<E>?) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return true
    }

    override fun getOldListSize(): Int {
        return if (oldList != null) newList!!.size else 0
    }

    override fun getNewListSize(): Int {
        return if (newList != null) newList!!.size else 0
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if ((newList == null || oldList == null) || (newList!!.isEmpty() || oldList!!.isEmpty()))
            return false
        return newList!![newItemPosition] == oldList!![oldItemPosition]
    }
}