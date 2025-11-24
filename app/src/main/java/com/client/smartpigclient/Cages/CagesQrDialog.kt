package com.client.smartpigclient.Cages

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

import com.client.smartpigclient.databinding.FragmentQrDialogBinding
import java.io.File
import java.io.FileOutputStream

private const val ARG_QR_URL = "qr_url"

class CagesQrDialog : DialogFragment() {

    private var _binding: FragmentQrDialogBinding? = null
    private val binding get() = _binding!!

    private var qrUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            qrUrl = it.getString(ARG_QR_URL)
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

        // Load QR code
        qrUrl?.let {
            Glide.with(requireContext())
                .load(it)
                .placeholder(com.client.smartpigclient.R.drawable.qr_ic)
                .into(binding.qrImageView)
        }

        // Download button click
        binding.downloadButton.setOnClickListener {
            saveQrToGallery()
        }
    }

    private fun saveQrToGallery() {
        val drawable = binding.qrImageView.drawable
        if (drawable != null && drawable is BitmapDrawable) {
            val bitmap: Bitmap = drawable.bitmap
            try {
                val filename = "qr_code_${System.currentTimeMillis()}.png"
                val resolver = requireContext().contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
                }

                val imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    resolver.openOutputStream(imageUri)?.use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                    Toast.makeText(requireContext(), "Saved to Gallery", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to create file in Gallery", Toast.LENGTH_SHORT).show()
                }
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
        @JvmStatic
        fun newInstance(qrUrl: String) =
            CagesQrDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_QR_URL, qrUrl)
                }
            }
    }
}
