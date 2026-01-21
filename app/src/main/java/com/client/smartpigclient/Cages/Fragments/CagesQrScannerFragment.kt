package com.client.smartpigclient.Cages.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.client.smartpigclient.Cages.Api.FetchCageByIdRI
import com.client.smartpigclient.Cages.Api.PigsCountRI
import com.client.smartpigclient.Cages.CustomScannerActivity

import com.client.smartpigclient.Cages.Model.CageModel
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.FragmentCagesQrScannerBinding
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CagesQrScannerFragment : Fragment() {

    private var _binding: FragmentCagesQrScannerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCagesQrScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start scanning automatically
        startScanner()

        // Scan again button
        binding.scanAgainBtn.setOnClickListener {
            startScanner()
        }
    }

    private fun startScanner() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan Cage QR Code")
        integrator.setCameraId(0)

        integrator.setOrientationLocked(true)
        integrator.captureActivity = CustomScannerActivity::class.java

        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val cageId = result.contents
                fetchCageDetails(cageId)
            } else {
                Toast.makeText(requireContext(), "Scan canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCageDetails(cageId: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", "") ?: ""


        val cageApi = FetchCageByIdRI.getInstance(token)
//        val pigsCountApi = PigsCountRI.getInstance() // <-- adjust to your actual class name

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch single cage
                val cage: CageModel = cageApi.fetchCageById(cageId)

                // Fetch pigs count for this cage
                try {
                    val unsoldPigCount = cage.pigs.count { it.isSold != true }
                    cage.pigCount = unsoldPigCount
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error fetching pigs count: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                // Update UI
                withContext(Dispatchers.Main) {
                    binding.cageName.text = "${cage.name}"
                    binding.pigsCount.text = "Pigs: ${cage.pigCount}"
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error fetching cage",
                        Toast.LENGTH_SHORT
                    ).show()
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
