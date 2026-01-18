package com.client.smartpigclient.Pigs.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.client.smartpigclient.Pigs.Model.PigHealthEvent
import com.client.smartpigclient.Utils.formatDate
import com.client.smartpigclient.Utils.formatDateWithoutHours
import com.client.smartpigclient.databinding.ItemPigHealthHistoryBinding

class PigHealthHistoryAdapter(
    private val healthHistory: List<PigHealthEvent>
) : RecyclerView.Adapter<PigHealthHistoryAdapter.PigHealthViewHolder>() {

    inner class PigHealthViewHolder(val binding: ItemPigHealthHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PigHealthViewHolder {
        val binding = ItemPigHealthHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PigHealthViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PigHealthViewHolder, position: Int) {
        val healthEvent: PigHealthEvent = healthHistory[position]
        val binding = holder.binding

        // Health Event Info
        binding.tvAge.text = "Age: ${healthEvent.age ?: "N/A"}"
        binding.tvWeight.text = "Weight: ${healthEvent.weight ?: "N/A"}"
        binding.tvFeed.text = "Feed: ${healthEvent.feed ?: "N/A"}"
        binding.tvPigType.text = "Pig Type: ${healthEvent.pigType ?: "N/A"}"

        binding.tvIllness.text = "Illness: ${healthEvent.illness ?: "N/A"}"
        binding.tvVaccine.text = "Vaccine: ${healthEvent.vaccine ?: "N/A"}"
        binding.tvVaccineDate.text = "Vaccine Date: ${formatDateWithoutHours(healthEvent.vaccineDate)}"
        binding.tvVaccineNextDue.text = "Vaccine Next Due: ${formatDateWithoutHours(healthEvent.vaccineNextDue)}"
        binding.tvLastCheckup.text = "Last Checkup: ${formatDateWithoutHours(healthEvent.lastCheckup)}"
        binding.tvHealthStatus.text = "Health Status: ${healthEvent.healthStatus ?: "N/A"}"
        binding.tvCheckupDate.text = "Checkup Date: ${formatDateWithoutHours(healthEvent.checkupDate)}"
        binding.tvNotes.text = "Notes: ${healthEvent.notes ?: "N/A"}"

        binding.action.text = "Action: ${formatDate(healthEvent.actionAt)}"
    }

    override fun getItemCount(): Int = healthHistory.size
}
