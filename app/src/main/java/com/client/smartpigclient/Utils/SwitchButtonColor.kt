package com.client.smartpigclient.Utils

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial

fun SwitchMaterial.updateRelayColor(isOn: Boolean) {
    val color = if (isOn) {
        Color.parseColor("#4CAF50") // ON
    } else {
        Color.parseColor("#9E9E9E") // OFF
    }

    thumbTintList = ColorStateList.valueOf(color)
    trackTintList = ColorStateList.valueOf(color)
}


