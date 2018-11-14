package com.nankai.multifunctionadapter.model

import com.nankai.multifunctionadapter.repository.Data

class DummyData {
    companion object {
        fun getDummyData(): MutableList<Data> {
            val listItem: MutableList<Data> = ArrayList()

            for (i in 0..50) {
                listItem.add(Data(i, "Name $i"))
            }
            return listItem
        }
    }
}