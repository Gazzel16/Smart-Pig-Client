package com.client.smartpigclient.Pigs.Fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.client.smartpigclient.MainActivity
import com.client.smartpigclient.Pigs.Api.AddPigsRI
import com.client.smartpigclient.Pigs.Model.PigRequestModel
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.FragmentAddPigBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class AddPigFragment : Fragment() {

    private var _binding: FragmentAddPigBinding? = null
    private val binding get() = _binding!!

    private var imageFile: File? = null
    private var cageId: String? = null
    private var cageName: String? = null

    // Request code for picking image
    private val pickImageLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imagePlaceHolder.setImageURI(it)
                binding.imagePlaceHolder.visibility = View.VISIBLE
                binding.addImageHolder.visibility = View.GONE
                // Convert URI to File
                imageFile = uriToFile(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cageId = it.getString(ARG_CAGE_ID)
            cageName = it.getString(ARG_CAGE_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cageName.setText("Cage: ${cageName}" ?: "Unknown Cage")

        binding.addPig.setOnClickListener {
            addPig()
        }

        binding.addImage.setOnClickListener {
            pickImage()
        }

        binding.birthDate.setOnClickListener {
            showDatePicker { date -> binding.birthDate.setText(date) }
        }

        binding.vaccineDate.setOnClickListener {
            showDatePicker { date -> binding.vaccineDate.setText(date) }
        }

        binding.vaccineNextDue.setOnClickListener {
            showDatePicker { date -> binding.vaccineNextDue.setText(date) }
        }

        binding.lastCheckup.setOnClickListener {
            showDatePicker { date -> binding.lastCheckup.setText(date) }
        }

        dropDownDetails()
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun dropDownDetails(){

        //LIST
        val genderOptions = listOf("Male", "Female")
        val originOptions = listOf(
            "Born on Farm",
            "Purchased from Market",
            "Transferred from Another Farm",
            "Gifted",
        )
        val isAliveOptions = listOf("True", "False")  // or "Yes" / "No"
        val healthOptions = listOf(
            "Healthy",
            "Sick",
            "Injured",
            "Recovering",
            "Needs Checkup",
            "Vaccinated"
        )
        val illnessOptions = listOf(
            "None",
            "Swine Flu",
            "Foot and Mouth Disease",
            "Skin Infection",
            "Respiratory Infection",
            "Other"
        )
        val vaccineOptions = listOf(
            "None",
            "Swine Fever Vaccine",
            "Foot and Mouth Disease Vaccine",
            "PRRS Vaccine",
            "Classical Swine Fever Vaccine",
            "Other"
        )

        //ADAPTER LIST
        val vaccineAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            vaccineOptions
        )

        val illnessAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            illnessOptions
        )

        val healthAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            healthOptions
        )

        val isAliveAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            isAliveOptions
        )

        val originAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            originOptions
        )
        val adapterGender = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            genderOptions
        )

        //CALL THE COMPONENTS
        binding.gender.setAdapter(adapterGender)
        binding.origin.setAdapter(originAdapter)
        binding.isAlive.setAdapter(isAliveAdapter)
        binding.healthStatus.setAdapter(healthAdapter)
        binding.illness.setAdapter(illnessAdapter)
        binding.vaccine.setAdapter(vaccineAdapter)
    }
    private fun addPig() {
        val name = binding.name.text.toString().toRequestBody()
        val breed = binding.breed.text.toString().toRequestBodyOptional()
        val age = binding.age.text.toString().toRequestBodyOptional()
        val price = binding.price.text.toString().toRequestBodyOptional()
        val illness = binding.illness.text.toString().toRequestBodyOptional()
        val vaccine = binding.vaccine.text.toString().toRequestBodyOptional()
        val vaccineDate = binding.vaccineDate.text.toString().toRequestBodyOptional()
        val vaccineNextDue = binding.vaccineNextDue.text.toString().toRequestBodyOptional()
        val healthStatus = binding.healthStatus.text.toString().toRequestBodyOptional()
        val lastCheckup = binding.lastCheckup.text.toString().toRequestBodyOptional()
        val isAlive = binding.isAlive.text.toString().toRequestBodyOptional()
        val origin = binding.origin.text.toString().toRequestBodyOptional()
        val gender = binding.gender.text.toString().toRequestBodyOptional()
        val weight = binding.weight.text.toString().toRequestBodyOptional()
        val birthDate = binding.birthDate.text.toString().toRequestBodyOptional()


        val imagePart = imageFile?.let { file ->
            val reqFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("image", file.name, reqFile)
        }

        val api = AddPigsRI.getInstance()
        val cageIdBody = cageId?.toRequestBodyOptional()
        lifecycleScope.launch {
            try {
                val response = api.addPigs(
                    name = name,
                    breed = breed,
                    age = age,
                    price = price,
                    illness = illness,
                    vaccine = vaccine,
                    vaccineDate = vaccineDate,
                    vaccineNextDue = vaccineNextDue,
                    healthStatus = healthStatus,
                    lastCheckup = lastCheckup,
                    isAlive = isAlive,
                    origin = origin,
                    gender = gender,
                    cageId = cageIdBody,
                    image = imagePart,
                    birthDate = birthDate,
                    weight = weight
                )

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Pig added successfully!", Toast.LENGTH_SHORT).show()
                    val addedPig: PigRequestModel? = response.body()

                    clearForm()

                    println("Added pig: $addedPig")
                } else {
                    Toast.makeText(requireContext(), "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun clearForm() {
        binding.name.setText("")
        binding.breed.setText("")
        binding.age.setText("")
        binding.price.setText("")
        binding.illness.setText("")
        binding.vaccine.setText("")
        binding.vaccineDate.setText("")
        binding.vaccineNextDue.setText("")
        binding.healthStatus.setText("")
        binding.lastCheckup.setText("")
        binding.isAlive.setText("")
        binding.origin.setText("")
        binding.gender.setText("")
        binding.weight.setText("")
        binding.birthDate.setText("")

        // Reset image
        binding.imagePlaceHolder.setImageDrawable(null)
        binding.imagePlaceHolder.visibility = View.GONE
        binding.addImageHolder.visibility = View.VISIBLE
        imageFile = null
    }
    private fun String.toRequestBody(): RequestBody =
        RequestBody.create("text/plain".toMediaTypeOrNull(), this)

    private fun String.toRequestBodyOptional(): RequestBody? =
        if (this.isEmpty()) null else toRequestBody()

    // Convert URI to File
    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format date as YYYY-MM-DD
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(formattedDate)
            }, year, month, day
        )
        datePickerDialog.show()
    }

    companion object {
        private const val ARG_CAGE_ID = "cage_id"
        private const val ARG_CAGE_NAME = "cage_name"

        @JvmStatic
        fun newInstance(cageId: String, cageName: String) =
            AddPigFragment().apply {
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
