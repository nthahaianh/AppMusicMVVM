package com.example.appmusicmvvm.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.appmusicmvvm.Service.SongService

class SongReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent!!.getIntExtra("action",0)
        val intent1 = Intent(context, SongService::class.java)
        intent1.putExtra("action",action)
        Log.e("Receiver","$action")
        context!!.startService(intent1)
    }
}