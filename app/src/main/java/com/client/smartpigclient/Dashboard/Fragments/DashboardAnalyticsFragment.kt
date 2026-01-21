package com.client.smartpigclient.Dashboard.Fragments

import android.R.attr.data
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.telecom.Call
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.client.smartpigclient.Cages.Api.FetchCageRI
import com.client.smartpigclient.Dashboard.Api.DashBoardApi
import com.client.smartpigclient.Dashboard.Api.DashBoardRI
import com.client.smartpigclient.Dashboard.Model.ChatRequest
import com.client.smartpigclient.Pigs.Api.GetPigsAnalyticsRI
import com.client.smartpigclient.Pigs.Model.PigAnalyticsResponse

import com.client.smartpigclient.R
import com.client.smartpigclient.Utils.TokenManager
import com.client.smartpigclient.Utils.getSpinnerAdapter
import com.client.smartpigclient.databinding.FragmentDashboardAnalyticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import okhttp3.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import javax.security.auth.callback.Callback


class DashboardAnalyticsFragment : Fragment() {

    private var _binding: FragmentDashboardAnalyticsBinding? = null
    private val binding get() = _binding!!  // Only use between onCreateView and onDestroyView

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0

    private var totalPigs: Int = 0
    private var totalCages: Int = 0
    private var malePigs: Int = 0
    private var femalePigs: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDashboardAnalyticsBinding.inflate(inflater, container, false)

