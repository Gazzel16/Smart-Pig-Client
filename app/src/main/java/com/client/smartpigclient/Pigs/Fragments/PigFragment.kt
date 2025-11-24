package com.client.smartpigclient.Pigs.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.client.smartpigclient.MainActivity
import com.client.smartpigclient.Pigs.Adapter.PigsAdapter
import com.client.smartpigclient.Pigs.Model.PigsModel
import com.client.smartpigclient.Pigs.Api.FetchPigsRI
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.FragmentPigBinding
import kotlinx.coroutines.launch

class PigFragment : Fragment() {

    private var _binding: FragmentPigBinding? = null
    private val binding get() = _binding!!

    private lateinit var pigsAdapter: PigsAdapter
    private val pigList = mutableListOf<PigsModel>()

    private var cageId: String? = null
    private var cageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cageId = it.getString(ARG_CAGE_ID)
            cageName = it.getString(ARG_CAGE_NAME)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addPig.setOnClickListener {
            val addPigFragment = AddPigFragment.newInstance(cageId ?: "", cageName ?: "")
            parentFragmentManager.beginTransaction()
                .replace(
                    (requireActivity() as MainActivity).binding.fragmentContainer.id,
                    addPigFragment
                )
                .addToBackStack(null)
                .commit()
        }


        // 1️⃣ Setup RecyclerView
        binding.rvPigs.layoutManager = LinearLayoutManager(requireContext())
        pigsAdapter = PigsAdapter(pigList, parentFragmentManager)
        binding.rvPigs.adapter = pigsAdapter

        // 2️⃣ Fetch pigs from API
        fetchPigs()
    }

    private fun fetchPigs() {
        val api = FetchPigsRI.getInstance()

        lifecycleScope.launch {
            try {
                val pigs = api.fetchPigs(cageId ?: "")
                pigList.clear()
                pigList.addAll(pigs)
                pigsAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch pigs: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val ARG_CAGE_ID = "cage_id"
        private const val ARG_CAGE_NAME = "cage_name"

        @JvmStatic
        fun newInstance(cageId: String, cageName: String) =
            PigFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CAGE_ID, cageId)
                    putString(ARG_CAGE_NAME, cageName)
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
