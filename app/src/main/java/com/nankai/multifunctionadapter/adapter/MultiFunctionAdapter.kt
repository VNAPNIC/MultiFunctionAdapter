package com.nankai.multifunctionadapter.adapter

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import java.util.concurrent.Executors

abstract class MultiFunctionAdapter<E, VH : MultiFunctionAdapter.Companion.MultiFunctionViewHolder>
    : RecyclerView.Adapter<VH>()
        , IMultiFunctionAdapter<VH, E> {

    private var items: MutableList<E> = ArrayList()

    private var pendingUpdates: Queue<Collection<E>> = ArrayDeque()
    private val executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val handler = Handler(Looper.getMainLooper())
    private var isCancelled = false

    private var loadMoreListener: IMultiFunctionAdapter.LoadMoreListener? = null

    //RecyclerView
    private var recyclerView: RecyclerView? = null

    private var mLoadMoreView: LoadMoreView? = null
    //header
    private var mHeaderLayout: LinearLayout? = null
    //Footer
    private var mFooterLayout: LinearLayout? = null
    //LoadMore
    private var isLoadMoreEnable = false

    var isReloadMore = false

    var isAutoLoadMore = false

    var isLoading: Boolean = false
        set(value) {
            if (isLoadMoreEnable) {
                field = value
                Log.i(TAG, "isLoading $field & position ${getLoadMoreViewPosition()}")
                if (!field) {
                    mLoadMoreView?.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
                    notifyItemChanged(getLoadMoreViewPosition())
                }
            } else {
                field = false
            }
        }

    @LoadMoreView.Companion.Status
    var status: Int = LoadMoreView.STATUS_DEFAULT
        set(value) {
            if (isLoadMoreEnable) {
                Log.i(TAG, "Status $field & position ${getLoadMoreViewPosition()}")
                if (value != mLoadMoreView?.loadMoreStatus) {
                    field = value
                    mLoadMoreView?.loadMoreStatus = field
                    notifyItemChanged(getLoadMoreViewPosition())
                }
            }
        }

    var headerLayoutCount: Int = 0
        get() = if (mHeaderLayout != null)
            mHeaderLayout!!.childCount
        else 0

    var footerLayoutCount: Int = 0
        get() = if (mFooterLayout != null)
            mFooterLayout!!.childCount
        else 0

    /**
     * Calculate the correct item index because RecyclerView doesn't distinguish
     * between header rows and item rows.
     *
     * We have 2 possible cases, which are defined with {@param isViewBinding} value:
     *
     * 1. If we are trying to bind the view, than the index value has to be decremented by 1 if adapter contains header
     * view.
     *
     * 2. If we are trying to perform some action on the adapter, that the index value has to be incremented by 1
     * if adapter contains header view.
     *
     * @param index         RecyclerView row index.
     * @param isViewBinding boolean value, which indicates whether we are trying to bind the view or perform some action on adapter.
     * @return correct item index.
     */
    private fun calculateIndex(index: Int, isViewBinding: Boolean): Int {
        val adjIndex: Int
        return if (isViewBinding) {
            adjIndex = index - headerLayoutCount

            if (adjIndex >= items.size) {
                throw IllegalStateException("Index is defined in wrong range!")
            } else {
                adjIndex
            }
        } else {
            adjIndex = index + headerLayoutCount
            adjIndex
        }
    }

    override fun getItemCount(): Int = items.size + footerLayoutCount + if (isLoadMoreEnable) 1 else 0

    private fun getLoadMoreViewPosition(): Int = itemCount - if (isLoadMoreEnable) 1 else 0

    override fun getContentDataSize(): Int = itemCount - footerLayoutCount - if (isLoadMoreEnable) 1 else 0

    override fun getItemViewType(position: Int): Int {
        if (position < headerLayoutCount) {
            return HEADER_VIEW
        }

        if (position < getContentDataSize()) {
            return onInjectItemViewType(getContentDataSize())
        }

        if (footerLayoutCount > 0 && position < itemCount - if (isLoadMoreEnable) 1 else 0) {
            return FOOTER_VIEW
        }

        return LOAD_MORE_VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = when (viewType) {
        HEADER_VIEW -> HeaderViewHolder(mHeaderLayout) as VH
        FOOTER_VIEW -> FooterViewHolder(mFooterLayout) as VH
        LOAD_MORE_VIEW -> {
            var view = View(parent.context)
            mLoadMoreView?.layoutId?.let {
                view = LayoutInflater.from(parent.context).inflate(it, parent, false)
            }
            LoadMoreViewHolder(view) as VH
        }
        else -> {
            onInjectViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val viewType = (holder as MultiFunctionViewHolder).itemViewType
        when (viewType) {
            HEADER_VIEW -> {
            }
            FOOTER_VIEW -> {
            }
            LOAD_MORE_VIEW -> {
                autoLoadMore(position)
                mLoadMoreView?.convert(holder)
                holder.itemView.setOnClickListener { _ ->
                    mLoadMoreView.let {
                        if (isReloadMore
                                && (
                                        it?.loadMoreStatus == LoadMoreView.STATUS_FAIL
                                                || it?.loadMoreStatus == LoadMoreView.STATUS_EMPTY
                                                || it?.loadMoreStatus == LoadMoreView.STATUS_END
                                        )
                        )
                            reloadMore()
                    }
                }
            }
            else -> {
                val adjPosition = position - headerLayoutCount
                onViewReady(holder, adjPosition)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    protected open fun onInjectItemViewType(position: Int): Int = super.getItemViewType(position)

    //---------------------- Header ---------------------------//
    override fun setHeaderView(view: View?) {
        mHeaderLayout?.removeAllViews()
        setHeaderView(view, LinearLayoutCompat.VERTICAL)
    }

    override fun setHeaderView(view: View?, orientation: Int?) {
        mHeaderLayout?.removeAllViews()
        addHeaderView(view, -1, orientation)
    }

    override fun addHeaderView(view: View?, index: Int?, orientation: Int?) {
        if (mHeaderLayout == null) {
            mHeaderLayout = LinearLayout(view?.context)
            if (orientation == LinearLayout.VERTICAL) {
                mHeaderLayout?.orientation = LinearLayout.VERTICAL
                mHeaderLayout?.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            } else {
                mHeaderLayout?.orientation = LinearLayout.HORIZONTAL
                mHeaderLayout?.layoutParams = RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            }
        }

        var newIndex: Int? = index

        if (newIndex!! < 0 || newIndex > headerLayoutCount) {
            newIndex = headerLayoutCount
        }

        mHeaderLayout?.addView(view, newIndex)
        if (headerLayoutCount > 0) {
            notifyDataSetChanged()
        }
    }

    override fun removeHeaderView(view: View?) {
        if (headerLayoutCount <= 0)
            return
        mHeaderLayout?.removeView(view)
        notifyDataSetChanged()
    }

    override fun removeAllHeaderView() {
        if (headerLayoutCount <= 0)
            return
        mHeaderLayout?.removeAllViews()
        notifyDataSetChanged()
    }

    //---------------------- Footer ---------------------------//
    override fun setFooterView(view: View?) {
        mFooterLayout?.removeAllViews()
        setFooterView(view, LinearLayoutCompat.VERTICAL)
    }

    override fun setFooterView(view: View?, orientation: Int?) {
        mFooterLayout?.removeAllViews()
        addFooterView(view, -1, orientation)
    }

    override fun addFooterView(view: View?, index: Int?, orientation: Int?) {
        if (mFooterLayout == null) {
            mFooterLayout = LinearLayout(view?.context)
            if (orientation == LinearLayout.VERTICAL) {
                mFooterLayout?.orientation = LinearLayout.VERTICAL
                mFooterLayout?.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            } else {
                mFooterLayout?.orientation = LinearLayout.HORIZONTAL
                mFooterLayout?.layoutParams = RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            }
        }

        var newIndex: Int? = index

        if (newIndex!! < 0 || newIndex > footerLayoutCount) {
            newIndex = footerLayoutCount
        }

        mFooterLayout?.addView(view, newIndex)
        if (footerLayoutCount > 0) {
            notifyDataSetChanged()
        }
    }

    override fun removeFooterView(view: View?) {
        if (footerLayoutCount <= 0)
            return
        mFooterLayout?.removeView(view)
        notifyDataSetChanged()
    }

    override fun removeAllFooterView() {
        if (footerLayoutCount <= 0)
            return
        mFooterLayout?.removeAllViews()
        notifyDataSetChanged()
    }

    //---------------------- LoadMore ---------------------------//
    override fun setOnLoadMoreListener(loadMoreListener: IMultiFunctionAdapter.LoadMoreListener) {
        loadMoreListener.let {
            this.loadMoreListener = it
            enableLoadMore(true)
        }
    }

    override fun enableLoadMore(enable: Boolean) {
        isLoadMoreEnable = true
        mLoadMoreView = setLoadMoreView()
    }

    private fun autoLoadMore(position: Int) {
        if (!isLoadMoreEnable)
            return
        if (position < getLoadMoreViewPosition())
            return
        if (mLoadMoreView?.loadMoreStatus != LoadMoreView.STATUS_DEFAULT)
            return
        if (!isAutoLoadMore)
            return
        if (isCancelled)
            return

        mLoadMoreView?.loadMoreStatus = LoadMoreView.STATUS_LOADING

        recyclerView?.post {
            // If RecyclerView is currently computing a layout, it's in a lockdown state and any
            // attempt to update adapter contents will result in an exception. In these cases, we need to postpone the change
            // using a Handler.
            handler.post {
                loadMoreListener?.onLoadMore()
            }
        }
    }

    private fun reloadMore() {
        if (!isLoadMoreEnable)
            return
        mLoadMoreView?.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
        notifyItemChanged(getLoadMoreViewPosition())
    }

    //---------------------- binding data ---------------------------//

    override fun add(item: E?) {
        item?.let {
            val position = items.size
            items.add(it)
            notifyItemChanged(calculateIndex(position, false))
        }
    }

    override fun add(collection: Collection<E>?) {
        collection?.let {
            val position = items.size
            items.addAll(it)
            notifyItemRangeInserted(calculateIndex(position, false), it.size)
        }
    }

    override fun add(item: E?, index: Int) {
        if (index > items.size) {
            throw IllegalStateException("Index is defined in wrong range!")
        } else {
            item?.let {
                items.add(index, it)
                notifyItemInserted(calculateIndex(index, false))
            }
        }
    }

    override fun addAll(collection: Collection<E>?, index: Int) {
        collection?.let {
            if (index >= items.size) {
                throw IllegalStateException("Index is defined in wrong range!")
            } else {
                items.addAll(index, it)
                notifyItemRangeInserted(calculateIndex(index, false), it.size)
            }
        }
    }

    override fun remove(item: E?) {
        item?.let {
            val position = items.indexOf(it)
            if (items.remove(it)) {
                notifyItemRemoved(calculateIndex(position, false))
            }
        }
    }

    override fun removeAll(collection: Collection<E>?) {
        collection?.let {
            if (items.removeAll(it)) {
                notifyDataSetChanged()
            }
        }
    }

    override fun remove(index: Int) {
        if (index >= items.size) {
            throw IllegalStateException("Index is defined in wrong range!")
        } else if (items.removeAt(index) != null) {
            notifyItemRemoved(calculateIndex(index, false))
        }
    }

    override fun get(index: Int): E {
        if (index >= items.size) {
            throw IllegalStateException("Index is defined in wrong range!")
        }
        return items[index]
    }

    override fun set(item: E?, index: Int) {
        item.let {
            items[index] = it!!
            notifyItemChanged(calculateIndex(index, false))
        }
    }

    override fun getAll(): Collection<E> {
        return items
    }

    /**
     * Clears current items.
     */
    override fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    //DiffUtil

    override fun cancel() {
        isCancelled = true
    }

    override fun reset() {
        isCancelled = false
    }

    /**
     * Update the current adapter state. If {@param callback} is provided, an updated data set is calculated with DiffUtil, otherwise
     * current data set is clear and {@param newItems} are added to the internal items collection.
     *
     * @param newItems Collection of new items, which are added to adapter.
     * @param callback DiffUtil callback, which is used to update the items.
     */
    override fun update(newItems: Collection<E>?, callBack: DiffUtil.Callback?) {
        newItems?.let {
            if (callBack != null) {
                pendingUpdates.add(newItems)
                if (pendingUpdates.size == 1) {
                    updateData(it, callBack)
                }
            } else {
                items.clear()
                items.addAll(it)
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Calculates provided {@param callback} DiffResult by using DiffUtils.
     *
     * @param newItems Collection of new items, with which our current items collection is updated.
     * @param callback DiffUtil.Callback on which DiffResult is calculated.
     */
    private fun updateData(newItems: Collection<E>, callback: DiffUtil.Callback) {
        executorService.execute {
            val diffResult = DiffUtil.calculateDiff(callback)
            postDiffResults(newItems, diffResult, callback)
        }
    }

    /**
     * Dispatched {@param diffResult} DiffResults to the adapter if adapter has not been cancelled. If there are any queued pending updates,
     * it will peek the latest new items collection and once again update the adapter content.
     *
     * @param newItems   Collection of new items, with which our current items collection is updated.
     * @param diffResult DiffUtil.DiffResult which was calculated for {@param callback}.
     * @param callback   DiffUtil.Callback on which DiffResult was calculated.
     */
    private fun postDiffResults(newItems: Collection<E>, diffResult: DiffUtil.DiffResult, callback: DiffUtil.Callback) {
        if (!isCancelled) {
            handler.post {
                pendingUpdates.remove()
                diffResult.dispatchUpdatesTo(this@MultiFunctionAdapter)
                items.clear()
                items.addAll(newItems)
                if (pendingUpdates.size > 0) {
                    updateData(pendingUpdates.peek(), callback)
                }
            }
        }
    }

    //=================================== Inner class ============================================//
    companion object {
        private val TAG: String = MultiFunctionAdapter::class.java.simpleName

        open class MultiFunctionViewHolder internal constructor(rootView: View?) : RecyclerView.ViewHolder(rootView!!)

        class HeaderViewHolder internal constructor(view: View?) : MultiFunctionViewHolder(view)

        class FooterViewHolder internal constructor(view: View?) : MultiFunctionViewHolder(view)

        class LoadMoreViewHolder internal constructor(view: View?) : MultiFunctionViewHolder(view)

    }
}
