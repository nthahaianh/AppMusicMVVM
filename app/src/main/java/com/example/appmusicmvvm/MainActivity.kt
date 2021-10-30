package com.example.appmusicmvvm

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.appmusicmvvm.View.SongActivity
import com.example.appmusicmvvm.Service.SongService
import com.example.appmusicmvvm.ViewModel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        var mMainViewModel: MainViewModel?=null
    }
    private val REQUEST_CODE_PERMISSIONS = 1
    lateinit var navHostFragment: NavHostFragment
    lateinit var controller: NavController
    private lateinit var songService: SongService
    var isSongServiceConnected = false
    private val connectSongService = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SongService.MyBinder
            songService = binder.getSongService()
            isSongServiceConnected = true
            SongService.isDestroy = false
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isSongServiceConnected = false
            SongService.isDestroy = true
        }
    }

    override fun onStart() {
        super.onStart()
        SongService.isDestroy = false
        Intent(this, SongService::class.java).also { intent ->
            bindService(intent, connectSongService, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connectSongService)
        isSongServiceConnected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        SongService.isDestroy = true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        controller = navHostFragment.navController
        main_bottomNavigationView.setupWithNavController(controller)

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        mMainViewModel!!.registerReceiver(this)
        mMainViewModel!!.loadSongs(contentResolver)
//        mMainViewModel!!.getCharts(this)
        mMainViewModel!!.getFavourite(baseContext)
        mMainViewModel!!.strTitleSong.observe(this, Observer{
            main_tvTitle.text = it.toString()
        })
        mMainViewModel!!.displayBottomBar.observe(this, Observer {
            if (it){
                main_clDisplay.visibility = View.VISIBLE
            } else {
                main_clDisplay.visibility = View.GONE
            }
        })
        mMainViewModel!!.isSongPlay.observe(this, Observer {
            if (it) {
                main_btnPlay.setImageResource(R.drawable.ic_pause)
            } else {
                main_btnPlay.setImageResource(R.drawable.ic_play_arrow)
            }
        })
        main_tvTitle.setOnClickListener {
            val intent = Intent(this, SongActivity::class.java)
            startActivity(intent)
        }
        main_btnPlay.setOnClickListener {
            if(mMainViewModel!!.isSongPlay.value==true){
                mMainViewModel!!.pauseSong(this)
            }else{
                mMainViewModel!!.resumeSong(this)
            }
        }
        main_btnNext_song.setOnClickListener {
            mMainViewModel!!.nextSong(this)
        }
        main_btnPrevious_song.setOnClickListener {
            mMainViewModel!!.previousSong(this)
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Please allow storage permission", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        mMainViewModel!!.loadSongs(contentResolver)
                    }
                }
            }
        }
    }
}