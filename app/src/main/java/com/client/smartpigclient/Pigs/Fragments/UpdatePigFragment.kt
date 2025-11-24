package com.client.smartpigclient.Pigs.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.client.smartpigclient.Pigs.Api.UpdatePigsRI
import com.client.smartpigclient.Pigs.Model.PigsModel
import com.client.smartpigclient.Pigs.Model.PigRequestModel
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.FragmentUpdatePigBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UpdatePigFragment() : Fragment() {

    private var _binding: FragmentUpdatePigBinding? = null
    private val binding get() = _binding!!

    private var imageFile: File? = null
    private var pig: PigsModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pig = it.getParcelable(ARG_PIG)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUpdatePigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-fill data
        binding.name.setText(pig?.name)
        binding.breed.setText(pig?.breed)
        binding.age.setText(pig?.age?.toString() ?: "")
        binding.price.setText(pig?.price?.toString() ?: "")
        binding.weight.setText(pig?.weight)
        binding.cageName.text = "Cage: ${pig?.cageName ?: "Unknown"}"
        binding.birthDate.setText(pig?.birthDate)
        binding.vaccineDate.setText(pig?.vaccineDate?.substring(0,10) ?: "")
        binding.vaccineNextDue.setText(pig?.vaccineNextDue?.substring(0,10) ?: "")
        binding.lastCheckup.setText(pig?.lastCheckup?.substring(0,10) ?: "")

        // Dropdowns
        setupDropdown(binding.gender, listOf("Male", "Female"), pig?.gender)
        setupDropdown(binding.origin, listOf("Born on Farm", "Purchased from Market", "Transferred from Another Farm", "Gifted"), pig?.origin)
        setupDropdown(binding.isAlive, listOf("Alive", "Dead"), if (pig?.isAlive == true) "Alive" else "Dead")
        setupDropdown(binding.healthStatus, listOf("Healthy", "Sick", "Injured", "Recovering", "Needs Checkup", "Vaccinated"), pig?.healthStatus)
        setupDropdown(binding.illness, listOf("None", "Swine Flu", "Foot and Mouth Disease", "Skin Infection", "Respiratory Infection", "Other"), pig?.illness)
        setupDropdown(binding.vaccine, listOf("None", "Swine Fever Vaccine", "Foot and Mouth Disease Vaccine", "PRRS Vaccine", "Classical Swine Fever Vaccine", "Other"), pig?.vaccine)

        // Image placeholder
        pig?.image_url?.let {
            // Load with Glide or Coil
            // Glide.with(this).load(it).into(binding.imagePlaceHolder)
        }

        // Date pickers
        binding.birthDate.setOnClickListener { showDatePicker { date -> binding.birthDate.setText(date) } }
        binding.vaccineDate.setOnClickListener { showDatePicker { date -> binding.vaccineDate.setText(date) } }
        binding.vaccineNextDue.setOnClickListener { showDatePicker { date -> binding.vaccineNextDue.setText(date) } }
        binding.lastCheckup.setOnClickListener { showDatePicker { date -> binding.lastCheckup.setText(date) } }

        // Image picker
        binding.addImage.setOnClickListener { pickImage() }

        binding.updatePig.setOnClickListener { updatePig() }
    }

    private fun setupDropdown(autoComplete: android.widget.AutoCompleteTextView, options: List<String>, preselect: String?) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
        autoComplete.setAdapter(adapter)
        preselect?.let { autoComplete.setText(it, false) }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == android.app.Activity.RESULT_OK && requestCode == 1001) {
            val uri: Uri? = data?.data
            uri?.let {
                binding.imagePlaceHolder.setImageURI(it)
                binding.imagePlaceHolder.visibility = View.VISIBLE
                binding.addImageHolder.visibility = View.GONE
                imageFile = uriToFile(it)
            }
        }
    }

    private fun updatePig() {
        val api = UpdatePigsRI.getInstance()

        val name = binding.name.text.toString().toRequestBody()
        val breed = binding.breed.text.toString().toRequestBodyOptional()
        val age = binding.age.text.toString().toRequestBodyOptional()
        val price = binding.price.text.toString().toRequestBodyOptional()
        val weight = binding.weight.text.toString().toRequestBodyOptional()
        val gender = binding.gender.text.toString().toRequestBodyOptional()
        val isAlive = when(binding.isAlive.text.toString()) {
            "Alive" -> "true"
            "Dead" -> "false"
            else -> null
        }?.toRequestBodyOptional()
        val healthStatus = binding.healthStatus.text.toString().toRequestBodyOptional()
        val origin = binding.origin.text.toString().toRequestBodyOptional()
        val illness = binding.illness.text.toString().toRequestBodyOptional()
        val vaccine = binding.vaccine.text.toString().toRequestBodyOptional()
        val birthDate = binding.birthDate.text.toString().toRequestBodyOptional()
        val vaccineDate = binding.vaccineDate.text.toString().toRequestBodyOptional()
        val vaccineNextDue = binding.vaccineNextDue.text.toString().toRequestBodyOptional()
        val lastCheckup = binding.lastCheckup.text.toString().toRequestBodyOptional()

        val imagePart = imageFile?.let { file ->
            val reqFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("image", file.name, reqFile)
        }

        val pigId = pig?.id ?: run {
            Toast.makeText(requireContext(), "Pig data is missing", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = api.updatePigs(
                    pigId,
                    name, breed, age, price, illness, vaccine,
                    vaccineDate, vaccineNextDue, healthStatus, lastCheckup,
                    isAlive, origin, null, null, null,
                    gender, null, birthDate, weight, imagePart
                )

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Pig updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun String.toRequestBody(): RequestBody =
        RequestBody.create("text/plain".toMediaTypeOrNull(), this)

    private fun String.toRequestBodyOptional(): RequestBody? =
        if (this.isEmpty()) null else toRequestBody()

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = java.util.Calendar.getInstance()
        val datePickerDialog = android.app.DatePickerDialog(requireContext(),
            { _, year, month, day ->
                onDateSelected(String.format("%04d-%02d-%02d", year, month + 1, day))
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    companion object {
        private const val ARG_PIG = "pig"

        fun newInstance(pig: PigsModel): UpdatePigFragment {
            val fragment = UpdatePigFragment()
            val bundle = Bundle()
            bundle.putParcelable(ARG_PIG, pig) // PigsModel must implement Parcelable
            fragment.arguments = bundle
            return fragment
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
