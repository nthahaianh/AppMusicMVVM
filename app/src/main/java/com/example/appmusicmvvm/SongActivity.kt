package com.example.appmusicmvvm

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusicmvvm.Adapter.SongAdapter
import com.example.appmusicmvvm.MainActivity.Companion.mMainViewModel
import com.example.appmusicmvvm.Model.MySong
import com.example.appmusicmvvm.Model.Song
import com.example.appmusicmvvm.Retrofit.IRetrofit
import com.example.appmusicmvvm.Retrofit.MyRetrofit
import com.example.appmusicmvvm.SQLite.SQLHelper
import com.example.appmusicmvvm.Service.SongService.Companion.currentSong
import kotlinx.android.synthetic.main.activity_song.*

class SongActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var iRetrofit: IRetrofit
    lateinit var sqlHelper: SQLHelper
    var listRecommend: MutableList<Song> = mutableListOf()
    var typeRepeat: Int = 0
    var isShuffle = false
    var isFavourite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)

        sqlHelper = SQLHelper(baseContext)
        iRetrofit = MyRetrofit.getRetrofit().create(IRetrofit::class.java)
        sharedPreferences = getSharedPreferences("SharePreferences", Context.MODE_PRIVATE)
        typeRepeat = sharedPreferences.getInt("typeRepeat", 0)
        isShuffle = sharedPreferences.getBoolean("isShuffle", false)

        updateIconRepeat()
        updateIconShuffle()
        updateIconFavourite()
        setUpBtnPlay()
        setUpRecommend()
        setUpSeekBar()

        mMainViewModel!!.nowSong.observe(this, Observer { song ->
            song_tvTitle.text = song.title
            song_tvSinger.text = song.artist
            try {
                if (song.isOnline) {
                    Glide.with(baseContext).load(Uri.parse(song.img)).into(song_ivImage)
                } else {
                    var mmr = MediaMetadataRetriever()
                    mmr.setDataSource(baseContext, Uri.parse(song.img))
                    val byteImage = mmr.embeddedPicture
                    var bitmap = BitmapFactory.decodeByteArray(byteImage, 0, byteImage!!.size)
                    Glide.with(baseContext).load(bitmap).into(song_ivImage)
                }
            } catch (e: Exception) {
                e.stackTrace
                Glide.with(baseContext).load(R.drawable.music).into(song_ivImage)
            }

            song_ivDownload.setOnClickListener {
                if (song.isOnline) {
                    mMainViewModel!!.downloadSong(this)
                } else {
                    Toast.makeText(baseContext, "This song is available", Toast.LENGTH_SHORT).show()
                }
            }
            val totalDuration = song.duration
            song_tvMaxTime.text = millionSecondsToTime(totalDuration)
            song_seekBar.max = totalDuration.toInt()
        })

        try {
            isFavourite = sqlHelper.isExists(currentSong.id)
            updateIconFavourite()
        } catch (e: Exception) {
            e.stackTrace
        }
        song_ivBack.setOnClickListener {
            finish()
        }
        song_ivFavourite.setOnClickListener {
            try {
                if (isFavourite) {
                    sqlHelper.removeSong(currentSong.id)
                    Log.e("song-ivFa", "remove sql")
                } else {
                    sqlHelper.addSong(currentSong)
                    Log.e("song-ivFa", "add sql ")
                }

            } catch (e: Exception) {
                e.stackTrace
            }
            isFavourite = !isFavourite
            updateIconFavourite()
        }
        song_ivShuffle.setOnClickListener {
            isShuffle = !isShuffle
            updateIconShuffle()
        }
        song_ivTypeRepeat.setOnClickListener {
            when (typeRepeat) {
                2 -> typeRepeat = 0
                else -> typeRepeat++
            }
            updateIconRepeat()
        }
        song_btnPlay.setOnClickListener {
            if (mMainViewModel!!.isSongPlay.value == true) {
                mMainViewModel!!.pauseSong(this)
            } else {
                mMainViewModel!!.resumeSong(this)
            }
        }
        song_btnNext_song.setOnClickListener {
            mMainViewModel!!.nextSong(this)
        }
        song_btnPrevious_song.setOnClickListener {
            mMainViewModel!!.previousSong(this)
        }
    }

    private fun setUpSeekBar() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                mMainViewModel!!.currentPos().observe(this@SongActivity, Observer {
                    song_tvCurrentTime?.text = intToTime(it)
                    song_seekBar?.progress = it
                })
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 1000)
        song_seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mMainViewModel!!.rewind(seekBar.progress)
            }
        })
    }

    private fun setUpRecommend() {
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)
        var adapter = SongAdapter(listRecommend)
        adapter.setCallBack {
            song_progressBar.visibility = View.VISIBLE
            val song = listRecommend[it]
            val id = song.id
            val title = song.title
            var artist: String = song.artists_names
            var displayName = "${song.title} - ${artist}"
            var data = "http://api.mp3.zing.vn/api/streaming/audio/${song.id}/128"
            var duration: Long = (song.duration * 1000).toLong()
            var mySong = MySong(id, title, artist, displayName, data, duration, song.thumbnail, true)
            currentSong = mySong
            mMainViewModel!!.startSong(this, mySong)
        }
        song_rvRecommend.layoutManager = layoutManager
        song_rvRecommend.adapter = adapter
        mMainViewModel!!.vmListRecommend.observe(this, Observer {
            listRecommend = it
            adapter.listSong = it
            adapter.notifyDataSetChanged()
            song_progressBar.visibility = View.GONE
        })
    }

    private fun setUpBtnPlay() {
        mMainViewModel!!.isSongPlay.observe(this, Observer {
            if (it) {
                song_btnPlay?.setImageResource(R.drawable.ic_pause)
            } else {
                song_btnPlay?.setImageResource(R.drawable.ic_play_arrow)
            }
        })
    }

    private fun updateIconRepeat() {
        when (typeRepeat) {
            0 -> {
                song_ivTypeRepeat.setImageResource(R.drawable.ic_no_repeat)
            }
            1 -> {
                song_ivTypeRepeat.setImageResource(R.drawable.ic_repeat_one)
            }
            2 -> {
                song_ivTypeRepeat.setImageResource(R.drawable.ic_repeat)
            }
        }
        val editor = sharedPreferences.edit()
        editor.putInt("typeRepeat", typeRepeat)
        editor.apply()
    }

    private fun updateIconShuffle() {
        if (isShuffle) {
            song_ivShuffle.setImageResource(R.drawable.ic__shuffle_choose)
        } else {
            song_ivShuffle.setImageResource(R.drawable.ic_shuffle)
        }
        val editor = sharedPreferences.edit()
        editor.putBoolean("isShuffle", isShuffle)
        editor.apply()
    }

    private fun updateIconFavourite() {
        if (isFavourite) {
            song_ivFavourite.setImageResource(R.drawable.ic_baseline_favorite_24)
        } else {
            song_ivFavourite.setImageResource(R.drawable.ic_not_favorite)
        }
    }

    fun millionSecondsToTime(milliSeconds: Long): String {
        val hours = milliSeconds / (1000 * 60 * 60)
        val minutes = (milliSeconds % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun intToTime(milliSeconds: Int): String {
        val hours = milliSeconds / (1000 * 60 * 60)
        val minutes = (milliSeconds % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}