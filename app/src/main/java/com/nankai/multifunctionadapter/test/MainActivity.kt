package com.nankai.multifunctionadapter.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nankai.multifunctionadapter.R
import com.nankai.multifunctionadapter.adapter.IMultiFunctionAdapter
import com.nankai.multifunctionadapter.adapter.LoadMoreView
import com.nankai.multifunctionadapter.model.DummyData
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), IMultiFunctionAdapter.LoadMoreListener {

    override fun onLoadMore() {
        Log.i(MainActivity::class.java.simpleName, "-------------------------> LoadMore")
    }

    companion object {
        val TAG: String? = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = TestAdapter()
        listData.adapter = adapter

        adapter.setHeaderView(LayoutInflater.from(baseContext).inflate(R.layout.item_header, null))
        adapter.setFooterView(LayoutInflater.from(baseContext).inflate(R.layout.item_footer, null))
        adapter.setOnLoadMoreListener(this)
        adapter.isReloadMore = true
        adapter.isAutoLoadMore = true

        val data = DummyData.getDummyData()
        Log.i(TAG, "Dummy data : ${data.size}")
        adapter.setNewData(data)

        refresh.setOnRefreshListener {
            adapter.setNewData(DummyData.getDummyData())
            refresh.isRefreshing = false
        }

        fabButtonLoad.setOnClickListener { _->
            adapter.isLoading = false
        }

        fabButtonFail.setOnClickListener { _->
            adapter.status = LoadMoreView.STATUS_FAIL
        }

        fabButtonEmpty.setOnClickListener { _->
            adapter.status = LoadMoreView.STATUS_EMPTY
        }

        fabButtonEnd.setOnClickListener { _->
            adapter.status = LoadMoreView.STATUS_END
        }

        fabButtonAddNewData.setOnClickListener { _->
            adapter.setNewData(DummyData.getDummyData())
        }
    }
}
