package com.client.smartpigclient.Cages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.client.smartpigclient.Cages.Api.AddCageRI
import com.client.smartpigclient.Cages.Fragments.CagesFragment
import com.client.smartpigclient.Cages.Model.CageModel
import com.client.smartpigclient.Cages.Model.CageRequest
import com.client.smartpigclient.databinding.FragmentCagesDialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CagesDialog(private val listener: OnCageAddedListener) : DialogFragment() {


    private var _binding: FragmentCagesDialogBinding? = null
    private val binding get() = _binding!!

    interface OnCageAddedListener {
        fun onCageAdded()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCagesDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.addCageBtn.setOnClickListener {
            val cageName = binding.cageInput.text.toString().trim()
            if (cageName.isNotEmpty()) {
                addCage(cageName)
            } else {
                Toast.makeText(requireContext(), "Enter cage name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addCage(name: String) {
        val api = AddCageRI.getInstance()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = CageRequest(name)
                val response: CageModel = api.addCage(request)

                withContext(Dispatchers.Main) {
                    listener.onCageAdded()
                    Toast.makeText(requireContext(), "Cage added: ${response.name}", Toast.LENGTH_SHORT).show()
                    binding.cageInput.text.clear()
                    dismiss() // close dialog only after success
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
