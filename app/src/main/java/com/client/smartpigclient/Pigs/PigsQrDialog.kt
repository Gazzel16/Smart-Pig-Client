package com.client.smartpigclient.Pigs

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.client.smartpigclient.Pigs.Api.FetchPigsByIdRI
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.FragmentQrDialogBinding

class PigsQrDialog : DialogFragment() {

    private var _binding: FragmentQrDialogBinding? = null
    private val binding get() = _binding!!

    private var qrUrl: String? = null
    private var pigId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            qrUrl = it.getString(ARG_QR_URL)
            pigId = it.getString(ARG_PIG_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val correctedQr = qrUrl?.replace("\\", "/") ?: ""
        val fullQrUrl = if (correctedQr.startsWith("http")) correctedQr
        else "${FetchPigsByIdRI.BASE_URL}${correctedQr.removePrefix("/")}"

        // ---- Load QR image ----
        Glide.with(requireContext())
            .load(fullQrUrl)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.qrImageView)

        // ---- Download button ----
        binding.downloadButton.setOnClickListener {
            saveQrToGallery()
        }
    }

    private fun saveQrToGallery() {
        val drawable = binding.qrImageView.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            try {
                val filename = "qr_code_${System.currentTimeMillis()}.png"
                val resolver = requireContext().contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
                }

                val imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageUri?.let {
                    resolver.openOutputStream(it)?.use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                    Toast.makeText(requireContext(), "Saved to Gallery", Toast.LENGTH_LONG).show()
                } ?: Toast.makeText(requireContext(), "Failed to create file in Gallery", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to save QR", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "QR not loaded yet", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_QR_URL = "qr_url"
        private const val ARG_PIG_ID = "pig_id"

        @JvmStatic
        fun newInstance(qrUrl: String, pigId: String) =
            PigsQrDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_QR_URL, qrUrl)
                    putString(ARG_PIG_ID, pigId)
                }
            }
    }
}
