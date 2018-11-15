package com.nankai.multifunctionadapter.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nankai.multifunctionadapter.R
import com.nankai.multifunctionadapter.model.DummyData
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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

        val data = DummyData.getDummyData()
        Log.i(TAG, "Dummy data : ${data.size}")
        adapter.submitList(data)

        refresh.setOnRefreshListener {
            adapter.submitList(DummyData.getDummyData())
            refresh.isRefreshing = false
        }
    }
}
