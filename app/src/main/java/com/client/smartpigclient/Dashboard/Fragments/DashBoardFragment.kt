package com.client.smartpigclient.Dashboard.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.client.smartpigclient.Cages.Fragments.CagesQrScannerFragment
import com.client.smartpigclient.Dashboard.Adapter.DashBoardAdapter
import com.client.smartpigclient.Dashboard.Api.DashBoardApi
import com.client.smartpigclient.Dashboard.Api.DashBoardRI

import com.client.smartpigclient.MainActivity
import com.client.smartpigclient.Pigs.Fragments.PigDetailsFragment
import com.client.smartpigclient.Pigs.Fragments.ScanPigFragment
import com.client.smartpigclient.Pigs.Model.PigsModel
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.FragmentDashBoardBinding
import kotlinx.coroutines.launch

class DashBoardFragment : Fragment() {

    private var _binding: FragmentDashBoardBinding? = null
    private val binding get() = _binding!!

    private lateinit var pigAdapter: DashBoardAdapter
    private val pigsList = mutableListOf<PigsModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDashBoardBinding.inflate(inflater, container, false)


        binding.chatBotCard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireActivity() as MainActivity).binding.fragmentContainer.id,
                    DashboardChatBotFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.sensorCard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireActivity() as MainActivity).binding.fragmentContainer.id,
                    DashboardSensorFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.analyticsCard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireActivity() as MainActivity).binding.fragmentContainer.id,
                    DashboardAnalyticsFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.scanPigsCard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireActivity() as MainActivity).binding.fragmentContainer.id,
                    ScanPigFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.cardCage.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireActivity() as MainActivity).binding.fragmentContainer.id,
                    CagesQrScannerFragment())
                .addToBackStack(null)
                .commit()
        }

        setupRecyclerView()
        fetchPigs()
        return binding.root
    }

    private fun setupRecyclerView() {
        pigAdapter = DashBoardAdapter(pigsList) { pig ->
            // On item click
            // You can navigate to pig details here

            val bundle = Bundle().apply {
                putParcelable("selected_pig", pig)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardPigDetailsFragment().apply { arguments = bundle })
                .addToBackStack(null)
                .commit()
        }

        binding.rvPigs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPigs.adapter = pigAdapter
    }

    private fun fetchPigs() {
        lifecycleScope.launch {
            try {
                val api = DashBoardRI.getInstance()
                val response = api.fetchAllPigs()

                pigsList.clear()
                pigsList.addAll(response)
                pigAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
