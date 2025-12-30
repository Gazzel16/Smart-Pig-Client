package com.client.smartpigclient.Dashboard.Fragments

import android.R.attr.data
import android.graphics.Color
import android.os.Bundle
import android.telecom.Call
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.client.smartpigclient.Cages.Api.FetchCageRI
import com.client.smartpigclient.Dashboard.Api.DashBoardApi
import com.client.smartpigclient.Dashboard.Api.DashBoardRI
import com.client.smartpigclient.Pigs.Api.GetPigsAnalyticsRI
import com.client.smartpigclient.Pigs.Model.PigAnalyticsResponse

import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.FragmentDashboardAnalyticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import okhttp3.Response
import java.time.LocalDate
import java.util.Calendar
import javax.security.auth.callback.Callback


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

        pigsAnalyticsSummary()
        pigsAndCagesChart()
        healthPigsAnalytics()
        return binding.root
    }

    private fun pigsAndCagesChart() {
        val apiPigs = DashBoardRI.getInstance()
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

        val api = DashBoardRI.getInstance()
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

    private fun pigsAnalyticsSummary() {
        // Use lifecycleScope in Fragment
        lifecycleScope.launch {
            try {
                val pigAnalyticsSummaryApi = GetPigsAnalyticsRI.getPigsAnalyticsSummaryApi()
                val pigAnalyticsSummary = pigAnalyticsSummaryApi.getPigsAnalyticsSummary()  // suspend call

                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1

                val pigsAnalyticsSummaryByPeriodApi = GetPigsAnalyticsRI.getPigsAnalyticsSummaryByPeriodApi()
                val pigsAnalyticsSummaryByPeriod = pigsAnalyticsSummaryByPeriodApi.getPigsAnalyticsSummaryByPeriod(year = year, month = month)

                pigsAnalyticsSummaryByPeriodBarChart(pigsAnalyticsSummaryByPeriod)
                pigsAnalyticsSummaryBarChart(pigAnalyticsSummary)  // populate the chart
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun pigsAnalyticsSummaryBarChart(analytics: PigAnalyticsResponse) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, analytics.soldPigs.toFloat()))
        entries.add(BarEntry(1f, analytics.unsoldPigs.toFloat()))
        entries.add(BarEntry(2f, analytics.totalEarnings.toFloat()))

        val dataSet = BarDataSet(entries, "") // no label
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#D31305"), // Red
            Color.parseColor("#2196F3")  // Blue
        )


        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        binding.totalEarningsChart.data = barData
        binding.totalEarningsChart.setFitBars(true)
        binding.totalEarningsChart.description.isEnabled = false

        // Disable legend
        binding.totalEarningsChart.legend.isEnabled = false

        // Hide X-axis labels
        binding.totalEarningsChart.xAxis.setDrawLabels(false)
        binding.totalEarningsChart.xAxis.setDrawGridLines(false)
        binding.totalEarningsChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        // Optional: Hide Y-axis labels if you want a very minimal chart
        binding.totalEarningsChart.axisRight.isEnabled = false
        binding.totalEarningsChart.axisLeft.setDrawLabels(true) // keep Y-axis values if needed

        binding.totalEarningsChart.animateY(1000)
        binding.totalEarningsChart.invalidate()
    }

    private fun pigsAnalyticsSummaryByPeriodBarChart(analytics: PigAnalyticsResponse) {
//        val entries = ArrayList<BarEntry>()
//        entries.add(BarEntry(0f, analytics.soldPigs.toFloat()))
//        entries.add(BarEntry(1f, analytics.unsoldPigs.toFloat()))
//        entries.add(BarEntry(2f, analytics.totalEarnings.toFloat()))
//
//        val dataSet = BarDataSet(entries, "") // no label
//        dataSet.colors = listOf(
//            Color.parseColor("#4CAF50"), // Green
//            Color.parseColor("#D31305"), // Red
//            Color.parseColor("#2196F3")  // Blue
//        )
//
//
//        val barData = BarData(dataSet)
//        barData.barWidth = 0.9f
//
//        binding.totalEarningsChart.data = barData
//        binding.totalEarningsChart.setFitBars(true)
//        binding.totalEarningsChart.description.isEnabled = false
//
//        // Disable legend
//        binding.totalEarningsChart.legend.isEnabled = false
//
//        // Hide X-axis labels
//        binding.totalEarningsChart.xAxis.setDrawLabels(false)
//        binding.totalEarningsChart.xAxis.setDrawGridLines(false)
//        binding.totalEarningsChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
//
//        // Optional: Hide Y-axis labels if you want a very minimal chart
//        binding.totalEarningsChart.axisRight.isEnabled = false
//        binding.totalEarningsChart.axisLeft.setDrawLabels(true) // keep Y-axis values if needed
//
//        binding.totalEarningsChart.animateY(1000)
//        binding.totalEarningsChart.invalidate()
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
