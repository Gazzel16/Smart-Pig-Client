package com.client.smartpigclient.Pigs.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.client.smartpigclient.Pigs.Api.FetchPigsByIdRI
import com.client.smartpigclient.Pigs.Fragments.UpdatePigFragment
import com.client.smartpigclient.Pigs.Model.PigsModel
import com.client.smartpigclient.Pigs.PigsQrDialog
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.ItemPigBinding

class PigsAdapter(
    private val pigsList: List<PigsModel>,
    private val onItemClick: (PigsModel) -> Unit,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<PigsAdapter.PigViewHolder>() {

    inner class PigViewHolder(val binding: ItemPigBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PigViewHolder {
        val binding = ItemPigBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PigViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PigViewHolder, position: Int) {
        val pig = pigsList[position]
        val binding = holder.binding


        if (pig.isSold == true){
            binding.isSold.text ="Sold by: ${pig.buyerName}"
            binding.isSold.background.setTint(Color.parseColor("#C50B01")) // light red background
            binding.isSold.setTextColor(Color.parseColor("#FFFFFFFF"))
        }else{
            binding.isSold.text ="Available"
            binding.isSold.background.setTint(Color.parseColor("#459E48")) // light red background
            binding.isSold.setTextColor(Color.parseColor("#FFFFFFFF"))
        }


        // Text fields
        binding.name.text = pig.name
        binding.breed.text = pig.breed ?: "Unknown Breed"
        binding.isAlive.text = if (pig.isAlive == true) "Alive" else "Dead"
        binding.birthDate.text = pig.birthDate ?: "Unknown Birthdate"
        binding.gender.text = pig.gender ?: "Unknown Gender"


        // Images
        Glide.with(binding.root.context)
            .load(pig.image_url ?: R.drawable.pig)
            .into(binding.imageHolder)

        // Load pig QR
        val fullQrUrl = if (pig.qr_url?.startsWith("http") == true)
            pig.qr_url
        else
            "${FetchPigsByIdRI.BASE_URL}${pig.qr_url?.removePrefix("/") ?: ""}"

        Glide.with(binding.root.context)
            .load(fullQrUrl)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.qrCode)

        binding.qrCode.setOnClickListener {
            fullQrUrl?.let { url ->
                val fragment = PigsQrDialog.newInstance(url, pig.id)
                fragment.show(
                    (holder.binding.root.context as FragmentActivity).supportFragmentManager,
                    "qrDialog"
                )
            }

        }


        // Show all details click
        binding.editDetails.setOnClickListener {
            val fragment = UpdatePigFragment.newInstance(pig, pig.cageId ?: "N/A")
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.root.setOnClickListener {
            onItemClick(pig) // trigger click callback
        }
    }

    override fun getItemCount(): Int = pigsList.size
}
