package com.nankai.multifunctionadapter.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.*

abstract class MultiFunctionAdapter<E, VH : MultiFunctionAdapter.Companion.MultiFunctionViewHolder>(diffCallback: DiffUtil.ItemCallback<E>) : ListAdapter<E, VH>(diffCallback), IMultiFunctionAdapter {

    private var mLayoutInflater: LayoutInflater? = null
    protected var data: MutableList<E> = ArrayList()

    //header
    private var mHeaderLayout: LinearLayout? = null

    public var headerLayoutCount: Int = 0
        get() = if (mHeaderLayout != null)
            mHeaderLayout!!.childCount
        else 0

    override fun getItemCount(): Int {
        return headerLayoutCount + data.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position < headerLayoutCount)
            return HEADER_VIEW

        val adjPosition = position - headerLayoutCount
//        val adapterItemCount = data.size
//        if (adjPosition < adapterItemCount)
        return onInjectItemViewType(adjPosition)
//        adjPosition -= adapterItemCount
//        return onInjectItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        mLayoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HEADER_VIEW -> {
                HeaderViewHolder(mHeaderLayout) as VH
            }
            else -> {
                onInjectViewHolder(parent, viewType)
            }
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val viewType = (holder as MultiFunctionViewHolder).itemViewType

        when (viewType) {
            HEADER_VIEW -> {
                //TODO
            }
            else -> {
                val adjPosition = position - headerLayoutCount
                onViewReady(holder, data[adjPosition], adjPosition)
            }
        }
    }

    override fun setHeaderView(view: View?) {
        setHeaderView(view, 0)
    }

    override fun setHeaderView(view: View?, index: Int?) {
        addHeaderView(view, 0, LinearLayoutCompat.VERTICAL)
    }

    override fun addHeaderView(view: View?) {
        val increase = headerLayoutCount.plus(1)
        addHeaderView(view, increase, LinearLayoutCompat.VERTICAL)
    }

    override fun addHeaderView(view: View?, orientation: Int?) {
        val increase = headerLayoutCount.plus(1)
        addHeaderView(view, increase, orientation)
    }

    override fun addHeaderView(view: View?, index: Int?, orientation: Int?) {
        if (mHeaderLayout == null) {
            mHeaderLayout = LinearLayout(view?.context)
            if (orientation == LinearLayout.VERTICAL) {
                mHeaderLayout!!.orientation = LinearLayout.VERTICAL
                mHeaderLayout!!.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            } else {
                mHeaderLayout!!.orientation = LinearLayout.HORIZONTAL
                mHeaderLayout!!.layoutParams = RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            }
        }
        var newIndex: Int? = index

        val childCount = headerLayoutCount
        if (newIndex!! < 0 || newIndex > childCount) {
            newIndex = childCount
        }

        mHeaderLayout!!.addView(view, newIndex)
        if (headerLayoutCount > 0) {
            notifyDataSetChanged()
        }
    }

    override fun removeHeaderView(view: View?) {
        if (headerLayoutCount <= 0)
            return
        mHeaderLayout!!.removeView(view)
        notifyDataSetChanged()
    }


    override fun removeAllHeaderView() {
        if (headerLayoutCount <= 0)
            return
        mHeaderLayout!!.removeAllViews()
        notifyDataSetChanged()
    }

    protected open fun onInjectItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    abstract fun onInjectViewHolder(parent: ViewGroup, viewType: Int): VH

    abstract fun onViewReady(holder: VH, item: E, adjPosition: Int)

    //=================================== Inner class ============================================//
    companion object {
        open class MultiFunctionViewHolder internal constructor(rootView: View?) : RecyclerView.ViewHolder(rootView!!)

        class HeaderViewHolder internal constructor(view: View?) : MultiFunctionViewHolder(view)

    }
}
