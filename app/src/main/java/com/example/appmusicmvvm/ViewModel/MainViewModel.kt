package com.example.appmusicmvvm.ViewModel

import android.app.Activity
import android.content.*
import android.net.ConnectivityManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.appmusicmvvm.Model.MySong
import com.example.appmusicmvvm.Service.SongService
import com.example.appmusicmvvm.Service.SongService.Companion.currentSong
import com.example.appmusicmvvm.Service.SongService.Companion.isDisplay
import com.example.appmusicmvvm.Service.SongService.Companion.isPlaying
import com.example.appmusicmvvm.Service.SongService.Companion.mediaPlayer
import com.example.appmusicmvvm.Model.Song
import com.example.appmusicmvvm.Model.SongSearch
import com.example.appmusicmvvm.SQLite.SQLHelper
import com.example.appmusicmvvm.View.SongActivity

class MainViewModel : ViewModel() {
    lateinit var sqlHelper: SQLHelper
    private val myRepository = MyRepository()
    var nowSong: MutableLiveData<MySong> = MutableLiveData()
    var currentPos: MutableLiveData<Int> = MutableLiveData()
    var hasInternet: MutableLiveData<Boolean> = MutableLiveData()
    var displayBottomBar: MutableLiveData<Boolean> = MutableLiveData()
    var isSongPlay: MutableLiveData<Boolean> = MutableLiveData()
    var isFavourite: MutableLiveData<Boolean> = MutableLiveData()
    var isLoad: MutableLiveData<Boolean> = MutableLiveData()
    var songs: MutableList<MySong> = mutableListOf()
    var strTitleSong = MutableLiveData<String>()
    var vmListOffline: MutableLiveData<MutableList<MySong>> = MutableLiveData()
    var vmFavouriteList: MutableLiveData<MutableList<MySong>> = MutableLiveData()
    var vmListTop: MutableLiveData<MutableList<Song>> = MutableLiveData()
    var vmListSearch: MutableLiveData<MutableList<SongSearch>> = MutableLiveData()
    var vmListRecommend: MutableLiveData<MutableList<Song>> = MutableLiveData()

    init {
        strTitleSong.value = ""
        vmListTop.value = mutableListOf()
        vmListSearch.value = mutableListOf()
        vmListRecommend.value = mutableListOf()
        vmFavouriteList.value = mutableListOf()
        vmListOffline.value = songs
        hasInternet.value = false
        displayBottomBar.value = false
        isFavourite.value = false
        isLoad.value = false
        isSongPlay.value = isPlaying
        if (isDisplay) {
            currentPos.value = mediaPlayer.currentPosition
            nowSong.value = currentSong
            strTitleSong.value = currentSong.title
            vmListRecommend.value = SongService.listRecommend
            displayBottomBar.value = true
        }
    }

