package com.client.smartpigclient.Dashboard.Fragments

import android.R.attr.data
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.client.smartpigclient.Cages.Api.FetchCageRI
import com.client.smartpigclient.Dashboard.Api.FetchAllPigsRI
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.FragmentDashboardAnalyticsBinding
import kotlinx.coroutines.launch


class DashboardAnalyticsFragment : Fragment() {

    private var _binding: FragmentDashboardAnalyticsBinding? = null
    private val binding get() = _binding!!  // Only use between onCreateView and onDestroyView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDashboardAnalyticsBinding.inflate(inflater, container, false)

        pigsAndCagesChart()
        healthPigsAnalytics()
        return binding.root
    }

    private fun pigsAndCagesChart() {
        val apiPigs = FetchAllPigsRI.getInstance()
        val apiCage = FetchCageRI.getInstance() // assuming this returns a list of cages

        lifecycleScope.launch {
            try {
                // Fetch data
                val allPigs = apiPigs.fetchAllPigs()
                val allCages = apiCage.fetchCage() // implement fetchAllCages() in your API

                // Analytics
                val totalPigs = allPigs.size
                val totalCages = allCages.size
                val malePigs = allPigs.count { it.gender == "Male" }
                val femalePigs = allPigs.count { it.gender == "Female" }

                // Prepare chart entries
                val entries = arrayListOf<com.github.mikephil.charting.data.BarEntry>()
                entries.add(com.github.mikephil.charting.data.BarEntry(0f, totalPigs.toFloat()))
                entries.add(com.github.mikephil.charting.data.BarEntry(1f, totalCages.toFloat()))
                entries.add(com.github.mikephil.charting.data.BarEntry(2f, malePigs.toFloat()))
                entries.add(com.github.mikephil.charting.data.BarEntry(3f, femalePigs.toFloat()))

                val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "")
                dataSet.colors = listOf(
                    Color.parseColor("#FFC107"),
                    Color.parseColor("#FF5722"),
                    Color.parseColor("#2196F3"),
                    Color.parseColor("#E91E63")
                )
                dataSet.valueTextColor = Color.BLACK

                val barData = com.github.mikephil.charting.data.BarData(dataSet)
                barData.barWidth = 0.8f

                binding.pigsAndCagesChart.apply {
                    data = barData
                    xAxis.apply {
                        granularity = 1f
                        position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false) // Remove vertical lines
                        setDrawLabels(false)
                    }
                    axisLeft.apply {
                        axisMinimum = 0f
                        setDrawGridLines(true)
                    }
                    axisRight.isEnabled = false
                    legend.isEnabled = false
                    description.isEnabled = false
                    setFitBars(true)
                    invalidate()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to load analytics", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun healthPigsAnalytics() {
        val typeOptions = listOf("Select Type", "Vaccine", "Illness")
        val vaccineOptions = listOf(
            "Swine Fever Vaccine",
            "Foot and Mouth Disease Vaccine",
            "PRRS Vaccine",
            "Classical Swine Fever Vaccine",
            "Other"
        )
        val illnessOptions = listOf(
            "Swine Flu",
            "Foot and Mouth Disease",
            "Skin Infection",
            "Respiratory Infection",
            "Other"
        )

        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            typeOptions
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.typeDropdown.adapter = typeAdapter

        val api = FetchAllPigsRI.getInstance()
        lifecycleScope.launch {
            val allPigs = try {
                api.fetchAllPigs()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to load pigs", Toast.LENGTH_SHORT).show()
                return@launch
            }

            binding.typeDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedType = parent.getItemAtPosition(position).toString()
                    if (selectedType == "Select Type") {
                        updatePigVaccineChart(0, 0)
                        return
                    }

                    // Show a dialog to pick specific vaccine or illness
                    val options = if (selectedType == "Vaccine") vaccineOptions else illnessOptions
                    android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Select $selectedType")
                        .setItems(options.toTypedArray()) { _, which ->
                            val selectedOption = options[which]

                            val filteredPigs = if (selectedType == "Vaccine") {
                                allPigs.filter { it.vaccine == selectedOption }
                            } else {
                                allPigs.filter { it.illness == selectedOption }
                            }

                            val maleCount = filteredPigs.count { it.gender == "Male" }
                            val femaleCount = filteredPigs.count { it.gender == "Female" }

                            updatePigVaccineChart(maleCount, femaleCount)
                        }
                        .show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }



    private fun updatePigVaccineChart(maleCount: Int, femaleCount: Int) {
        val entries = ArrayList<com.github.mikephil.charting.data.BarEntry>()
        entries.add(com.github.mikephil.charting.data.BarEntry(0f, maleCount.toFloat()))
        entries.add(com.github.mikephil.charting.data.BarEntry(1f, femaleCount.toFloat()))

        val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "")
        dataSet.colors = listOf(Color.BLUE, Color.MAGENTA) // Male = blue, Female = pink
        dataSet.valueTextColor = Color.BLACK

        val barData = com.github.mikephil.charting.data.BarData(dataSet)
        barData.barWidth = 0.7f

        binding.pigVaccineChart.apply {
            data = barData
            xAxis.apply {
                granularity = 1f
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false) // Remove vertical lines
                setDrawLabels(false)
            }
            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
            }
            axisRight.isEnabled = false
            legend.isEnabled = false // Remove legend
            description.isEnabled = false
            setDrawValueAboveBar(true)
            setFitBars(true)
            invalidate() // Refresh chart
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
