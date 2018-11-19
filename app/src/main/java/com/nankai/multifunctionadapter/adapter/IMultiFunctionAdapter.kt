package com.nankai.multifunctionadapter.adapter

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil

interface IMultiFunctionAdapter<VH : MultiFunctionAdapter.Companion.MultiFunctionViewHolder, E> {

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

    fun setHeaderView(view: View?, orientation: Int)

    fun addHeaderView(view: View?, index: Int, orientation: Int)

    fun removeHeaderView(view: View?)

    fun removeAllHeaderView()

    //Footer
    fun setFooterView(view: View?)

    fun setFooterView(view: View?, orientation: Int)

    fun addFooterView(view: View?, index: Int, orientation: Int)

    fun removeFooterView(view: View?)

    fun removeAllFooterView()

    //LoadMore
    fun setOnLoadMoreListener(loadMoreListener: LoadMoreListener)

    fun enableLoadMore(enable: Boolean)

    fun setLoadMoreView(): LoadMoreView

    //Empty
    fun setEmptyView(view: View?)

    //binding data
    fun add(item: E?)

    fun add(collection: Collection<E>?)

    fun add(item: E?, index: Int)

    fun addAll(collection: Collection<E>?, index: Int)

    fun remove(item: E?)

    fun removeAll(collection: Collection<E>?)

    fun remove(index: Int)

    fun get(index: Int): E

    fun set(item: E?, index: Int)

    fun getAll(): Collection<E>

    fun clear()

    //DiffUtil

    /**
     * Update the current adapter state. If {@param callback} is provided, an updated data set is calculated with DiffUtil, otherwise
     * current data set is clear and {@param newItems} are added to the internal items collection.
     *
     * @param newItems Collection of new items, which are added to adapter.
     * @param callback DiffUtil callback, which is used to update the items.
     */
    fun update(newItems: Collection<E>?, callBack: DiffUtil.Callback?)

    /**
     * Sets the isCancelled flag to true, which will cancel DiffUtil.DiffResult update dispatch to the adapter. Call this method when
     * your activity or fragment is about to be destroyed.
     */
    fun cancel()

    /**
     * Sets the isCancelled flag to false, which will enable DiffUtil.DiffResult update dispatch to the adapter.
     */
    fun reset()
}