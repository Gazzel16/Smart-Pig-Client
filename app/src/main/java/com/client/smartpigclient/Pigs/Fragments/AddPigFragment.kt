package com.client.smartpigclient.Pigs.Fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.client.smartpigclient.Utils.OtherInputDialog
import com.client.smartpigclient.Utils.TokenManager
import com.client.smartpigclient.databinding.FragmentAddPigBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import com.client.smartpigclient.Utils.PigConstants


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
            showDatePicker {
                date -> binding.birthDate.setText(date)

                println("Selected birthDate: $date")

                val age = calculateAgeFromBirthDate(date)

                println("Calculated age: $age")
                binding.age.setText(age?.toString() ?: "")

            }
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

        binding.feed.setOnItemClickListener { parent, view, position, id ->
            val selectedFeed = PigConstants.PIG_FEEDS[position]
            binding.feed.setText(selectedFeed, false) // important: false prevents filtering again

            if (selectedFeed == "Other") {
                OtherInputDialog.show(
                    context = requireContext(),
                    title = "Other Feed",
                    hint = "Enter feed name",
                    targetView = binding.feed
                )
            }
        }


        binding.illness.setOnItemClickListener { _, _, position, _ ->
            val selectedIllness = PigConstants.ILLNESS_OPTIONS[position]
            binding.illness.setText(selectedIllness, false)

            if (selectedIllness == "Other") {
                OtherInputDialog.show(
                    context = requireContext(),
                    title = "Other Illness",
                    hint = "Enter Illness name",
                    targetView = binding.illness
                )
            }
        }

        binding.vaccine.setOnItemClickListener { _, _, position, _ ->
            val selectedVaccine = PigConstants.VACCINE_OPTIONS[position]
            binding.vaccine.setText(selectedVaccine, false)

            if (selectedVaccine == "Other") {
                OtherInputDialog.show(
                    context = requireContext(),
                    title = "Other Vaccine",
                    hint = "Enter Vaccine name",
                    targetView = binding.vaccine
                )
            }
        }

        binding.pigType.setOnItemClickListener { _, _, position, _ ->
            val selectedPigType = PigConstants.PIG_TYPE[position]
            binding.vaccine.setText(selectedPigType, false)

            if (selectedPigType == "Other") {
                OtherInputDialog.show(
                    context = requireContext(),
                    title = "Other Vaccine",
                    hint = "Enter Vaccine name",
                    targetView = binding.vaccine
                )
            }
        }



        dropDownDetails()
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun dropDownDetails(){
        //ADAPTER LIST
        val vaccineAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PigConstants.VACCINE_OPTIONS
        )

        val illnessAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PigConstants.ILLNESS_OPTIONS
        )

        val healthAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PigConstants.HEALTH_OPTIONS
        )

        val isAliveAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PigConstants.IS_ALIVE_OPTIONS
        )

        val originAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PigConstants.ORIGIN_OPTIONS
        )
        val adapterGender = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PigConstants.GENDER_OPTIONS
        )

        val adapterFeed = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PigConstants.PIG_FEEDS
        )

        val adapterPigType = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PigConstants.PIG_TYPE
        )

        //CALL THE COMPONENTS
        binding.gender.setAdapter(adapterGender)
        binding.origin.setAdapter(originAdapter)
        binding.isAlive.setAdapter(isAliveAdapter)
        binding.healthStatus.setAdapter(healthAdapter)
        binding.illness.setAdapter(illnessAdapter)
        binding.vaccine.setAdapter(vaccineAdapter)
        binding.feed.setAdapter(adapterFeed)
        binding.pigType.setAdapter(adapterPigType)
    }

    private fun calculateAgeFromBirthDate(birthDate: String): Int? {
        return try {
            // Parse birthDate "yyyy-MM-dd"
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd")
            val birth = sdf.parse(birthDate)

            val today = java.util.Calendar.getInstance()
            val dob = java.util.Calendar.getInstance()
            dob.time = birth!!

            var age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR)

            // Compare month and day instead of DAY_OF_YEAR
            val todayMonth = today.get(java.util.Calendar.MONTH)
            val todayDay = today.get(java.util.Calendar.DAY_OF_MONTH)
            val birthMonth = dob.get(java.util.Calendar.MONTH)
            val birthDay = dob.get(java.util.Calendar.DAY_OF_MONTH)

            if (todayMonth < birthMonth || (todayMonth == birthMonth && todayDay < birthDay)) {
                age--
            }

            age
        } catch (e: Exception) {
            null
        }
    }


    private fun addPig() {
        val name = binding.name.text.toString().toRequestBody()
        val breed = binding.breed.text.toString().toRequestBodyOptional()
//        val age = binding.age.text.toString().toRequestBodyOptional()
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
        val pigType = binding.pigType.text.toString().toRequestBodyOptional()

        val feedText = binding.feed.text?.toString()?.trim()
        val feed = if (feedText.isNullOrEmpty()) null else feedText.toRequestBody()



        val imagePart = imageFile?.let { file ->
            val reqFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("image", file.name, reqFile)
        }

        val api = AddPigsRI.getInstance(TokenManager.getToken(requireContext()))
        val cageIdBody = cageId?.toRequestBodyOptional()

        binding.addPig.isEnabled = false
        binding.addPig.text = "Adding pig...."

        lifecycleScope.launch {
            try {
                val response = api.addPigs(
                    name = name,
                    breed = breed,
                    pigType = pigType,
//                    age = age,
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
                    weight = weight,
                    feed = feed
                )

                if (response.isSuccessful) {

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PigFragment())
                        .addToBackStack(null)
                        .commit()

                    Toast.makeText(requireContext(), "Pig added successfully!", Toast.LENGTH_SHORT).show()
                    val addedPig: PigRequestModel? = response.body()

                    clearForm()

                    println("Added pig: $addedPig")
                } else {

                    binding.addPig.isEnabled = true
                    binding.addPig.text = "Add pig"

                    Toast.makeText(requireContext(), "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {

                binding.addPig.isEnabled = true
                binding.addPig.text = "Add pig"

                Log.e("Add Pig", "Error: ${e.message}")
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
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
