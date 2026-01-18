package com.client.smartpigclient.Dashboard.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.client.smartpigclient.Pigs.Adapter.PigFeedingScheduleAdapter
import com.client.smartpigclient.Pigs.Api.PigsRI
import com.client.smartpigclient.Pigs.Model.PigFeedingSchedule
import com.client.smartpigclient.Utils.TokenManager
import com.client.smartpigclient.databinding.FragmentDashboardFeedingScheduleBinding
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DashboardFeedingScheduleFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var pigFeedingScheduleAdapter: PigFeedingScheduleAdapter
    private val pigFeedingScheduleList = mutableListOf<PigFeedingSchedule>()
    private var _binding: FragmentDashboardFeedingScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDashboardFeedingScheduleBinding.inflate(inflater, container, false)

        feedingSchedule()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Example usage:
        // binding.textViewTitle.text = param1
    }

    private fun fetchFeedingSchedulesFromApi() {
        lifecycleScope.launch {
            try {
                // Get token from your TokenManager
                val token = TokenManager.getToken(requireContext())
                val api = PigsRI.getPigFeedingSchedule(token)

                // Make the API call
                val schedules = api.getFeedingSchedules()

                // Update the list and notify adapter
                pigFeedingScheduleList.clear()
                pigFeedingScheduleList.addAll(schedules)
                pigFeedingScheduleAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
                // Optional: Show a Toast or Snackbar
                // Toast.makeText(requireContext(), "Failed to fetch schedules", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun feedingSchedule() {
        pigFeedingScheduleAdapter = PigFeedingScheduleAdapter(pigFeedingScheduleList)
        binding.rvFeedingSchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeedingSchedule.adapter = pigFeedingScheduleAdapter

        fetchFeedingSchedulesFromApi()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // avoid memory leaks
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFeedingScheduleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
