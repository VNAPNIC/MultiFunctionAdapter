package com.nankai.multifunctionadapter.adapter

import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes


abstract class LoadMoreView {

    companion object {
        const val STATUS_DEFAULT = 1
        const val STATUS_LOADING = 2
        const val STATUS_FAIL = 3
        const val STATUS_END = 4
        const val STATUS_EMPTY = 5
    }

    public var loadMoreStatus = STATUS_DEFAULT

    /**
     * load more layout
     *
     * @return LayoutRes
     */
    @get:LayoutRes
    public abstract val layoutId: Int

    /**
     * layout_loading view
     *
     * @return @IdRes
     */
    @get:IdRes
    public abstract val loadingViewId: Int

    /**
     * load fail view
     *
     * @return @IdRes
     */
    @get:IdRes
    public abstract val loadFailViewId: Int

    /**
     * load end view, you can return 0
     *
     * @return @IdRes
     */
    @get:IdRes
    public abstract val loadEndViewId: Int

    /**
     * load end view, you can return 0
     *
     * @return @IdRes
     */
    @get:IdRes
    public abstract val loadEmptyViewId: Int

    fun convert(holder: MultiFunctionAdapter.Companion.MultiFunctionViewHolder) {
        Log.i(LoadMoreView::class.java.simpleName, "load more status: $loadMoreStatus")
        when (loadMoreStatus) {
            STATUS_LOADING -> {
                visibleEmpty(holder, false)
                visibleLoading(holder, true)
                visibleLoadFail(holder, false)
                visibleLoadEnd(holder, false)
            }
            STATUS_FAIL -> {
                visibleEmpty(holder, false)
                visibleLoading(holder, false)
                visibleLoadFail(holder, true)
                visibleLoadEnd(holder, false)
            }
            STATUS_END -> {
                visibleEmpty(holder, false)
                visibleLoading(holder, false)
                visibleLoadFail(holder, false)
                visibleLoadEnd(holder, true)
            }
            STATUS_DEFAULT -> {
                visibleEmpty(holder, false)
                visibleLoading(holder, false)
                visibleLoadFail(holder, false)
                visibleLoadEnd(holder, false)
            }
            STATUS_EMPTY -> {
                visibleEmpty(holder, true)
                visibleLoading(holder, false)
                visibleLoadFail(holder, false)
                visibleLoadEnd(holder, false)
            }
        }
    }

    private fun visibleLoading(holder: MultiFunctionAdapter.Companion.MultiFunctionViewHolder, visible: Boolean) {
        setVisible(holder, loadingViewId, visible)
    }

    private fun visibleEmpty(holder: MultiFunctionAdapter.Companion.MultiFunctionViewHolder, visible: Boolean) {
        setVisible(holder, loadEmptyViewId, visible)
    }

    private fun visibleLoadFail(holder: MultiFunctionAdapter.Companion.MultiFunctionViewHolder, visible: Boolean) {
        setVisible(holder, loadFailViewId, visible)
    }

    private fun visibleLoadEnd(holder: MultiFunctionAdapter.Companion.MultiFunctionViewHolder, visible: Boolean) {
        val loadEndViewId = loadEndViewId
        if (loadEndViewId != 0) {
            val view = holder.itemView.findViewById<View>(loadEndViewId)
            view.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    private fun setVisible(holder: MultiFunctionAdapter.Companion.MultiFunctionViewHolder, @IdRes viewId: Int, visible: Boolean) {
        val view = holder.itemView.findViewById<View>(viewId)
        view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

}