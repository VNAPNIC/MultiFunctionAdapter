package com.nankai.multifunctionadapter.test

import com.nankai.multifunctionadapter.R
import com.nankai.multifunctionadapter.adapter.LoadMoreView

class LoadMoreTest : LoadMoreView() {

    override val layoutId: Int
        get() = R.layout.view_load_more

    override val loadingViewId: Int
        get() = R.id.load_more_loading_view

    override val loadFailViewId: Int
        get() = R.id.load_more_load_fail_view

    override val loadEndViewId: Int
        get() = R.id.load_more_load_end_view

    override val loadEmptyViewId: Int
        get() = R.id.load_more_load_empty
}