package com.example.appmusicmvvm.View

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusicmvvm.Adapter.FavouriteSongAdapter
import com.example.appmusicmvvm.Model.MySong
import com.example.appmusicmvvm.R
import com.example.appmusicmvvm.SQLite.SQLHelper
import com.example.appmusicmvvm.ViewModel.MainViewModel
import kotlinx.android.synthetic.main.fragment_favourite.*

class FavouriteFragment: Fragment() {
    var mMainViewModel: MainViewModel?=null
    lateinit var sqlHelper: SQLHelper
    var favouriteList: MutableList<MySong> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourite,container,false)
        sqlHelper = SQLHelper(context)
        mMainViewModel = activity?.let { ViewModelProvider(it).get(MainViewModel::class.java) }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        var adapter = FavouriteSongAdapter(context,favouriteList)
        adapter.setCallBack {
            mMainViewModel!!.startMySong(activity,favouriteList[it])
        }
        favourite_rvFavourite.layoutManager = layoutManager
        favourite_rvFavourite.adapter = adapter
        mMainViewModel!!.vmFavouriteList.observe(viewLifecycleOwner, Observer {
            favouriteList = it
            adapter.listSong = it
            adapter.notifyDataSetChanged()
        })
    }
}