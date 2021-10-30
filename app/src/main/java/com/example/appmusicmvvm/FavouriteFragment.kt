package com.example.appmusicmvvm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusicmvvm.Adapter.FavouriteSongAdapter
import com.example.appmusicmvvm.Model.MySong
import com.example.appmusicmvvm.SQLite.SQLHelper
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
        try {
            favouriteList = sqlHelper.getAll()
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            var adapter = FavouriteSongAdapter(context,favouriteList)
            adapter.setCallBack {
//                if (favouriteList[it].isOnline){
//                    if (checkConnectivity()){ mMainViewModel!!.startSong(activity,favouriteList[it]) }
//                }else{
                    mMainViewModel!!.startSong(activity,favouriteList[it])
//                }
            }
            favourite_rvFavourite.layoutManager = layoutManager
            favourite_rvFavourite.adapter = adapter
        }catch (e:Exception){
            e.stackTrace
            Log.e("favourite-SQL","Read SQL error")
        }
    }

//    private fun checkConnectivity(): Boolean {
//        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val info = connectivityManager.activeNetworkInfo
//        return if (info == null || !info.isConnected || !info.isAvailable) {
//            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
//            false
//        } else {
//            true
//        }
//        return false
//    }
}