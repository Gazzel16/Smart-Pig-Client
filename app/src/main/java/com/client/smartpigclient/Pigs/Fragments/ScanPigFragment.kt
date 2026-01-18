package com.client.smartpigclient.Pigs.Fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.client.smartpigclient.Pigs.Api.FetchPigsByIdRI
import com.client.smartpigclient.Pigs.Model.PigsModel
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.FragmentScanPigBinding
import com.client.smartpigclient.Cages.CustomScannerActivity
import com.client.smartpigclient.Utils.TokenManager
import com.client.smartpigclient.Utils.formatDateWithoutHours
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanPigFragment : Fragment() {

    private var _binding: FragmentScanPigBinding? = null
    private val binding get() = _binding!!
    private var currentPig: PigsModel? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentScanPigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start scanner automatically
        startScanner()
        detailSections()

        binding.markAsSold.setOnClickListener {
            if (currentPig == null) {
                Toast.makeText(requireContext(), "No pig loaded to mark as sold", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Open BottomModalFragment, passing current pig and listener
            val modal = BottomModalFragment(currentPig!!, object : BottomModalListener {
                override fun onMarkAsSold(updatedPig: PigsModel) {
                    // Update local currentPig with the patched data from server
                    currentPig = updatedPig

                    // Update the UI
                    binding.tvIsSold.text = if (updatedPig.isSold == true) "Yes" else "No"
                    binding.tvBuyerName.text = updatedPig.buyerName ?: "N/A"

                    // Disable button if pig is sold
                    binding.markAsSold.isEnabled = updatedPig.isSold != true
                    binding.markAsSold.alpha = if (updatedPig.isSold == true) 0.5f else 1f
                }
            })

            // Show the modal
            modal.show(parentFragmentManager, "BottomModal")
        }


        binding.qrScan.setOnClickListener { startScanner() }
        binding.backBtn.setOnClickListener { requireActivity().onBackPressed() }
    }

    private fun detailSections(){

        val tint = ContextCompat.getColor(requireContext(), R.color.blueGreen)
        val noTint = ColorStateList.valueOf(Color.TRANSPARENT)

        binding.overview.setTextColor(Color.parseColor("#FFFFFF"))
        binding.health.setTextColor(Color.parseColor("#000000"))
        binding.salesIn.setTextColor(Color.parseColor("#000000"))

        binding.overview.backgroundTintList = ColorStateList.valueOf(tint)
        binding.health.backgroundTintList = noTint
        binding.salesIn.backgroundTintList = noTint

        binding.overview.setOnClickListener {

            binding.overview.backgroundTintList = ColorStateList.valueOf(tint)
            binding.health.backgroundTintList = noTint
            binding.salesIn.backgroundTintList = noTint

            binding.overview.setTextColor(Color.parseColor("#FFFFFF"))
            binding.health.setTextColor(Color.parseColor("#000000"))
            binding.salesIn.setTextColor(Color.parseColor("#000000"))


            binding.overViewTab.visibility = View.VISIBLE
            binding.healthTab.visibility = View.GONE
            binding.marketValueTab.visibility = View.GONE
        }

        binding.health.setOnClickListener {

            binding.health.backgroundTintList = ColorStateList.valueOf(tint)
            binding.overview.backgroundTintList = noTint
            binding.salesIn.backgroundTintList = noTint

            binding.overview.setTextColor(Color.parseColor("#000000"))
            binding.health.setTextColor(Color.parseColor("#FFFFFF"))
            binding.salesIn.setTextColor(Color.parseColor("#000000"))

            binding.overViewTab.visibility = View.GONE
            binding.healthTab.visibility = View.VISIBLE
            binding.marketValueTab.visibility = View.GONE
        }

        binding.salesIn.setOnClickListener {

            binding.salesIn.backgroundTintList = ColorStateList.valueOf(tint)
            binding.overview.backgroundTintList = noTint
            binding.health.backgroundTintList = noTint

            binding.overview.setTextColor(Color.parseColor("#000000"))
            binding.health.setTextColor(Color.parseColor("#000000"))
            binding.salesIn.setTextColor(Color.parseColor("#FFFFFF"))

            binding.overViewTab.visibility = View.GONE
            binding.healthTab.visibility = View.GONE
            binding.marketValueTab.visibility = View.VISIBLE
        }

    }

    private fun startScanner() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan Pig QR Code")
        integrator.setCameraId(0)
        integrator.setOrientationLocked(true)
        integrator.captureActivity = CustomScannerActivity::class.java
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val rawContent = result?.contents?.trim()

        if (!rawContent.isNullOrEmpty()) {
            Log.d("ScanPigFragment", "Raw QR content: $rawContent")
            fetchPigDetails(rawContent) // Always trigger API
        } else {
            Toast.makeText(requireContext(), "Scan canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPigDetails(pigId: String) {
        val api = FetchPigsByIdRI.getInstance(TokenManager.getToken(requireContext()))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pig: PigsModel = api.fetchPigsById(pigId)

                withContext(Dispatchers.Main) {

                    currentPig = pig
                    binding.markAsSold.isEnabled = pig.isSold != true
                    binding.markAsSold.alpha = if (pig.isSold == true) 0.5f else 1f

                    // Populate UI
                    binding.name.text = pig.name
                    binding.tvBreed.text = pig.breed ?: "N/A"
                    binding.tvGender.text = pig.gender ?: "N/A"
                    binding.tvAge.text = pig.age?.let { "$it months" } ?: "N/A"
                    binding.tvStatus.text = pig.status ?: "N/A"
                    binding.tvOrigin.text = pig.origin ?: "N/A"
                    binding.tvCageName.text = pig.cageName ?: "N/A"
                    binding.tvWeight.text = pig.weight ?: "N/A"
                    binding.tvIllness.text = pig.illness ?: "None"
                    binding.tvVaccine.text = pig.vaccine ?: "N/A"
                    binding.tvHealthStatus.text = pig.healthStatus ?: "N/A"
                    binding.tvLastCheckup.text = formatDateWithoutHours(pig.lastCheckup) ?: "N/A"
                    binding.tvIsAlive.text = if (pig.isAlive == true) "Yes" else "No"
                    binding.tvPrice.text = pig.price?.let { "₱$it" } ?: "N/A"
                    binding.tvIsSold.text = if (pig.isSold == true) "Yes" else "No"
                    binding.tvBuyerName.text = pig.buyerName ?: "N/A"

                    binding.tvPigType.text = pig.pigType ?: "N/A"
                    binding.tvFeed.text = pig.feed ?: "N/A"

                    binding.tvVaccine.text = pig.vaccine ?: "N/A"
                    binding.tvVaccineDate.text = formatDateWithoutHours(pig.vaccineDate) ?: "N/A"
                    binding.tvVaccineNextDue.text = formatDateWithoutHours(pig.vaccineNextDue) ?: "N/A"
                    binding.tvBirthDate.text = formatDateWithoutHours(pig.birthDate) ?: "N/A"


                    pig.image_url?.let { url ->
                        val fullUrl = if (url.startsWith("http")) url else "${FetchPigsByIdRI.BASE_URL}$url"
                        Glide.with(requireContext())
                            .load(fullUrl)
                            .placeholder(R.drawable.pig)
                            .error(R.drawable.pig)
                            .into(binding.imagePlaceHolder)
                    }
                }
            } catch (e: Exception) {
                Log.e("ScanPigFragment", "Failed to fetch pig details", e)
                withContext(Dispatchers.Main) {
                    // Backend can respond with 404 if old pig
                    Toast.makeText(requireContext(),
                        "Pig not found or QR is invalid. Please scan a valid QR code.",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
        super.onDestroyView()
        _binding = null
    }
}
