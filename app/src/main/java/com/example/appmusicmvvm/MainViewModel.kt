package com.example.appmusicmvvm

import android.app.Activity
import android.content.*
import android.net.ConnectivityManager
import android.provider.MediaStore
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

class MainViewModel : ViewModel() {
    val myRepository = MyRepository()
    var nowSong:MutableLiveData<MySong> = MutableLiveData()
    var currentPos:MutableLiveData<Int> = MutableLiveData()
    var hasInternet:MutableLiveData<Boolean> = MutableLiveData()
    var displayBottomBar:MutableLiveData<Boolean> = MutableLiveData()
    var isSongPlay:MutableLiveData<Boolean> = MutableLiveData()
    var songs: MutableList<MySong> = mutableListOf()
    var strTitleSong = MutableLiveData<String>()
    var vmListOffline: MutableLiveData<MutableList<MySong>> = MutableLiveData()
    var vmListTop: MutableLiveData<MutableList<Song>> = MutableLiveData()
    var vmListSearch: MutableLiveData<MutableList<SongSearch>> = MutableLiveData()
    var vmListRecommend: MutableLiveData<MutableList<Song>> = MutableLiveData()

    init {
        strTitleSong.value = ""
        vmListTop.value = mutableListOf()
        vmListSearch.value = mutableListOf()
        vmListRecommend.value = mutableListOf()
        vmListOffline.value = songs
        hasInternet.value = false
        displayBottomBar.value = false
        isSongPlay.value = isPlaying
        if(isDisplay){
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
            var action=bundle.getInt("action")
            updateUI(action)
        }
    }
    private fun updateUI(action: Int) {
        when (action) {
            SongService.ON_START -> {
                isSongPlay.value = isPlaying
                strTitleSong.value = currentSong.title
                nowSong.value = currentSong
                vmListRecommend.value = SongService.listRecommend
            }
            SongService.ON_RECOMMEND-> {
                vmListRecommend.value = SongService.listRecommend
            }
            SongService.ON_PAUSE -> {
                isSongPlay.value = isPlaying
            }
            SongService.ON_DONE-> {
                isSongPlay.value = isPlaying
            }
            SongService.ON_STOP -> {
                isSongPlay.value = isPlaying
            }
            SongService.ON_RESUME -> {
                isSongPlay.value = isPlaying
            }
            SongService.ON_DOWNLOAD -> {
                vmListOffline.value
            }
        }
    }
    fun registerReceiver(activity: Activity){
        LocalBroadcastManager.getInstance(activity).registerReceiver(myBroadcast, IntentFilter("ac_service_to_main"))
    }
    fun getCharts(activity: Activity) {
        hasInternet.value=checkConnectivity(activity)
        if (hasInternet.value == true){
            myRepository.getChart(vmListTop)
        }
    }
    fun getSearch(activity: Activity,key:String){
        hasInternet.value=checkConnectivity(activity)
        if (hasInternet.value == true){
            myRepository.getSearch(key,vmListSearch)
        }
    }

    fun currentPos():MutableLiveData<Int>{
        currentPos.value = mediaPlayer.currentPosition
        return currentPos
    }

    fun rewind(position:Int){
        mediaPlayer.seekTo(position)
    }

    fun downloadSong(activity: Activity?){
        if (checkConnectivity(activity)){
            actionToService(activity, SongService.ON_DOWNLOAD)
        }
    }
    fun nextSong(activity: Activity?){
        if (currentSong.isOnline){
            if (checkConnectivity(activity)){
                actionToService(activity, SongService.ON_NEXT)
            }
        }else{
            actionToService(activity, SongService.ON_NEXT)
        }
    }

    fun previousSong(activity: Activity){
        if (currentSong.isOnline){
            if (checkConnectivity(activity)){
                actionToService(activity, SongService.ON_PREVIOUS)
            }
        }else {
            actionToService(activity, SongService.ON_PREVIOUS)
        }
    }

    fun resumeSong(fragmentActivity: FragmentActivity?){
        actionToService(fragmentActivity, SongService.ON_RESUME)
    }

    fun pauseSong(fragmentActivity: FragmentActivity?){
        actionToService(fragmentActivity, SongService.ON_PAUSE)
    }

    fun startSong(activity: Activity?, song: MySong){
        if (song.isOnline){
            if (checkConnectivity(activity)){
                startASong(activity, song)
            }
        }else {
            startASong(activity, song)
        }
    }

    fun startASong(activity: Activity?, song: MySong) {
        strTitleSong.value = song.title
        currentSong = song
        actionToService(activity, SongService.ON_START)
        startSongActivity(activity)
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

    fun checkConnectivity(activity: Activity?): Boolean {
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
        vmListOffline.value=songs
        return songs
    }
}