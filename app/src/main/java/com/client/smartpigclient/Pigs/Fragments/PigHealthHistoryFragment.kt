package com.client.smartpigclient.Pigs.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.client.smartpigclient.R
import com.client.smartpigclient.Pigs.Adapter.PigHealthHistoryAdapter
import com.client.smartpigclient.Pigs.Api.PigsRI
import com.client.smartpigclient.Pigs.Model.PigHistoryResponse
import com.client.smartpigclient.Pigs.Model.PigsModel
import com.client.smartpigclient.Utils.TokenManager
import com.client.smartpigclient.Utils.formatDate
import com.client.smartpigclient.databinding.FragmentPigHealthHistoryBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale

class PigHealthHistoryFragment : Fragment() {

    private var _binding: FragmentPigHealthHistoryBinding? = null
    private val binding get() = _binding!!

    private var pigId: String? = null

    private var pig: PigsModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pigId = it.getString(ARG_PIG_ID)
            pig = it.getParcelable(ARG_PIG)
        }
    }
    companion object {
        private const val ARG_PIG_ID = "pig_id"
        private const val ARG_PIG = "pig"

        @JvmStatic
        fun newInstance(pig: PigsModel) =
            PigHealthHistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PIG_ID, pig.id)
                    putParcelable(ARG_PIG, pig)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPigHealthHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val token = TokenManager.getToken(requireContext())
        if (token.isNotEmpty()) {
            fetchPigHealthHistory(token)
        } else {
            Toast.makeText(requireContext(), "Auth token not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPigHealthHistory(token: String) {

        val pigId = this.pigId ?: run {
            Toast.makeText(requireContext(), "Pig ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val api = PigsRI.getPigHealthHistoryApi(token)

        lifecycleScope.launch {
            try {
                val pigHistory: PigHistoryResponse = api.getPigHealthHistory(pigId)

                // Sort healthHistory by actionAt descending (newest first)
                val sortedHealthHistory = pigHistory.healthHistory.sortedByDescending { event ->
                    try {
                        event.actionAt?.let {
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it)
                        } ?: Date(0) // fallback if null
                    } catch (e: Exception) {
                        Date(0) // fallback if parse fails
                    }
                }



                val priceText = pigHistory.healthHistory.firstOrNull()?.price?.toInt()?.toString() ?: "N/A"
                binding.tvPigPrice.text = "Price: $priceText PHP"
                // Pig Snapshot (Header)
                binding.tvPigName.text = "Name: ${pigHistory.pig.name}"

                val latest = sortedHealthHistory.firstOrNull()
                binding.tvFeed.text = "Feed: ${latest?.feed ?: "N/A"}"

                binding.tvPigBreed.text = "Breed: ${pigHistory.pig.breed ?: "N/A"}"
                binding.tvPigAge.text = "Age: ${pigHistory.healthHistory.firstOrNull()?.age ?: "N/A"}"
                binding.tvPigWeight.text = "Weight: ${pigHistory.healthHistory.firstOrNull()?.weight ?: "N/A"}"

                binding.tvPigCage.text = "Cage: ${pigHistory.healthHistory.firstOrNull()?.cageName ?: "N/A"}"
                binding.tvPigGender.text = "Gender: ${pigHistory.healthHistory.firstOrNull()?.gender ?: "N/A"}"

                binding.tvBirthDate.text = "BirthDate: ${pigHistory.healthHistory.firstOrNull()?.birthDate ?: "N/A"}"

                // Load pig image
                val pigImageUrl = pig?.image_url ?: pigHistory.pig.image_url
                if (!pigImageUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(pigImageUrl)
                        .placeholder(R.drawable.pig)
                        .into(binding.imagePlaceHolder)
                } else {
                    binding.imagePlaceHolder.setImageResource(R.drawable.pig)
                }


                // Setup RecyclerView for Health History
                val adapter = PigHealthHistoryAdapter(sortedHealthHistory)
                binding.rvHealthHistory.layoutManager = LinearLayoutManager(requireContext())
                binding.rvHealthHistory.adapter = adapter

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to fetch health history", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav?.visibility = View.GONE
    }

    override fun onDestroyView() {
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav?.visibility = View.VISIBLE
        super.onDestroyView()
        _binding = null
    }
}
