package com.client.smartpigclient.Cages.Fragments

import com.client.smartpigclient.R

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.client.smartpigclient.Cages.Adapter.CagesAdapter
import com.client.smartpigclient.Cages.Model.CageModel
import com.client.smartpigclient.Cages.Api.FetchCageRI
import com.client.smartpigclient.Cages.Api.PigsCountApi
import com.client.smartpigclient.Cages.Api.PigsCountRI
import com.client.smartpigclient.Cages.CagesDialog
import com.client.smartpigclient.Dashboard.Fragments.DashBoardFragment
import com.client.smartpigclient.MainActivity
import com.client.smartpigclient.Pigs.Fragments.PigFragment
import com.client.smartpigclient.databinding.FragmentCagesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CagesFragment : Fragment() {

    private var _binding: FragmentCagesBinding? = null
    private val binding get() = _binding!!

    private lateinit var cagesAdapter: CagesAdapter
    private val cagesList = mutableListOf<CageModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.beginTransaction()
                        .replace(
                            (requireActivity() as MainActivity)
                                .binding.fragmentContainer.id,
                            DashBoardFragment()
                        )
                        .commit()
                }
            }
        )



        binding.addCage.setOnClickListener {
            val modal = CagesDialog(object : CagesDialog.OnCageAddedListener {
                override fun onCageAdded() {
                    fetchCages() // refresh the RecyclerView
                }
            })
            modal.show(parentFragmentManager, "CagesModal")
        }


        // Setup RecyclerView
        binding.rvCages.layoutManager = LinearLayoutManager(requireContext())
        cagesAdapter = CagesAdapter(
            cagesList,

            onItemClick = { cage ->
                val pigFragment = PigFragment.newInstance(cage.id, cage.name)
                parentFragmentManager.beginTransaction()
                    .replace((requireActivity() as MainActivity).binding.fragmentContainer.id, pigFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
        binding.rvCages.adapter = cagesAdapter

        // Load cages from API
        fetchCages()
    }

    private fun fetchCages() {
        val api = FetchCageRI.getInstance()
        val pigsCountRI = PigsCountRI.getInstance()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cagesFromApi = api.fetchCage() // suspend call

                cagesFromApi.forEach{ cage ->
                    try{
                        val pigsCountResponse = pigsCountRI.pigsCount(cage.id)
                        cage.pigCount = pigsCountResponse.pigsCount
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Error fetching cages: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }


                withContext(Dispatchers.Main) {
                    cagesList.clear()
                    cagesList.addAll(cagesFromApi)
                    cagesAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching cages: ${e.message}", Toast.LENGTH_LONG).show()
                }
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
