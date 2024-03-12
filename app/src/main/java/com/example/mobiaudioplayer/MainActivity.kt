package com.example.mobiaudioplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, MobiAudioPlayer::class.java)
        intent.putExtra("playListName","Downloads")
        intent.putStringArrayListExtra("data",getFilePath())
        startActivity(intent)
    }
    private fun getFilePath(): ArrayList<String> {
        val data: ArrayList<String> = ArrayList()
        val table = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.AudioColumns.DATA)
        val fileCursor = contentResolver.query(
            table,
            projection,
            null,
            null,
            null
        )
        val path = fileCursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        while (fileCursor.moveToNext()) {
            val path = fileCursor.getString(path)
            val filePAth = File(path ?: "")
            if (filePAth.exists()) {
                data.add(path)

            }
        }
        Log.d("bvdfbghsk", data.size.toString())
        return data
    }
}