package com.client.smartpigclient.Utils

import android.content.Context
import android.widget.ArrayAdapter
import com.client.smartpigclient.R

fun getSpinnerAdapter(context: Context, items: List<String>): ArrayAdapter<String> {
    return ArrayAdapter(
        context,
        R.layout.item_spinner, // your custom spinner item
        items
    ).apply {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // dropdown list
    }
}
