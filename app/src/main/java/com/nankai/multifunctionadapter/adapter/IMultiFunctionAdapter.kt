package com.nankai.multifunctionadapter.adapter

import android.view.View

interface IMultiFunctionAdapter {

    val HEADER_VIEW: Int
        get() = 0x00000111
    val LOADING_VIEW: Int
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

    fun setHeaderView(view: View?)

    fun setHeaderView(view: View?, index: Int?)

    fun addHeaderView(view: View?)

    fun addHeaderView(view: View?, orientation: Int?)

    fun addHeaderView(view: View?, index: Int?, orientation: Int?)

    fun removeHeaderView(view: View?)

    fun removeAllHeaderView()
}