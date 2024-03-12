package com.example.audioplayer

import android.content.Context
import android.content.Context.MODE_PRIVATE


fun Context.setPlaySpeed(value : Float){
    val preference = getSharedPreferences("pref",MODE_PRIVATE)
    val editor = preference.edit()
    editor.putFloat("speed", value)
    editor.apply()

}

fun Context.getPlaySpeed():Float{
    val preference = getSharedPreferences("pref",MODE_PRIVATE)
    return preference.getFloat("speed", 1.0F)
}

fun Context.setLoop(value : Boolean){
    val preference = getSharedPreferences("pref",MODE_PRIVATE)
    val editor = preference.edit()
    editor.putBoolean("loop", value)
    editor.apply()

}

fun Context.getLoop():Boolean{
    val preference = getSharedPreferences("pref",MODE_PRIVATE)
    return preference.getBoolean("loop", true)
}