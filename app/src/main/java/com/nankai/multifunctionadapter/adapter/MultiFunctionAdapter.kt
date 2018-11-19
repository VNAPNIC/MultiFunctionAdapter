package com.nankai.multifunctionadapter.adapter

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.*
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

    /*
    * RecyclerView
    */
    private var recyclerView: RecyclerView? = null

    /*
      * Header
      */
    /**
     * if asFlow is true,header will arrange like normal item view.
     * only works when use [GridLayoutManager],and it will ignore span size.
     */
    var headerViewAsFlow: Boolean = false
    private var mHeaderLayout: LinearLayout? = null
    var headerLayoutCount: Int = 0
        get() = if (mHeaderLayout != null)
            mHeaderLayout!!.childCount
        else 0

    /*
    * Footer
    */
    /**
     * if asFlow is true,footer will arrange like normal item view.
     * only works when use [GridLayoutManager],and it will ignore span size.
     */
    var footerViewAsFlow: Boolean = false
    private var mFooterLayout: LinearLayout? = null
    var footerLayoutCount: Int = 0
        get() = if (mFooterLayout != null)
            mFooterLayout!!.childCount
        else 0

    /*
    * LoadMore
    */
    private var isLoadMoreEnable = false
    private var mLoadMoreView: LoadMoreView? = null

    var isReloadMore = false

    var isAutoLoadMore = false

    var isLoading: Boolean = false
        set(value) {
            if (isLoadMoreEnable) {
                field = value
                if (!field) {
                    mLoadMoreView?.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
                    notifyItemChanged(getLoadMoreViewPosition())
                }
            } else {
                field = false
            }
        }

    @LoadMoreView.Companion.Status
    var loadMoreStatus: Int = LoadMoreView.STATUS_DEFAULT
        set(value) {
            if (isLoadMoreEnable) {
                if (value != mLoadMoreView?.loadMoreStatus) {
                    field = value

                    if (field == LoadMoreView.STATUS_END) {
                        mLoadMoreView?.loadMoreStatus = field
                        notifyItemRemoved(itemCount)
                    } else {
                        if (mLoadMoreView?.loadMoreStatus == LoadMoreView.STATUS_END) {
                            mLoadMoreView?.loadMoreStatus = field
                            notifyItemInserted(itemCount)
                        } else {
                            mLoadMoreView?.loadMoreStatus = field
                            notifyItemChanged(getLoadMoreViewPosition())
                        }
                    }
                }
            }
        }

    private var loadMoreViewCount: Int = 0
        get() : Int {
            return when {
                items.size <= 0 -> 0
                loadMoreStatus == LoadMoreView.STATUS_END -> 0
                isLoadMoreEnable -> 1
                else -> 0
            }
        }
    /*
     * Empty Layout
     */
    private var mEmptyLayout: FrameLayout? = null
    var emptyLayoutCount: Int = 0
        get() = if (mEmptyLayout != null)
            mEmptyLayout!!.childCount
        else 0
    var enableEmpty: Boolean = false
        set(value) {
            recyclerView?.recycledViewPool?.clear()
            field = value
            notifyDataSetChanged()
        }

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

    override fun getItemCount(): Int =
            if (enableEmpty)
                emptyLayoutCount + headerLayoutCount
            else
                items.size + footerLayoutCount + loadMoreViewCount

    private fun getLoadMoreViewPosition(): Int = itemCount - loadMoreViewCount

    override fun getContentDataSize(): Int = itemCount - footerLayoutCount - loadMoreViewCount

    override fun getItemViewType(position: Int): Int {
        if (position < headerLayoutCount)
            return HEADER_VIEW

        if (enableEmpty && emptyLayoutCount > 0)
            return EMPTY_VIEW

        if (position < getContentDataSize())
            return onInjectItemViewType(getContentDataSize())

        if (footerLayoutCount > 0 && position < itemCount - loadMoreViewCount)
            return FOOTER_VIEW

        return LOAD_MORE_VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = when (viewType) {

        HEADER_VIEW -> HeaderViewHolder(mHeaderLayout) as VH
        EMPTY_VIEW -> EmptyViewHolder(mEmptyLayout) as VH
        FOOTER_VIEW -> FooterViewHolder(mFooterLayout) as VH
        LOAD_MORE_VIEW -> {
            var view = View(parent.context)
            mLoadMoreView?.layoutId?.let {
                view = LayoutInflater.from(parent.context).inflate(it, parent, false)
            }
            LoadMoreViewHolder(view) as VH
        }
        else -> onInjectViewHolder(parent, viewType)

    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val viewType = (holder as MultiFunctionViewHolder).itemViewType
        when (viewType) {
            HEADER_VIEW -> {
            }
            EMPTY_VIEW -> {
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

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        val type = holder.itemViewType
        if (isFixedViewType(type))
            setFullSpan(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        val manage: RecyclerView.LayoutManager? = recyclerView.layoutManager
        manage?.let {
            if (it is GridLayoutManager) {
                val gridLayoutManager: GridLayoutManager = manage as GridLayoutManager
                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        val type = getItemViewType(position)
                        if (type == HEADER_VIEW && headerViewAsFlow)
                            return 1
                        if (type == FOOTER_VIEW && footerViewAsFlow)
                            return 1
                        return if (isFixedViewType(type)) gridLayoutManager.spanCount else 1
                    }
                }
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        cancel()
        this.mHeaderLayout = null
        this.mFooterLayout = null
        this.mLoadMoreView = null
        this.loadMoreListener = null
        this.recyclerView = null
    }

    protected open fun onInjectItemViewType(position: Int): Int = super.getItemViewType(position)

    private fun setFullSpan(holder: RecyclerView.ViewHolder) {
        if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            val params: StaggeredGridLayoutManager.LayoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            params.isFullSpan = true
        }
    }

    private fun isFixedViewType(type: Int): Boolean {
        return type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOAD_MORE_VIEW
    }

    /**
     * Sets the default layout params to the provided {@param view} if they are not yet set. Default params are MATCH_PARENT for layout
     * width and WRAP_CONTENT for layout height.
     *
     * @param view View for which we want to set default layout params.
     */
    private fun setDefaultLayoutParams(view: LinearLayout?, orientation: Int) {
        val layoutParams: RecyclerView.LayoutParams?
        if (orientation == LinearLayoutManager.VERTICAL) {
            layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT)
        } else {
            layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                    RecyclerView.LayoutParams.MATCH_PARENT)
        }
        view?.orientation = orientation
        view?.layoutParams = layoutParams
    }

    /*
     * Header
     */
    override fun setHeaderView(view: View?) {
        mHeaderLayout?.removeAllViews()
        setHeaderView(view, LinearLayoutCompat.VERTICAL)
    }

    override fun setHeaderView(view: View?, orientation: Int) {
        mHeaderLayout?.removeAllViews()
        addHeaderView(view, -1, orientation)
    }

    override fun addHeaderView(view: View?, index: Int, orientation: Int) {
        view?.let {
            if (mHeaderLayout == null) {
                mHeaderLayout = LinearLayout(it.context)
                setDefaultLayoutParams(mHeaderLayout!!, orientation)
            }
            mHeaderLayout?.run {
                addView(it)
                notifyDataSetChanged()
            }
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

    /*
    * Footer
    */
    override fun setFooterView(view: View?) {
        mFooterLayout?.removeAllViews()
        setFooterView(view, LinearLayoutCompat.VERTICAL)
    }

    override fun setFooterView(view: View?, orientation: Int) {
        mFooterLayout?.removeAllViews()
        addFooterView(view, -1, orientation)
    }

    override fun addFooterView(view: View?, index: Int, orientation: Int) {
        view?.let {
            if (mFooterLayout == null) {
                mFooterLayout = LinearLayout(it.context)
                setDefaultLayoutParams(mFooterLayout!!, orientation)
            }
            mFooterLayout?.run {
                addView(it)
                notifyDataSetChanged()
            }
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

    /*
     * LoadMore
     */
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
        if (loadMoreViewCount <= 0)
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

    /*
    * Layout Empty
    */
    override fun setEmptyView(view: View?) {
        view?.let {
            if (mEmptyLayout == null) {
                mEmptyLayout = FrameLayout(view.context)
                val layoutParams: RecyclerView.LayoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.MATCH_PARENT)
                mEmptyLayout?.layoutParams = layoutParams
            }
            mEmptyLayout?.run {
                addView(it)
                notifyDataSetChanged()
            }
        }
    }

    /*
         * Data binding
         */
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
                items.add(calculateIndex(index, false), it)
                notifyItemInserted(calculateIndex(index, false))
            }
        }
    }

    override fun addAll(collection: Collection<E>?, index: Int) {
        collection?.let {
            if (index >= items.size) {
                throw IllegalStateException("Index is defined in wrong range!")
            } else {
                items.addAll(calculateIndex(index, false), it)
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
        } else if (items.removeAt(calculateIndex(index, false)) != null) {
            notifyItemRemoved(calculateIndex(index, false))
        }
    }

    override fun get(index: Int): E {
        if (index >= items.size) {
            throw IllegalStateException("Index is defined in wrong range!")
        }
        return items[calculateIndex(index, false)]
    }

    override fun set(item: E?, index: Int) {
        item.let {
            items[calculateIndex(index, false)] = it!!
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

        class EmptyViewHolder internal constructor(view: View?) : MultiFunctionViewHolder(view)

        class FooterViewHolder internal constructor(view: View?) : MultiFunctionViewHolder(view)

        class LoadMoreViewHolder internal constructor(view: View?) : MultiFunctionViewHolder(view)

    }
}
