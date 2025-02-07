package com.example.audioplayer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobiaudioplayer.Data
import com.example.mobiaudioplayer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class AudioAdapter(
    val context:Context,
    val icon:Bitmap,
    val iconSize:Int, val list: ArrayList<Data>, val listener: ClickListener) : RecyclerView.Adapter<AudioAdapter.AudioAdapterViewHolder>() {
    var currentPlayingIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioAdapterViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.audio_item, parent, false)
        return AudioAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioAdapterViewHolder, position: Int) {
        holder.audioTitle.text = File(list[position].path).name

        if (position==currentPlayingIndex) holder.audioTitle.setTextColor(Color.parseColor("#346ADE"))
        else  holder.audioTitle.setTextColor(Color.parseColor("#000000"))
        Glide.with(context).load(icon).override(iconSize).into(holder.audioImage)
        CoroutineScope(Dispatchers.Main).launch {
            holder.duration.text =getDurationFromMillis(list[position].duration)
        }

        holder.itemView.setOnClickListener { // Triggers click upwards to the adapter on click
            listener.clicked(list[position].path,position)
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
        fun getDurationFromMillis(duration: String): String {
        val totalSeconds = duration?.toInt()?.div(1000)
        val hours = totalSeconds?.div(3600)
        val minutes = (totalSeconds?.rem(3600))?.div(60)
        val seconds = totalSeconds?.rem(60)
        if (hours != null && hours > 0) {
            return "$hours:${minutes.toString().padStart(2, '0')}:${
                seconds.toString().padStart(2, '0')
            }"
        } else {
            return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        }

    }
    interface ClickListener {
            fun clicked(path:String,position: Int)
    }
}