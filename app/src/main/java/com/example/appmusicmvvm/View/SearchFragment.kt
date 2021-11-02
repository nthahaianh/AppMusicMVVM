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
import com.example.appmusicmvvm.Adapter.SearchListAdapter
import com.example.appmusicmvvm.Retrofit.IRetrofit
import com.example.appmusicmvvm.Model.SongSearch
import com.example.appmusicmvvm.R
import com.example.appmusicmvvm.Retrofit.MyRetrofit
import com.example.appmusicmvvm.ViewModel.MainViewModel
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {
    var mMainViewModel: MainViewModel?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        mMainViewModel = activity?.let { ViewModelProvider(it).get(MainViewModel::class.java) }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_btnSeach.setOnClickListener {
            search_progressBar.visibility = View.VISIBLE
            getSearchResult()
        }
        setUpResult()
    }

    private fun getSearchResult() {
        var key = search_etSearch.text.toString()
        activity?.let { mMainViewModel!!.getSearch(it,key) }
    }

    private fun setUpResult() {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        var adapter = mMainViewModel!!.vmListSearch.value?.let { SearchListAdapter(it) }
        adapter?.setCallBack {
            mMainViewModel!!.vmListSearch.value?.get(it)?.let { it1 -> mMainViewModel!!.startSearchSong(activity, it1) }
        }
        search_rvSearch.layoutManager = layoutManager
        search_rvSearch.adapter = adapter
        mMainViewModel!!.vmListSearch.observe(viewLifecycleOwner, Observer {
            if(it.size>0){
                search_tvNoResult.visibility = View.GONE
            }else{
                search_tvNoResult.visibility = View.VISIBLE
            }
            adapter?.listSong = it
            adapter?.notifyDataSetChanged()
            search_progressBar.visibility = View.GONE
        })
        mMainViewModel!!.isLoad.observe(viewLifecycleOwner, Observer {
            if(!it){
                search_progressBar.visibility = View.GONE
            }
        })
    }
}