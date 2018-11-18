package com.nankai.multifunctionadapter.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nankai.multifunctionadapter.R
import com.nankai.multifunctionadapter.adapter.IMultiFunctionAdapter
import com.nankai.multifunctionadapter.adapter.LoadMoreView
import com.nankai.multifunctionadapter.model.DummyData
import com.nankai.multifunctionadapter.repository.Data
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), IMultiFunctionAdapter.LoadMoreListener {
    var adapter: TestAdapter? = null
    override fun onLoadMore() {
        Log.i(MainActivity::class.java.simpleName, "-------------------------> LoadMore")
//        val handler = Handler()
//        handler.postDelayed({
//            if (!isFinishing) {
//                adapter?.add(DummyData.getNewDummyData(adapter?.getAll() as MutableList<Data>))
//                adapter?.isLoading = false
//            }
//        }, 5000)
    }

    companion object {
        val TAG: String? = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = TestAdapter()
        listData.adapter = adapter

//        adapter?.setHeaderView(LayoutInflater.from(baseContext).inflate(R.layout.item_header, null))
        adapter?.setFooterView(LayoutInflater.from(baseContext).inflate(R.layout.item_footer, null))

//        adapter?.setEmptyView(R.layout.layout_empty)

        adapter?.setOnLoadMoreListener(this)
//        adapter?.isReloadMore = true
//        adapter?.isAutoLoadMore = true

        val data = DummyData.getDummyData()
        Log.i(TAG, "Dummy data : ${data.size}")
        adapter?.add(data)

        refresh.setOnRefreshListener {
            adapter?.clear()
            adapter?.add(DummyData.getDummyData())
            refresh.isRefreshing = false
        }

        fabButtonLoad.setOnClickListener {
            adapter?.isLoading = false
        }

        fabButtonFail.setOnClickListener {
            adapter?.loadMoreStatus = LoadMoreView.STATUS_FAIL
        }

        fabButtonEmpty.setOnClickListener {
            adapter?.loadMoreStatus = LoadMoreView.STATUS_EMPTY
        }

        fabButtonEnd.setOnClickListener {
            adapter?.loadMoreStatus = LoadMoreView.STATUS_END
        }

        fabButtonAddNewData.setOnClickListener {
            adapter?.add(DummyData.getOneDummyData(adapter?.getAll() as MutableList<Data>)[0], 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
