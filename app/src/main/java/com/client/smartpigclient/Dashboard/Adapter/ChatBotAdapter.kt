package com.client.smartpigclient.Dashboard.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.client.smartpigclient.R
import com.client.smartpigclient.Dashboard.Model.ChatMessage

class ChatAdapter(private val list: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val USER = 1
    private val BOT = 2

    override fun getItemViewType(position: Int): Int {
        return if (list[position].isUser) USER else BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == USER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_bot, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = list[position]

        if (holder is UserViewHolder) {
            holder.message.text = message.message
        } else if (holder is BotViewHolder) {
            holder.message.text = message.message
        }
    }

    override fun getItemCount(): Int = list.size

    fun addMessage(msg: ChatMessage) {
        list.add(msg)
        notifyItemInserted(list.size - 1)
    }

    // ---------------- View Holders ----------------

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message: TextView = itemView.findViewById(R.id.userChat)
    }

    inner class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message: TextView = itemView.findViewById(R.id.userChat)
    }
}