        binding.pigHealthInsights.setOnClickListener {
            if (binding.typeDropdown.selectedItemPosition == 0) {
                Toast.makeText(requireContext(), "Please select a category first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedType = binding.categoryDropdown.selectedItem.toString()

            // Skip placeholder
            if (selectedType == "Select Type" || selectedType.startsWith("Select")) {
                Toast.makeText(requireContext(), "Please select a valid category first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Count pigs for this category
            val api = DashBoardRI.getInstance(TokenManager.getToken(requireContext()))
            lifecycleScope.launch {
                val allPigs = try {
                    api.fetchAllPigs()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Failed to load pigs", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val filteredPigs =
                    if (binding.typeDropdown.selectedItem == "Vaccine") {
                        allPigs.filter { it.vaccine == selectedType }
                    } else {
                        allPigs.filter { it.illness == selectedType }
                    }

                val pigsCount = filteredPigs.size
                val maleCount = filteredPigs.count { it.gender == "Male" }
                val femaleCount = filteredPigs.count { it.gender == "Female" }

                // Send message to chatbot with counts
                navigateToChatBot(
                    "Give me health insights about pig $selectedType. " +
                            "Total pigs recorded: $pigsCount (Male: $maleCount, Female: $femaleCount)."
                )
            }
        }
        binding.pigsAndCagesInsights.setOnClickListener {
            // Make sure data is loaded
            if (totalPigs == 0 && totalCages == 0) {
                Toast.makeText(requireContext(), "Analytics not loaded yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Build the message just like pigHealthInsights
            val message = "Give me insights about pigs and cages status. " +
                    "Total Pigs: $totalPigs (Male: $malePigs, Female: $femalePigs), " +
                    "Total Cages: $totalCages."

            // Navigate to chat fragment and let it handle API call
            navigateToChatBot(message)
        }



        binding.yearAndMonth.setOnClickListener {
            showMonthYearPicker()
        }

        pigsAnalyticsSummary()
        pigsAndCagesChart()
        healthPigsAnalytics()
        return binding.root
    }

    private fun navigateToChatBot(insightMessage: String) {
        val fragment = DashboardChatBotFragment().apply {
            arguments = Bundle().apply {
                putString("INSIGHT_MESSAGE", insightMessage)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // ⚠️ use your real container id
            .addToBackStack(null)
            .commit()
    }

    private fun pigsAndCagesChart() {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", "") ?: ""

        val apiPigs = DashBoardRI.getInstance(token)
        val apiCage = FetchCageRI.getInstance(token)

        lifecycleScope.launch {
            try {
                val allPigs = apiPigs.fetchAllPigs()
                val allCages = apiCage.fetchCage()

                // Save counts to fragment-level variables
                totalPigs = allPigs.size
                totalCages = allCages.size
                malePigs = allPigs.count { it.gender == "Male" }
                femalePigs = allPigs.count { it.gender == "Female" }

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
                        setDrawGridLines(false)
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
            "Select Vaccine",
            "Swine Fever Vaccine",
            "Foot and Mouth Disease Vaccine",
            "PRRS Vaccine",
            "Classical Swine Fever Vaccine",
            "Other",
            "Select Type"
        )

        val illnessOptions = listOf(
            "Select Illness",
            "Swine Flu",
            "Foot and Mouth Disease",
            "Skin Infection",
            "Respiratory Infection",
            "Other",
            "Select Type"
        )

        binding.typeDropdown.adapter = getSpinnerAdapter(requireContext(), typeOptions)
        binding.typeDropdown.visibility = View.VISIBLE
        binding.categoryDropdown.visibility = View.GONE

        val api = DashBoardRI.getInstance(TokenManager.getToken(requireContext()))

        lifecycleScope.launch {
            val allPigs = api.fetchAllPigs()

            // 🔹 TYPE SPINNER
            binding.typeDropdown.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        when (position) {
                            1 -> { // Vaccine
                                binding.categoryDropdown.adapter = getSpinnerAdapter(requireContext(), vaccineOptions)
                                binding.categoryDropdown.visibility = View.VISIBLE
                                binding.typeDropdown.visibility = View.GONE
                            }

                            2 -> { // Illness

                                binding.categoryDropdown.adapter = getSpinnerAdapter(requireContext(), illnessOptions)
                                binding.categoryDropdown.visibility = View.VISIBLE
                                binding.typeDropdown.visibility = View.GONE
                            }

                            else -> {
                                binding.categoryDropdown.visibility = View.GONE
                                updatePigVaccineChart(0, 0)
                            }
                        }

                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            // 🔹 CATEGORY SPINNER
            binding.categoryDropdown.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (position == 0) return

                        val selectedValue = parent.getItemAtPosition(position).toString()


                        if (selectedValue == "Select Type") {
                            binding.categoryDropdown.visibility = View.GONE
                            binding.typeDropdown.visibility = View.VISIBLE
                            binding.typeDropdown.setSelection(0) // reset typeDropdown
                            return
                        }

                        if (position == 0) return // skip placeholder

                        val filteredPigs =
                            if (binding.typeDropdown.selectedItem == "Vaccine") {
                                allPigs.filter { it.vaccine == selectedValue }
                            } else {
                                allPigs.filter { it.illness == selectedValue }
                            }

                        val maleCount = filteredPigs.count { it.gender == "Male" }
                        val femaleCount = filteredPigs.count { it.gender == "Female" }

                        updatePigVaccineChart(maleCount, femaleCount)
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
                val pigAnalyticsSummaryApi = GetPigsAnalyticsRI.getPigsAnalyticsSummaryApi(TokenManager.getToken(requireContext()))
                val pigAnalyticsSummary = pigAnalyticsSummaryApi.getPigsAnalyticsSummary()  // suspend call

                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1

                val pigsAnalyticsSummaryByPeriodApi = GetPigsAnalyticsRI.getPigsAnalyticsSummaryByPeriodApi(TokenManager.getToken(requireContext()))
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

    private fun showMonthYearPicker() {
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_month_year,
            null
        )

        val monthInput = dialogView.findViewById<AutoCompleteTextView>(R.id.monthSpinner)
        val yearInput = dialogView.findViewById<AutoCompleteTextView>(R.id.yearSpinner)

        // 🔹 Months
        val months = listOf(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        )

        val monthAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            months
        )
        monthInput.setAdapter(monthAdapter)
        monthInput.inputType = 0  // prevent keyboard from showing
        monthInput.setOnClickListener { monthInput.showDropDown() }

        // 🔹 Years (currentYear -5 to currentYear +5)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear + 5).toList()

        val yearAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            years
        )
        yearInput.setAdapter(yearAdapter)
        yearInput.inputType = 0
        yearInput.setOnClickListener { yearInput.showDropDown() }

        // 🔹 Default selection
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        monthInput.setText(months[currentMonth], false)
        yearInput.setText(currentYear.toString(), false)

        // 🔹 Dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Select Month & Year")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->

                val selectedMonthIndex = months.indexOf(monthInput.text.toString())
                val selectedYearValue = yearInput.text.toString().toIntOrNull() ?: currentYear

                selectedMonth = selectedMonthIndex + 1 // 1–12
                selectedYear = selectedYearValue

                // Display in main EditText
                binding.yearAndMonth.setText("${months[selectedMonthIndex]} $selectedYear")

                // Trigger API
                fetchAnalyticsByPeriod()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    private fun fetchAnalyticsByPeriod() {

        if (selectedYear == 0 || selectedMonth == 0) return

        lifecycleScope.launch {
            try {
                val api = GetPigsAnalyticsRI
                    .getPigsAnalyticsSummaryByPeriodApi(TokenManager.getToken(requireContext()))

                val response = api.getPigsAnalyticsSummaryByPeriod(
                    year = selectedYear,
                    month = selectedMonth
                )

                pigsAnalyticsSummaryByPeriodBarChart(response)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Failed to load analytics for selected month",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun pigsAnalyticsSummaryByPeriodBarChart(
        analytics: PigAnalyticsResponse
    ) {
        val entries = arrayListOf(
            BarEntry(0f, analytics.soldPigs.toFloat()),
            BarEntry(1f, analytics.unsoldPigs.toFloat()),
            BarEntry(2f, analytics.totalEarnings.toFloat())
        )

        val dataSet = BarDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"),
            Color.parseColor("#D31305"),
            Color.parseColor("#2196F3")
        )
        dataSet.valueTextColor = Color.BLACK

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        binding.perPeriodEarningsChart.apply {
            data = barData
            setFitBars(true)
            description.isEnabled = false
            legend.isEnabled = false

            xAxis.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
            }

            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f

            animateY(800)
            invalidate()
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
