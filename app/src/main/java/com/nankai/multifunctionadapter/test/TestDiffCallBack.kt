package com.nankai.multifunctionadapter.test

import com.nankai.multifunctionadapter.repository.Data
import android.os.Bundle
import com.nankai.multifunctionadapter.adapter.MultiFunctionDiffCallBack

class TestDiffCallBack(newList: MutableList<Data>?, oldList: MutableList<Data>?) : MultiFunctionDiffCallBack<Data>(newList, oldList) {

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val newContact = newList?.get(newItemPosition)
        val oldContact = oldList?.get(oldItemPosition)

        val diff = Bundle()
        if (!newContact?.name.equals(oldContact?.name)) {
            diff.putString("name", newContact?.name)
        }
        return if (diff.size() == 0) {
            null
        } else diff
    }
}