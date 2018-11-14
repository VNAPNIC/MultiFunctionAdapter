package com.nankai.multifunctionadapter.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.nankai.multifunctionadapter.R
import com.nankai.multifunctionadapter.model.DummyData
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = TestAdapter()
//        listData.adapter = adapter
//        adapter.setHeaderView(LayoutInflater.from(baseContext).inflate(R.layout.item_header, null))
//        adapter.submitList(DummyData.getDummyData())
    }
}
