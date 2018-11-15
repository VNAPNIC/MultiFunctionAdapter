package com.nankai.multifunctionadapter.adapter

import android.view.View

interface IMultiFunctionAdapter {

    val HEADER_VIEW: Int
        get() = 0x00000111
    val LOAD_MORE_VIEW: Int
        get() = 0x00000222
    val FOOTER_VIEW: Int
        get() = 0x00000333
    val EMPTY_VIEW: Int
        get() = 0x00000555

    /**
     * child class extend it
     * it is callback of item view to Main
     */
    interface AdapterListener<E>

    fun getContentDataSize(): Int

    //Header
    fun setHeaderView(view: View?)

    fun setHeaderView(view: View?, orientation: Int?)

    fun addHeaderView(view: View?, index: Int?, orientation: Int?)

    fun removeHeaderView(view: View?)

    fun removeAllHeaderView()

    //Footer
    fun setFooterView(view: View?)

    fun setFooterView(view: View?, orientation: Int?)

    fun addFooterView(view: View?, index: Int?, orientation: Int?)

    fun removeFooterView(view: View?)

    fun removeAllFooterView()
}