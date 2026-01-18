package com.client.smartpigclient.Pigs.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.client.smartpigclient.Authentication.Fragments.SignupFragment
import com.client.smartpigclient.Pigs.Api.UpdatePigsRI
import com.client.smartpigclient.Pigs.Model.PigsModel
import com.client.smartpigclient.Pigs.Model.PigRequestModel
import com.client.smartpigclient.R
import com.client.smartpigclient.Utils.OtherInputDialog
import com.client.smartpigclient.Utils.PigConstants
import com.client.smartpigclient.Utils.TokenManager
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
    private var cageId: String? = null

    private var selectedFeed: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cageId = it.getString(ARG_CAGE_ID)
            pig = it.getParcelable(ARG_PIG)
        }
    }

    companion object {
        private const val ARG_PIG = "pig"
        private const val ARG_CAGE_ID = "cage_id"

        fun newInstance(pig: PigsModel, cageId: String): UpdatePigFragment {
            val fragment = UpdatePigFragment()
            val bundle = Bundle()
            bundle.putParcelable(ARG_PIG, pig) // PigsModel must implement Parcelable
            bundle.putString(ARG_CAGE_ID, cageId)
            fragment.arguments = bundle
            return fragment
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

        // Image placeholder
        pig?.image_url?.let {
            // Load with Glide or Coil
            // Glide.with(this).load(it).into(binding.imagePlaceHolder)
        }

        // Date pickers
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
            showDatePicker {
                date -> binding.vaccineDate.setText(date)
            }
        }
        binding.vaccineNextDue.setOnClickListener {
            showDatePicker {
                date -> binding.vaccineNextDue.setText(date)
            }
        }
        binding.lastCheckup.setOnClickListener {
            showDatePicker {
                date -> binding.lastCheckup.setText(date)
            }
        }

        // Image picker
        binding.addImage.setOnClickListener {
            pickImage()
        }

        binding.updatePig.setOnClickListener {
            updatePig()
        }

        binding.feed.setOnItemClickListener { parent, view, position, id ->
            val selectedFeed = PigConstants.PIG_FEEDS[position]

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

            if (selectedPigType == "Other") {
                OtherInputDialog.show(
                    context = requireContext(),
                    title = "Other PigType",
                    hint = "Enter Vaccine name",
                    targetView = binding.pigType
                )
            }
        }


        dropDownDetails()
        fetchDetails()
    }

    private fun fetchDetails (){
        // Pre-fill data
        binding.name.setText(pig?.name)
        binding.breed.setText(pig?.breed)
        binding.pigType.setText(pig?.pigType, false)
        binding.feed.setText(pig?.feed, false)
        binding.illness.setText(pig?.illness, false)
        binding.vaccine.setText(pig?.vaccine, false)

        binding.isAlive.setText(
            when (pig?.isAlive) {
                true -> "Alive"
                false -> "Dead"
                null -> "" // or "Unknown"
            },
            false
        )

        binding.vaccine.setText(pig?.vaccine)
        binding.origin.setText(pig?.origin, false)
        binding.gender.setText(pig?.gender, false)
        binding.age.setText(pig?.age?.toString() ?: "")
        binding.price.setText(pig?.price?.toString() ?: "")
        binding.weight.setText(pig?.weight)
        binding.cageName.text = "Cage: ${pig?.cageName ?: "Unknown"}"
        binding.birthDate.setText(pig?.birthDate)
        binding.vaccineDate.setText(pig?.vaccineDate?.substring(0,10) ?: "")
        binding.vaccineNextDue.setText(pig?.vaccineNextDue?.substring(0,10) ?: "")
        binding.lastCheckup.setText(pig?.lastCheckup?.substring(0,10) ?: "")

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

    private fun updatePig() {
        val api = UpdatePigsRI.getInstance(TokenManager.getToken(requireContext()))

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

        val feedText = binding.feed.text?.toString()?.trim()
        val feed = if (feedText.isNullOrEmpty()) null else feedText.toRequestBody()

        val pigType = binding.pigType.text.toString().toRequestBodyOptional()




        val imagePart = imageFile?.let { file ->
            val reqFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("image", file.name, reqFile)
        }

        val pigId = pig?.id ?: run {
            Toast.makeText(requireContext(), "Pig data is missing", Toast.LENGTH_SHORT).show()
            return
        }

        binding.updatePig.isEnabled = false
        binding.updatePig.text = "Updating pig details..."

        val cageIdValue = pig?.cageId ?: cageId ?: ""

        lifecycleScope.launch {
            try {
                val response = api.updatePigs(
                    pigId,
                    name,
                    breed,
                    pigType,
                     age,
                    price,
                    illness,
                    vaccine,
                    vaccineDate,
                    vaccineNextDue,
                    healthStatus,
                    lastCheckup,
                    isAlive,
                    origin,
                    null,  // buyerName
                    null,  // isSold
                    cageIdValue.toRequestBodyOptional(),  // cageId
                    gender,
                    null,  // status
                    birthDate,
                    weight,
                    feed,
                    imagePart // <- passed by position
                )



                if (response.isSuccessful) {

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PigFragment())
                        .addToBackStack(null)
                        .commit()

                    binding.updatePig.isEnabled = true
                    binding.updatePig.text = "Update Pig"

                    Toast.makeText(requireContext(), "Pig updated successfully!", Toast.LENGTH_SHORT).show()
                } else {

                    binding.updatePig.isEnabled = true
                    binding.updatePig.text = "Update Pig"

                    Toast.makeText(requireContext(), "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {

                binding.updatePig.isEnabled = true
                binding.updatePig.text = "Update Pig"

                Log.e("Update Pig", "Error: ${e.message}")
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
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
