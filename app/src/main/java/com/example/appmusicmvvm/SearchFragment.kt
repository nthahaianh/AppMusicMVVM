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
import com.example.appmusicmvvm.Adapter.SearchListAdapter
import com.example.appmusicmvvm.Model.MySong
import com.example.appmusicmvvm.Retrofit.IRetrofit
import com.example.appmusicmvvm.Model.SongSearch
import com.example.appmusicmvvm.Retrofit.MyRetrofit
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {
    var mMainViewModel: MainViewModel?=null
    lateinit var iSearchRetrofit: IRetrofit
    var listSearch: MutableList<SongSearch> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        iSearchRetrofit = MyRetrofit.getRetrofitSearch().create(IRetrofit::class.java)
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
        var adapter = SearchListAdapter(listSearch)
        adapter.setCallBack {
            val song = listSearch[it]
            val id = song.id
            val title = song.name
            var artist: String = song.artist
            var displayName = "${song.name} - ${song.artist}"
            var data = "http://api.mp3.zing.vn/api/streaming/audio/${song.id}/128"
            var duration: Long = (song.duration * 1000).toLong()
            val urlThumb = "https://photo-zmp3.zadn.vn/"
            var mySong = MySong(id,title,artist,displayName,data,duration,"$urlThumb${song.thumb}",true)
            mMainViewModel!!.startSong(activity,mySong)
        }
        search_rvSearch.layoutManager = layoutManager
        search_rvSearch.adapter = adapter
        mMainViewModel!!.vmListSearch.observe(viewLifecycleOwner, Observer {
            if(it.size>0){
                search_tvNoResult.visibility = View.GONE
            }else{
                search_tvNoResult.visibility = View.VISIBLE
            }
            listSearch = it
            adapter.listSong = it
            adapter.notifyDataSetChanged()
            search_progressBar.visibility = View.GONE
        })
    }
}