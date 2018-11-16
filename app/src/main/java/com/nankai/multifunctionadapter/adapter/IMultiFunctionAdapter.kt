package com.nankai.multifunctionadapter.adapter

import android.view.View
import android.view.ViewGroup

interface IMultiFunctionAdapter<VH : MultiFunctionAdapter.Companion.MultiFunctionViewHolder,E> {

    val HEADER_VIEW: Int
        get() = -1
    val FOOTER_VIEW: Int
        get() = -2
    val LOAD_MORE_VIEW: Int
        get() = -3
    val EMPTY_VIEW: Int
        get() = -4

    interface LoadMoreListener {
        fun onLoadMore()
    }

    fun onInjectViewHolder(parent: ViewGroup, viewType: Int): VH

    fun onViewReady(holder: VH, adjPosition: Int)

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

    //LoadMore
    fun setOnLoadMoreListener(loadMoreListener: LoadMoreListener)

    fun enableLoadMore(enable: Boolean)

    fun setLoadMoreView(): LoadMoreView

    //binding data
    fun setNewData(newData: List<E>)
}