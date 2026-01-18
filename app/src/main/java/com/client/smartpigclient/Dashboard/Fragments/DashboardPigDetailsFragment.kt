package com.client.smartpigclient.Dashboard.Fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.client.smartpigclient.Pigs.Api.FetchPigsByIdRI
import com.client.smartpigclient.Pigs.Model.PigsModel
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.FragmentDashboardPigDetailsBinding
import android.content.Context
import com.client.smartpigclient.Utils.formatDate
import com.client.smartpigclient.Utils.formatDateWithoutHours


class DashboardPigDetailsFragment : Fragment() {

    private var _binding: FragmentDashboardPigDetailsBinding? = null
    private val binding get() = _binding!! // Only valid between onCreateView and onDestroyView

    private var pig: PigsModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the pig from arguments
        pig = arguments?.getParcelable("selected_pig")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDashboardPigDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", "") ?: ""

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

        pigDetails()
    }

    private fun pigDetails(){

        pig?.let { pig ->
            // Header
            binding.name.text = pig.name
            binding.birthDate.text = formatDateWithoutHours(pig.birthDate) ?: "N/A" // You can change this field

            // Overview Tab
            binding.tvBreed.text = pig.breed ?: "N/A"
            binding.tvGender.text = pig.gender ?: "N/A"
            binding.tvAge.text = pig.age?.toString() ?: "N/A"
            binding.tvStatus.text = pig.status ?: "N/A"
            binding.tvOrigin.text = pig.origin ?: "N/A"
            binding.tvCageName.text = pig.cageName ?: "N/A"
            binding.tvWeight.text = pig.weight ?: "N/A"

            // Health Tab
            binding.tvIllness.text = pig.illness ?: "N/A"
            binding.tvVaccine.text = pig.vaccine ?: "N/A"
            binding.tvVaccineDate.text = formatDateWithoutHours(pig.vaccineDate)
            binding.tvVaccineNextDue.text = formatDateWithoutHours(pig.vaccineNextDue)
            binding.tvHealthStatus.text = pig.healthStatus ?: "N/A"
            binding.tvLastCheckup.text = formatDateWithoutHours(pig.lastCheckup )
            binding.tvIsAlive.text = if (pig.isAlive == true) "Yes" else "No"

            // Market/Sales Tab
            binding.tvPrice.text = pig.price?.let { "₱$it" } ?: "N/A"
            binding.tvIsSold.text = if (pig.isSold == true) "Yes" else "No"
            binding.tvBuyerName.text = pig.buyerName ?: "N/A"

            binding.tvPigType.text = pig.pigType ?: "N/A"
            binding.tvFeed.text = pig.feed ?: "N/A"

            pig.image_url?.let { url ->
                val fullUrl = if (url.startsWith("http")) url else "${FetchPigsByIdRI.BASE_URL}$url"
                Glide.with(requireContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.pig)
                    .error(R.drawable.pig)
                    .into(binding.imagePlaceHolder)
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

    companion object {
        @JvmStatic
        fun newInstance(pig: PigsModel) =
            DashboardPigDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("selected_pig", pig)
                }
            }
    }
}
