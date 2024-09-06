package com.example.audioplayer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiaudioplayer.R
import java.io.File


class AudioAdapter(val context:Context, val list: List<String>, val listener : ClickListener) : RecyclerView.Adapter<AudioAdapter.AudioAdapterViewHolder>() {
    var currentPlayingIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioAdapterViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.audio_item, parent, false)
        return AudioAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioAdapterViewHolder, position: Int) {
        holder.audioTitle.text = File(list[position]).name

        if (position==currentPlayingIndex) holder.audioTitle.setTextColor(Color.parseColor("#346ADE"))
        else  holder.audioTitle.setTextColor(Color.parseColor("#000000"))
        holder.audioImage.setImageBitmap(getAudioThumbnail(File(list[position]).path))
        holder.duration.text = getAudioDuration(File(list[position]).path)
        holder.itemView.setOnClickListener { // Triggers click upwards to the adapter on click
            listener.clicked(list[position],position)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getItemCount(): Int {
        return list.size
    }

    fun positionChanged(currentPosition: Int) {
        currentPlayingIndex =currentPosition
        notifyDataSetChanged()
    }
    class AudioAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val audioTitle: TextView = view.findViewById(R.id.audio_title)
        val card: CardView = view.findViewById(R.id.card)
        val audioImage: ImageView = view.findViewById(R.id.audioImage)
        val duration: TextView = view.findViewById(R.id.duration)

    }
    private fun getAudioThumbnail(audioPath: String): Bitmap? {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(audioPath)
            val rawArt: ByteArray? = retriever.embeddedPicture

            return if (rawArt != null) {
                BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size)
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.mobi_audio_music_icon)
            }
        }
        catch (e:IllegalStateException){
            return  BitmapFactory.decodeResource(context.resources, R.drawable.mobi_audio_music_icon)
        }
    }

    private fun getAudioDuration(audioPath: String): String {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(audioPath)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

        return duration(durationString?.toInt())
    }

    private fun duration(duration: Int?): String {
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
    interface ClickListener {
            fun clicked(path:String,position: Int)
    }
}