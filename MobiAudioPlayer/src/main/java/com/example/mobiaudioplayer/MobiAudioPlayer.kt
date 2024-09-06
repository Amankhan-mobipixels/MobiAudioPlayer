package com.example.mobiaudioplayer

import android.app.Dialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audioplayer.AudioAdapter
import com.example.audioplayer.getLoop
import com.example.audioplayer.getPlaySpeed
import com.example.audioplayer.setLoop
import com.example.audioplayer.setPlaySpeed
import com.example.mobiaudioplayer.databinding.ActivityMobiAudioPlayerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class MobiAudioPlayer: AppCompatActivity(), AudioAdapter.ClickListener {
    private var dialogThumbnail: ImageView? = null
    private var dialogAudioTitle: TextView? = null
    private var dialogBtnPlayPause: ImageButton? = null
    private var dialogCurrentDuration: TextView? = null
    private var dialogTotalDuration: TextView? = null
    private var dialogProgressSeekBar: SeekBar? = null
    val ACTION_PLAY = "com.example.audioplayer.ACTION_PLAY"
    val ACTION_PAUSE = "com.example.audioplayer.ACTION_PAUSE"
    val ACTION_NEXT = "com.example.audioplayer.ACTION_NEXT"
    val ACTION_PREVIOUS = "com.example.audioplayer.ACTION_PREVIOUS"
    val CHANNEL_ID = "AudioServiceChannel"
    val NOTIFICATION_ID = 100

    private lateinit var binding: ActivityMobiAudioPlayerBinding
    private var currentDurationJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingIndex = 0
    private var allAudioFiles = ArrayList<String>()
    private var notificationManager: NotificationManager? = null
    private var adapter: AudioAdapter? = null
    private var isPlaying = false
    private val speeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityMobiAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener{
            finish()
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_mobi_audio_back)

        binding.playListName.text = intent.getStringExtra("playListName")
        currentPlayingIndex = intent.getIntExtra("position",0)
        allAudioFiles = intent.getStringArrayListExtra("data") as ArrayList<String>

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        adapter = AudioAdapter(this, allAudioFiles,this)
        binding.recyclerview.adapter = adapter

        if (allAudioFiles.size!=0)  {
            registerBroadcastReceiver()
            playAudio(allAudioFiles[currentPlayingIndex])
        }

        binding.btnPlayPause.setOnClickListener {
            playPause()

        }
        binding.progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer?.seekTo(progress) }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.parent.setOnClickListener{
            playerDialog()
        }
    }
    private fun playerDialog(){
        val dialog = Dialog(this, com.google.android.material.R.style.AlertDialog_AppCompat)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.player_dialog)
        val window: Window? = dialog.window
        val wlp = window?.attributes
        wlp?.gravity = Gravity.CENTER
        wlp?.flags = wlp?.flags?.and(WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv())
        window?.attributes = wlp
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        val close = dialog.findViewById<ImageView>(R.id.close)
        dialogAudioTitle = dialog.findViewById<TextView>(R.id.audioTitle)
        val btnNext = dialog.findViewById<ImageButton>(R.id.btnNext)
        val btnPrev = dialog.findViewById<ImageButton>(R.id.btnPrev)
        val btnRepeat = dialog.findViewById<ImageButton>(R.id.btnRepeat)
