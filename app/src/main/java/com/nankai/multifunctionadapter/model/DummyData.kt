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

        fun getNewDummyData(listItem: MutableList<Data>): MutableList<Data> {
            val newList = ArrayList<Data>()
            for (i in 0..5) {
                newList.add(Data(listItem.size + i, "Name ${listItem.size + i}"))
            }
            return newList
        }

        fun getOneDummyData(listItem: MutableList<Data>): MutableList<Data> {
            val newList = ArrayList<Data>()
            newList.add(Data(listItem.size, "Name ${listItem.size}"))
            return newList
        }
    }
}