package com.client.smartpigclient.Dashboard.Adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.client.smartpigclient.Pigs.Model.PigsModel
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.ItemDashboardBinding
import com.client.smartpigclient.databinding.ItemPigBinding

class DashBoardAdapter(
    private val items: List<PigsModel>,
    private val onClick: (PigsModel) -> Unit
) : RecyclerView.Adapter<DashBoardAdapter.PigViewHolder>() {

    inner class PigViewHolder(val binding: ItemDashboardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PigsModel) {

            // Breed
            binding.breed.text = "Breed: ${item.breed ?: "Unknown"}"

            // Cage
            binding.cage.text = "Cage: ${item.cageName ?: "No Cage"}"

            // Alive / Dead
            binding.isAlive.text = if (item.isAlive == true) "Status: Alive" else "Status: Dead"

            // Sold Badge
            if (item.isSold == true) {
                binding.isSold.text = "Sold by:. ${item.buyerName}"
                binding.isSold.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#E74C3C"))
            } else {
                binding.isSold.text = "Available"
                binding.isSold.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#2ECC71"))

            }

            // Price
            binding.price.text = "Price: ₱${item.price?.toInt() ?: 0}"

            // Image
            Glide.with(binding.root.context)
                .load(item.image_url)
                .placeholder(R.drawable.pig)
                .centerCrop()
                .into(binding.imagePlaceHolder)

            // Click event
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PigViewHolder {
        val binding = ItemDashboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PigViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PigViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}


















