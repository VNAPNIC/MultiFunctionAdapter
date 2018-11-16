package com.nankai.multifunctionadapter.test

import com.nankai.multifunctionadapter.repository.Data
import android.os.Bundle
import com.nankai.multifunctionadapter.adapter.MultiFunctionDiffCallBack

class TestDiffCallBack(newList: List<Data>?, oldList: List<Data>?) : MultiFunctionDiffCallBack<Data>(newList, oldList) {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if ((newList == null || oldList == null) || (newList!!.isEmpty() || oldList!!.isEmpty()))
            return false
        return newList!![newItemPosition].id == oldList!![oldItemPosition].id
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val newData = newList?.get(newItemPosition)
        val oldData = oldList?.get(oldItemPosition)

        val diff = Bundle()
        if (newData?.id != oldData?.id) {
            newData?.id?.let { diff.putInt("id", it) }
        }
        if (!newData?.name.equals(oldData?.name)) {
            diff.putString("name", newData?.name)
        }
        return if (diff.size() == 0) {
            null
        } else diff
    }
}