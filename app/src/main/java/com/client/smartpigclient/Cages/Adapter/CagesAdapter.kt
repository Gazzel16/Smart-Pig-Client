package com.client.smartpigclient.Cages.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.client.smartpigclient.Cages.Api.FetchCageRI
import com.client.smartpigclient.Cages.CagesQrDialog
import com.client.smartpigclient.Cages.Model.CageModel
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.ItemCagesBinding

class CagesAdapter(
    private val cagesList: List<CageModel>,
    private val onItemClick: (CageModel) -> Unit
) : RecyclerView.Adapter<CagesAdapter.CageViewHolder>() {

    inner class CageViewHolder(val binding: ItemCagesBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CageViewHolder {
        val binding = ItemCagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CageViewHolder, position: Int) {
        val cage = cagesList[position]
        val binding = holder.binding

        // Set cage name
        binding.name.text = "${cage.name}"

        // Set pig count
        binding.pigCount.text = "Pigs: ${cage.pigCount}"

        // Convert QR URL to full URL if it's local
        val fullQrUrl = if (cage.qr_url?.startsWith("http") == true)
            cage.qr_url
        else
            "${FetchCageRI.BASE_URL}${cage.qr_url?.removePrefix("/") ?: ""}"

        // Load QR code if available
        if (!fullQrUrl.isNullOrEmpty()) {
            Glide.with(binding.root.context)
                .load(fullQrUrl)
                .placeholder(R.drawable.qr_ic)
                .into(binding.qrCode)
        } else {
            binding.qrCode.setImageResource(R.drawable.qr_ic)
        }

        // QR code click listener
        binding.qrCode.setOnClickListener {
            val fragment = CagesQrDialog.newInstance(fullQrUrl)
            fragment.show((binding.root.context as FragmentActivity).supportFragmentManager, "qrDialog")
        }

        binding.root.setOnClickListener {
            onItemClick(cage)
        }
    }

    override fun getItemCount(): Int = cagesList.size
}
