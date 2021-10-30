package com.example.appmusicmvvm.ViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.appmusicmvvm.Model.*
import com.example.appmusicmvvm.Retrofit.IRetrofit
import com.example.appmusicmvvm.Retrofit.MyRetrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyRepository {
    lateinit var iRetrofit: IRetrofit

    fun getChart(liveData: MutableLiveData<MutableList<Song>>) {
        iRetrofit = MyRetrofit.getRetrofit().create(IRetrofit::class.java)
        iRetrofit.getChart30().enqueue(object : Callback<ResultChart> {
            override fun onResponse(call: Call<ResultChart>, response: Response<ResultChart>) {
                if (response.isSuccessful) {
                    var listTopSong: MutableList<Song> = mutableListOf()
                    var dataRespone = response.body()
                    if (dataRespone?.data?.song != null) {
                        listTopSong = dataRespone.data.song
                        Log.e("myRepository", "listMySong not null")
                        liveData.value = listTopSong
                    } else {
                        Log.e("myRepository", "listMySong null")
                    }
                }
            }

            override fun onFailure(call: Call<ResultChart>, t: Throwable) {
                Log.e("homeFragment", "Load error")
            }
        })
    }

    fun getSearch(key: String, liveData: MutableLiveData<MutableList<SongSearch>>){
        iRetrofit = MyRetrofit.getRetrofitSearch().create(IRetrofit::class.java)
        iRetrofit.getResultSearch("name,artist,song", 500, key).enqueue(object :
            Callback<ResultSearch> {
            override fun onResponse(
                call: Call<ResultSearch>,
                response: Response<ResultSearch>
            ) {
                var dataRespone = response.body()
                var listSearch: MutableList<SongSearch> = mutableListOf()
                if (dataRespone?.data != null && dataRespone.data.size > 0) {
                    var dataSearch = dataRespone.data[0].song
                    listSearch = dataSearch
                    liveData.value = listSearch
                    Log.e("homeFragment", "listSearch not null")
                } else {
                    Log.e("homeFragment", "listSearch null")
                    liveData.value = listSearch
                }
            }

            override fun onFailure(call: Call<ResultSearch>, t: Throwable) {
                Log.e("homeFragment", "Search error")
            }
        })
    }

    fun getRecommend(currentSong: MySong, liveData: MutableLiveData<MutableList<Song>>){
        var type = "audio"
        var id = "${currentSong.id}"
        val iRetrofit = MyRetrofit.getRetrofit().create(IRetrofit::class.java)
        iRetrofit.getSongRecommend(type, id).enqueue(object : Callback<ResultRecommend> {
            override fun onResponse(
                call: Call<ResultRecommend>,
                response: Response<ResultRecommend>
            ) {
                if (response.isSuccessful) {
                    var dataRespone = response.body()
                    if (dataRespone?.data?.items != null) {
                        liveData.value = dataRespone.data.items
                    } else {
                        Log.e("myRes", "listRecommend null")
                    }
                }
            }

            override fun onFailure(call: Call<ResultRecommend>, t: Throwable) {
                Log.e("Myres", "Recommend error")
            }
        })
    }
}