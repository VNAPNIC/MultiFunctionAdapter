package com.nankai.multifunctionadapter.adapter

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class MultiFunctionAdapter<E, VH : MultiFunctionAdapter.Companion.MultiFunctionViewHolder>(diffUtil: DiffUtil.ItemCallback<E>)
    : ListAdapter<E, VH>(diffUtil)
        , IMultiFunctionAdapter {

    //header
    private var mHeaderLayout: LinearLayout? = null
    //Footer
    private var mFooterLayout: LinearLayout? = null

    private var isLoadMore = false

    public var headerLayoutCount: Int = 0
        get() = if (mHeaderLayout != null)
            mHeaderLayout!!.childCount
        else 0

    public var footerLayoutCount: Int = 0
        get() = if (mFooterLayout != null)
            mFooterLayout!!.childCount
        else 0

    override fun getItemCount(): Int {
        return super.getItemCount() + footerLayoutCount + if (isLoadMore) 1 else 0
    }

    override fun getContentDataSize(): Int {
        return itemCount - footerLayoutCount - if (isLoadMore) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {

        if (position < headerLayoutCount)
            return HEADER_VIEW

        if (footerLayoutCount > 0 && position == getContentDataSize() - if (isLoadMore) 1 else 0)
            return FOOTER_VIEW

        if (position < getContentDataSize())
            return onInjectItemViewType(getContentDataSize())

        return LOADING_VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return when (viewType) {
            HEADER_VIEW -> HeaderViewHolder(mHeaderLayout) as VH
            FOOTER_VIEW -> FooterViewHolder(mFooterLayout) as VH
            else -> {
                onInjectViewHolder(parent, viewType)
            }
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val viewType = (holder as MultiFunctionViewHolder).itemViewType
        when (viewType) {
            HEADER_VIEW -> {
            }
            FOOTER_VIEW -> {
            }
            else -> {
                val adjPosition = position - footerLayoutCount
                onViewReady(holder, adjPosition)
            }
        }
    }

    protected open fun onInjectItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    abstract fun onInjectViewHolder(parent: ViewGroup, viewType: Int): VH

    abstract fun onViewReady(holder: VH, adjPosition: Int)

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

    //=================================== Inner class ============================================//
    companion object {
        open class MultiFunctionViewHolder internal constructor(rootView: View?) : RecyclerView.ViewHolder(rootView!!)

        class HeaderViewHolder internal constructor(view: View?) : MultiFunctionViewHolder(view)

        class FooterViewHolder internal constructor(view: View?) : MultiFunctionViewHolder(view)

    }
}
