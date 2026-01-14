package com.client.smartpigclient.Pigs.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.client.smartpigclient.Pigs.Api.FetchPigsByIdRI
import com.client.smartpigclient.Pigs.Api.PigBuyerNameRI
import com.client.smartpigclient.Pigs.Api.UpdatePigsRI
import com.client.smartpigclient.Pigs.Model.PigBuyerNameRequest
import com.client.smartpigclient.Pigs.Model.PigsModel
import com.client.smartpigclient.Utils.TokenManager
import com.client.smartpigclient.databinding.FragmentBottomModalBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class BottomModalFragment(
    private val pig: PigsModel,
    private val listener: BottomModalListener
) : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomModalBinding? = null
    private val binding get() = _binding!!

    // Helper to convert string to RequestBody
    private fun String.toPlainRequestBody(): RequestBody =
        this.toRequestBody("text/plain".toMediaTypeOrNull())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomModalBinding.inflate(inflater, container, false)

        // Pre-fill buyer name if already exists
        binding.editTextText.setText(pig.buyerName ?: "")

        binding.markAsSold.setOnClickListener {
            val buyerName = binding.editTextText.text.toString().trim()
            if (buyerName.isEmpty()) {
                binding.editTextText.error = "Enter buyer name"
                return@setOnClickListener
            }

            binding.markAsSold.isEnabled = false
            binding.markAsSold.text = "Marking..."

            // Call API in coroutine
            lifecycleScope.launch {
                val success = markPigAsSoldApiCall(pig, buyerName)
                withContext(Dispatchers.Main) {
                    binding.markAsSold.isEnabled = true
                    binding.markAsSold.text = "Mark as Sold"

                    if (success) {
                        Toast.makeText(requireContext(), "Pig marked as sold", Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update pig", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return binding.root
    }

    private suspend fun markPigAsSoldApiCall(
        pig: PigsModel,
        buyerName: String,
        isSold: Boolean = true
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val api = PigBuyerNameRI.getInstance(TokenManager.getToken(requireContext())) // PATCH endpoint
                val response = api.pigBuyerName(
                    pig.id,
                    PigBuyerNameRequest(buyerName = buyerName, isSold = isSold) // include isSold
                )

                if (response.isSuccessful) {
                    pig.isSold = isSold
                    pig.buyerName = buyerName

                    withContext(Dispatchers.Main) {
                        listener.onMarkAsSold(pig)
                    }
                }

                response.isSuccessful
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Callback to notify parent
interface BottomModalListener {
    fun onMarkAsSold(updatedPig: PigsModel)
}
