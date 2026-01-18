package com.client.smartpigclient.Pigs.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.client.smartpigclient.Pigs.Model.PigFeedingSchedule
import com.client.smartpigclient.databinding.ItemPigFeedingScheduleBinding

class PigFeedingScheduleAdapter(
    private val schedules: List<PigFeedingSchedule>
) : RecyclerView.Adapter<PigFeedingScheduleAdapter.PigViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PigViewHolder {
        val binding = ItemPigFeedingScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PigViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PigViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.bind(schedule)
    }

    override fun getItemCount(): Int = schedules.size

    inner class PigViewHolder(private val binding: ItemPigFeedingScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: PigFeedingSchedule) {
            binding.tvPigType.text = schedule.pig
            binding.tvCurrentTime.text = "Current Feeding Schedule: ${schedule.currentTime}"
            binding.tvNextSchedule.text =
                "Next Feeding Schedule: ${schedule.nextSchedule} (in ${schedule.betweenHours})"
            binding.tvAdvice.text = schedule.advice
            // Optional: If you have a note TextView, you can bind it here
            // binding.tvNote.text = "This Feeding Schedule is for reference only"
        }
    }
}