//        val btnSpeed = dialog.findViewById<ImageButton>(R.id.btnSpeed)
        dialogThumbnail = dialog.findViewById(R.id.thumnail)
        dialogBtnPlayPause = dialog.findViewById(R.id.btnPlayPause)
        dialogProgressSeekBar = dialog.findViewById(R.id.progressSeekBar)
        val speedText = dialog.findViewById<TextView>(R.id.speedText)
        dialogTotalDuration = dialog.findViewById(R.id.totalDuration)
        dialogCurrentDuration = dialog.findViewById(R.id.currentDuration)
        dialogAudioTitle?.text = File(allAudioFiles[currentPlayingIndex]).name
        dialogProgressSeekBar?.max = mediaPlayer!!.duration

        if (isPlaying) dialogBtnPlayPause?.setImageResource(R.drawable.ic_mobi_audio_pause)
        else   dialogBtnPlayPause?.setImageResource(R.drawable.ic_mobi_audio_play)
        if (getLoop()) btnRepeat.setImageResource(R.drawable.ic_mobi_audio_repeat)
        else btnRepeat.setImageResource(R.drawable.ic_mobi_audio_repeat_one)

        dialogThumbnail?.setImageBitmap(getAudioThumbnail(allAudioFiles[currentPlayingIndex]))
        speedText.text = "${speeds[speeds.indexOf(getPlaySpeed())]}x"


        close.setOnClickListener{
            dialog.dismiss()
        }

        btnNext.setOnClickListener {
            playNext()
        }
        btnPrev.setOnClickListener {
            playPrevious()
        }
        btnRepeat.setOnClickListener {
            if (getLoop()){
                setLoop(false)
                btnRepeat.setImageResource(R.drawable.ic_mobi_audio_repeat_one)
            }
            else{
                setLoop(true)
                btnRepeat.setImageResource(R.drawable.ic_mobi_audio_repeat)
            }

        }
        speedText.setOnClickListener {
            var index = speeds.indexOf(getPlaySpeed())
            index++
            if (index>speeds.size-1) index = 0
            setPlaySpeed(speeds[index])
            speedText.text = "${speeds[index]}x"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val params = PlaybackParams()
                params.speed = getPlaySpeed()
                if (isPlaying)  {
                    mediaPlayer?.playbackParams = params
                }
                else{
                    mediaPlayer?.playbackParams = params
                    mediaPlayer?.pause()


                }
            }
        }

        dialogBtnPlayPause?.setOnClickListener {
            playPause()
        }
        dialogProgressSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer?.seekTo(progress) }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        dialog.show()
    }
    private val audioBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY -> {
                    playPause()
                }
                ACTION_PAUSE -> {
                    playPause()

                }
                ACTION_NEXT -> playNext()
                ACTION_PREVIOUS -> playPrevious()

            }
        }
    }
    private fun playPause(){
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
            binding.btnPlayPause.setImageResource(R.drawable.ic_mobi_audio_play)
            dialogBtnPlayPause?.setImageResource(R.drawable.ic_mobi_audio_play)
            createNotification(File(allAudioFiles[currentPlayingIndex]).name)
            return

        }
        if (mediaPlayer != null && !isPlaying){
            mediaPlayer?.start()
            isPlaying = true
            binding.btnPlayPause.setImageResource(R.drawable.ic_mobi_audio_pause)
            dialogBtnPlayPause?.setImageResource(R.drawable.ic_mobi_audio_pause)
            createNotification(File(allAudioFiles[currentPlayingIndex]).name)
            return
        }
    }
    private fun registerBroadcastReceiver(){
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREVIOUS)
        }
        ContextCompat.registerReceiver( this, audioBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED )
    }
    private fun createNotification(name: String): Notification {
        // Create notification channel (for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, "audio", importance).apply {
                description = "audio"
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create intents for notification actions
        val notificationIntent = Intent(this, MobiAudioPlayer::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val playPauseIntent = if (isPlaying) {
            Intent(ACTION_PAUSE)
        } else {
            Intent(ACTION_PLAY)
        }
        val playPausePendingIntent = PendingIntent.getBroadcast(this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)
        val nextIntent = Intent(ACTION_NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)
        val previousIntent = Intent(ACTION_PREVIOUS)
        val previousPendingIntent = PendingIntent.getBroadcast(this, 0, previousIntent, PendingIntent.FLAG_IMMUTABLE)



        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(name)
            .setSmallIcon(R.drawable.ic_mobi_audio_notification_icon)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_mobi_audio_previous, "Previous", previousPendingIntent)
            .addAction(if (isPlaying) R.drawable.ic_mobi_audio_pause else R.drawable.ic_mobi_audio_play, "Play/Pause", playPausePendingIntent)
            .addAction(R.drawable.ic_mobi_audio_next, "Next", nextPendingIntent)
            .setAutoCancel(true)
            .setNotificationSilent()
            .setShowWhen(false)
            .setOngoing(true)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(null)
            )
            .build()

        // Display the notification
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, notification)
        return notification
    }
    private fun playAudio(audioPath: String) {
        try {
            stopProgress()
            adapter?.positionChanged(currentPlayingIndex)
            mediaPlayer?.release()
            mediaPlayer = null
//                val playIntent = Intent(this, AudioService::class.java).apply {
//                    action = ACTION_PLAY
//                }
//                startService(playIntent)
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(audioPath)
            mediaPlayer?.prepare()
            binding.btnPlayPause.setImageResource(R.drawable.ic_mobi_audio_pause)
            dialogBtnPlayPause?.setImageResource(R.drawable.ic_mobi_audio_pause)
            mediaPlayer?.setOnCompletionListener {
                if (getLoop()) currentPlayingIndex++

                if (currentPlayingIndex < allAudioFiles.size) {
                    playAudio(allAudioFiles[currentPlayingIndex])
                } else {
//                          for last item whether its play or not
//                        mediaPlayer?.release()
//                        mediaPlayer = null
                    currentPlayingIndex = 0
                    playAudio(allAudioFiles[currentPlayingIndex])
                }
            }

            binding.audioTitle.text = File(allAudioFiles[currentPlayingIndex]).name
            dialogAudioTitle?.text = File(allAudioFiles[currentPlayingIndex]).name
            dialogTotalDuration?.text = getAudioDuration(mediaPlayer?.duration)
            binding.thumbnail.setImageBitmap(getAudioThumbnail(allAudioFiles[currentPlayingIndex]))
            dialogThumbnail?.setImageBitmap(getAudioThumbnail(allAudioFiles[currentPlayingIndex]))
            binding.progressSeekBar.max = mediaPlayer!!.duration
            dialogProgressSeekBar?.max = mediaPlayer!!.duration
            isPlaying = true
            createNotification(File(allAudioFiles[currentPlayingIndex]).name)
            startProgress()

            // Start playing the audio
            mediaPlayer?.start()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val params = PlaybackParams()
                params.speed = getPlaySpeed()
                if (isPlaying)  {
                    mediaPlayer?.playbackParams = params
                }
                else{
                    mediaPlayer?.pause()
                    mediaPlayer?.playbackParams = params

                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun playNext(){
        if (allAudioFiles.size != 0){
            currentPlayingIndex++
            if (currentPlayingIndex < allAudioFiles.size) {
                playAudio(allAudioFiles[currentPlayingIndex])
            } else {
//                          for last item whether its play or not
//                        mediaPlayer?.release()
//                        mediaPlayer = null
                currentPlayingIndex = 0
                playAudio(allAudioFiles[currentPlayingIndex])
            }
        }
    }
    private fun playPrevious(){
        if (allAudioFiles.size != 0){
            currentPlayingIndex--
            if (currentPlayingIndex < 0) {
                currentPlayingIndex = allAudioFiles.size-1
                playAudio(allAudioFiles[currentPlayingIndex])
            } else {
//                          for last item whether its play or not
//                        mediaPlayer?.release()
//                        mediaPlayer = null
                playAudio(allAudioFiles[currentPlayingIndex])
            }
        }
    }
    private fun getAudioThumbnail(audioPath: String): Bitmap? {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(audioPath)

            val rawArt: ByteArray? = retriever.embeddedPicture

            return if (rawArt != null) {
                BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size)
            } else {
                BitmapFactory.decodeResource(resources, R.drawable.mobi_audio_music_icon)
            }
        }
        catch (e:IllegalArgumentException){
            return  BitmapFactory.decodeResource(resources, R.drawable.mobi_audio_music_icon)
        }

    }
    private fun startProgress(){
        currentDurationJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                try {
                    val duration = getAudioDuration(mediaPlayer?.currentPosition)
                    dialogCurrentDuration?.text = duration
                    binding.progressSeekBar.progress = mediaPlayer!!.currentPosition
                    dialogProgressSeekBar?.progress = mediaPlayer!!.currentPosition
                }
                catch (e:IllegalStateException) {
                }

                delay(1000)


            }
        }
    }
    private fun stopProgress(){
        currentDurationJob?.cancel()
    }
    private fun getAudioDuration(duration: Int?): String {
        val totalSeconds = duration?.div(1000)
        val hours = totalSeconds?.div(3600)
        val minutes = (totalSeconds?.rem(3600))?.div(60)
        val seconds = totalSeconds?.rem(60)

        val durationString = if (hours != null && hours > 0) {
            "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        } else {
            "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        }

        return durationString
    }
    override fun onDestroy() {
        notificationManager?.cancelAll()
        unregisterReceiver(audioBroadcastReceiver)
        mediaPlayer?.release()
        stopProgress()
        super.onDestroy()
    }
    override fun clicked(path:String,position:Int) {
        currentPlayingIndex = position
        playAudio(path)
    }
}