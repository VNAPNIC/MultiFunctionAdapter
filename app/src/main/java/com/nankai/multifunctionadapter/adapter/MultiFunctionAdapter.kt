package com.nankai.multifunctionadapter.adapter

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


abstract class MultiFunctionAdapter<E, VH : MultiFunctionAdapter.Companion.MultiFunctionViewHolder>
    : RecyclerView.Adapter<VH>()
        , IMultiFunctionAdapter<VH, E> {

    protected var currentList: MutableList<E> = ArrayList()

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

    override fun getItemCount(): Int = currentList.size + footerLayoutCount + if (isLoadMoreEnable) 1 else 0

    private fun getLoadMoreViewPosition(): Int = itemCount - 1

    override fun getContentDataSize(): Int = itemCount - footerLayoutCount - if (isLoadMoreEnable) 1 else 0

    override fun getItemViewType(position: Int): Int {
        Log.i(TAG, "getItemViewType position = $position")
        if (position < headerLayoutCount) {
            Log.i(TAG, "getItemViewType viewType = HEADER_VIEW")
            return HEADER_VIEW
        }

        if (position < getContentDataSize()) {
            Log.i(TAG, "getItemViewType viewType = onInjectItemViewType")
            return onInjectItemViewType(getContentDataSize())
        }

        if (footerLayoutCount > 0 && position < itemCount - if (isLoadMoreEnable) 1 else 0) {
            Log.i(TAG, "getItemViewType viewType = FOOTER_VIEW")
            return FOOTER_VIEW
        }

        Log.i(TAG, "getItemViewType viewType = LOAD_MORE_VIEW")
        return LOAD_MORE_VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = when (viewType) {
        HEADER_VIEW -> HeaderViewHolder(mHeaderLayout) as VH
        FOOTER_VIEW -> FooterViewHolder(mFooterLayout) as VH
        LOAD_MORE_VIEW -> {
            Log.i(TAG, "onCreateViewHolder viewType = LOAD_MORE_VIEW")
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
        Log.i(TAG, "onBindViewHolder viewType = $viewType")
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
        mLoadMoreView?.loadMoreStatus = LoadMoreView.STATUS_LOADING

        recyclerView?.post {
            loadMoreListener?.onLoadMore()
        }
    }

    private fun reloadMore() {
        mLoadMoreView?.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
        notifyItemChanged(getLoadMoreViewPosition())
    }

    //---------------------- binding data ---------------------------//
    override fun setNewData(newData: List<E>) {
        val diffResult = DiffUtil.calculateDiff(MultiFunctionDiffCallBack(newData, currentList))
        diffResult.dispatchUpdatesTo(this)
        this.currentList.clear()
        this.currentList.addAll(newData)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        Log.i(TAG, "onAttachedToRecyclerView $recyclerView")
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        Log.i(TAG, "onDetachedFromRecyclerView $recyclerView")
        this.recyclerView = null
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