    private val myBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle = intent!!.extras ?: return
            var action = bundle.getInt("action")
            updateUI(action)
        }
    }

    private fun updateUI(action: Int) {
        when (action) {
            SongService.ON_START -> {
                isLoad.value = false
                isSongPlay.value = isPlaying
                strTitleSong.value = currentSong.title
                nowSong.value = currentSong
                vmListRecommend.value = SongService.listRecommend
            }
            SongService.ON_RECOMMEND -> {
                isLoad.value = false
                vmListRecommend.value = SongService.listRecommend
            }
            SongService.ON_PAUSE -> {
                isSongPlay.value = isPlaying
            }
            SongService.ON_DONE -> {
                isSongPlay.value = isPlaying
            }
            SongService.ON_PREVIOUS -> {
                isLoad.value = false
                isSongPlay.value = isPlaying
            }
            SongService.ON_STOP -> {
                isSongPlay.value = isPlaying
            }
            SongService.ON_RESUME -> {
                isSongPlay.value = isPlaying
            }
        }
    }

    fun registerReceiver(activity: Activity) {
        LocalBroadcastManager.getInstance(activity).registerReceiver(myBroadcast, IntentFilter("ac_service_to_main"))
    }

    fun getCharts(activity: Activity) {
        hasInternet.value = checkConnectivity(activity)
        if (hasInternet.value == true) {
            myRepository.getChart(vmListTop)
        }
    }

    fun getSearch(activity: Activity, key: String) {
        hasInternet.value = checkConnectivity(activity)
        if (hasInternet.value == true) {
            myRepository.getSearch(key, vmListSearch)
        }else{
            isLoad.value = false
        }
    }

    fun getFavourite(context: Context) {
        sqlHelper = SQLHelper(context)
        try {
            vmFavouriteList.value = sqlHelper.getAll()
        } catch (e: Exception) {
            e.stackTrace
            Log.e("favourite-SQL", "Read SQL error")
        }
    }

    fun removeFavourite(context: Context) {
        sqlHelper = SQLHelper(context)
        try {
            sqlHelper.removeSong(currentSong.id)
            isFavourite.value = sqlHelper.isExists(currentSong.id)
            getFavourite(context)
        } catch (e: Exception) {
            e.stackTrace
            Log.e("favourite-SQL", "Read SQL error")
        }
    }

    fun addFavourite(context: Context) {
        sqlHelper = SQLHelper(context)
        try {
            sqlHelper.addSong(currentSong)
            isFavourite.value = sqlHelper.isExists(currentSong.id)
            getFavourite(context)
        } catch (e: Exception) {
            e.stackTrace
            Log.e("favourite-SQL", "Read SQL error")
        }
    }

    fun checkFavourite(context: Context) {
        sqlHelper = SQLHelper(context)
        try {
            isFavourite.value = sqlHelper.isExists(currentSong.id)
        } catch (e: Exception) {
            e.stackTrace
            Log.e("favourite-SQL", "Read SQL error")
        }
    }

    fun currentPos(): MutableLiveData<Int> {
        currentPos.value = mediaPlayer.currentPosition
        return currentPos
    }

    fun rewind(position: Int) {
        mediaPlayer.seekTo(position)
    }

    fun downloadSong(activity: Activity?) {
        if (checkConnectivity(activity)) {
            actionToService(activity, SongService.ON_DOWNLOAD)
        }
    }

    fun nextSong(activity: Activity?) {
        isLoad.value = true
        if (currentSong.isOnline) {
            if (checkConnectivity(activity)) {
                actionToService(activity, SongService.ON_NEXT)
            }else{
                isLoad.value = false
            }
        } else {
            actionToService(activity, SongService.ON_NEXT)
        }
    }

    fun previousSong(activity: Activity) {
        isLoad.value = true
        if (currentSong.isOnline) {
            if (checkConnectivity(activity)) {
                actionToService(activity, SongService.ON_PREVIOUS)
            }else{
                isLoad.value = false
            }
        } else {
            actionToService(activity, SongService.ON_PREVIOUS)
        }
    }

    fun resumeSong(fragmentActivity: FragmentActivity?) {
        actionToService(fragmentActivity, SongService.ON_RESUME)
    }

    fun pauseSong(fragmentActivity: FragmentActivity?) {
        actionToService(fragmentActivity, SongService.ON_PAUSE)
    }

    fun startSearchSong(activity: Activity?, song: SongSearch) {
        val id = song.id
        val title = song.name
        var artist: String = song.artist
        var displayName = "${song.name} - ${song.artist}"
        var data = "http://api.mp3.zing.vn/api/streaming/audio/${song.id}/128"
        var duration: Long = (song.duration * 1000).toLong()
        val urlThumb = "https://photo-zmp3.zadn.vn/"
        var mySong = MySong(id, title, artist, displayName, data, duration, "$urlThumb${song.thumb}", true)
        startMySong(activity, mySong)
    }

    fun startSong(activity: Activity?, song: Song) {
        val id = song.id
        val title = song.title
        var artist: String = song.artists_names
        var displayName = "${song.title} - ${song.artists_names}"
        var data = "http://api.mp3.zing.vn/api/streaming/audio/${song.id}/128"
        var duration: Long = (song.duration * 1000).toLong()
        var mySong = MySong(id, title, artist, displayName, data, duration, song.thumbnail, true)
        startMySong(activity, mySong)
    }

    fun startMySong(activity: Activity?, song: MySong) {
        if (song.isOnline) {
            if (checkConnectivity(activity)) {startASong(activity, song)}
        } else {
            startASong(activity, song)
        }
    }

    fun startASong(activity: Activity?, song: MySong) {
        strTitleSong.value = song.title
        currentSong = song
        actionToService(activity, SongService.ON_START)
        startSongActivity(activity)
        isLoad.value = true
        displayBottomBar.value = true
        isSongPlay.value = true
    }

    private fun startSongActivity(activity: Activity?) {
        val intentSong = Intent(activity, SongActivity::class.java)
        activity?.startActivity(intentSong)
    }

    private fun actionToService(activity: Activity?, action: Int) {
        val intent = Intent(activity, SongService::class.java)
        intent.putExtra("action", action)
        activity?.startService(intent)
    }

    private fun checkConnectivity(activity: Activity?): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return if (info == null || !info.isConnected || !info.isAvailable) {
            Toast.makeText(activity.baseContext, "No internet connection", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
        return false
    }

    fun loadSongs(contentResolver: ContentResolver?): MutableList<MySong> {
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        )
        val cursor = contentResolver?.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        )
        while (cursor!!.moveToNext()) {
            if (cursor.getLong(5) > 0) {
                songs.add(
                    MySong(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getString(4),
                        false
                    )
                )
            }
        }
        vmListOffline.value = songs
        return songs
    }
}