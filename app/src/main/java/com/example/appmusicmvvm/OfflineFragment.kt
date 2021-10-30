package com.example.appmusicmvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusicmvvm.Adapter.MySongAdapter
import com.example.appmusicmvvm.Model.MySong
import kotlinx.android.synthetic.main.fragment_offline.*

class OfflineFragment: Fragment() {
    var mMainViewModel: MainViewModel?=null
    var songs: MutableList<MySong> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_offline,container,false)
        mMainViewModel = activity?.let { ViewModelProvider(it).get(MainViewModel::class.java) }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        var adapter = MySongAdapter(context,songs)
        offline_rvSongs.layoutManager = layoutManager
        offline_rvSongs.adapter = adapter
        adapter.setCallBack {
            mMainViewModel!!.startASong(activity,songs[it])
        }
        mMainViewModel!!.vmListOffline.observe(viewLifecycleOwner, Observer {
            songs = it
            adapter.listSong=songs
            adapter.notifyDataSetChanged()
        })
    }
}