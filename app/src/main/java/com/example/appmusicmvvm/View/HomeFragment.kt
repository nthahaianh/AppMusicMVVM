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
import com.example.appmusicmvvm.Adapter.SongAdapter
import com.example.appmusicmvvm.Retrofit.IRetrofit
import com.example.appmusicmvvm.Model.Song
import com.example.appmusicmvvm.R
import com.example.appmusicmvvm.Retrofit.MyRetrofit
import com.example.appmusicmvvm.ViewModel.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    var mMainViewModel: MainViewModel?=null
    lateinit var iRetrofit: IRetrofit
    var listTopSong: MutableList<Song> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        iRetrofit = MyRetrofit.getRetrofit().create(IRetrofit::class.java)
        mMainViewModel = activity?.let { ViewModelProvider(it).get(MainViewModel::class.java) }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { mMainViewModel!!.getCharts(it) }
        mMainViewModel!!.hasInternet.observe(viewLifecycleOwner, Observer {
            if (it){
                home_btnReload.visibility = View.GONE
            }else{
                if (mMainViewModel!!.vmListTop.value?.size!! <= 0)
                    home_btnReload.visibility = View.VISIBLE
            }
        })
        home_btnReload.setOnClickListener {
            home_progressBar.visibility = View.VISIBLE
            activity?.let { it1 -> mMainViewModel!!.getCharts(it1) }
        }
        setUpChart()
    }

    private fun setUpChart() {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        var adapter = SongAdapter(listTopSong)
        adapter.setCallBack {
            mMainViewModel!!.startSong(activity,listTopSong[it])
        }
        home_rvCharts?.layoutManager = layoutManager
        home_rvCharts?.adapter = adapter
        mMainViewModel!!.vmListTop.observe(viewLifecycleOwner, Observer {
            if(it.size>0){
                home_progressBar.visibility = View.GONE
            }else{
                home_progressBar.visibility = View.VISIBLE
            }
            listTopSong = it
            adapter.listSong = listTopSong
            adapter.notifyDataSetChanged()
        })
    }
}