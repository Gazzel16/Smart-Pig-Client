package com.client.smartpigclient.Dashboard.Fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.client.smartpigclient.Dashboard.Api.DashBoardApi
import com.client.smartpigclient.Dashboard.Api.DashBoardRI
import com.client.smartpigclient.Dashboard.Model.RelayModel
import com.client.smartpigclient.R
import com.client.smartpigclient.Utils.TokenManager
import com.client.smartpigclient.databinding.FragmentDashboardSensorBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis

// MPAndroidChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry

// Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class DashboardSensorFragment : Fragment() {

    private var _binding: FragmentDashboardSensorBinding? = null
    private val binding get() = _binding!!

    private val database = FirebaseDatabase.getInstance()
    private val sensorRef = database.getReference("sensor")
    private var lastTemperature: Int? = null
    private var lastHumidity: Int? = null
    private var lastWaterIndicator: Int? = null
    private lateinit var tempDataSet: LineDataSet
    private lateinit var humidDataSet: LineDataSet
    private lateinit var waterIndicatorDataSet: LineDataSet
    private lateinit var lineData: LineData
    private var index = 0f // X-axis counter

    private var ignoreSwitchListener = false
    private var currentRelayState = false
    private var countdownJob: Job? = null
    private var remainingSeconds = 0


    private lateinit var dashboardApi: DashBoardApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardSensorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val token = TokenManager.getToken(requireContext())
        dashboardApi = DashBoardRI.getInstance(token)



        relaySwitch()
        startRelayAutoSync()
        tempHumidDescription()
        setupChart(binding.sensorChart)
        listenSensorData()
    }

    private fun relaySwitch(){
        currentRelayState = false
        binding.relaySwitch.isChecked = false
        updateRelaySwitchUI(false)

        binding.relaySwitch.setOnCheckedChangeListener { _, isChecked ->
            // Ignore programmatic updates
            if (ignoreSwitchListener) return@setOnCheckedChangeListener

            // No-op if same state
            if (currentRelayState == isChecked) return@setOnCheckedChangeListener

            updateRelaySwitchUI(isChecked)

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val updatedRelay =
                        dashboardApi.setRelayRequest(RelayModel(isChecked))

                    currentRelayState = updatedRelay.is_on
                    updateRelaySwitchUI(isChecked)
                    Log.d("Relay", "Relay updated: ${updatedRelay.is_on}")

                } catch (e: Exception) {
                    Log.e("Relay", "Failed to update relay", e)

                    ignoreSwitchListener = true
                    binding.relaySwitch.isChecked = currentRelayState

                    updateRelaySwitchUI(isChecked)
                    ignoreSwitchListener = false
                }
            }
        }
    }
    private fun startRelayAutoSync() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (isAdded) { // run only while fragment is active
                try {
                    val relay = dashboardApi.getRelayResponse()
                    if (currentRelayState != relay.is_on) {
                        ignoreSwitchListener = true
                        binding.relaySwitch.isChecked = relay.is_on
                        currentRelayState = relay.is_on

                        updateRelaySwitchUI(relay.is_on)
                        ignoreSwitchListener = false
                    }

                } catch (e: Exception) {
                    Log.e("Relay", "Failed to fetch relay state", e)
                }

                // Wait before checking again (every 5 seconds here)
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    private fun updateRelaySwitchUI(isOn: Boolean) {
        val color = if (isOn) Color.parseColor("#009688") else Color.parseColor("#9E9E9E")
        binding.relaySwitch.thumbTintList = ColorStateList.valueOf(color)
        binding.relaySwitch.trackTintList = ColorStateList.valueOf(color)
        binding.relaySwitch.text = if (isOn) "ON" else "OFF"

        updateTimerVisibility(isOn)
    }

    private fun updateTimerVisibility(isOn: Boolean) {
        // Stop any existing countdown
        countdownJob?.cancel()
        countdownJob = null

        if (!isOn) {
            binding.timer.visibility = View.GONE
            return
        }

        binding.timer.visibility = View.VISIBLE

        var remainingSeconds = 2 * 60 // change to 2 * 60 if needed

        countdownJob = viewLifecycleOwner.lifecycleScope.launch {
            while (remainingSeconds >= 0 && isActive) {
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                binding.timer.text =
                    String.format("Timer: %02d:%02d", minutes, seconds)

                delay(1000)
                remainingSeconds--
            }

            // Optional: auto-hide when finished
            binding.timer.visibility = View.GONE
        }
    }


    private fun setupChart(chart: LineChart) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)

        // X Axis
        val xAxis: XAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        // Y Axis
        val leftAxis: YAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.LTGRAY
        leftAxis.setDrawAxisLine(false)

        chart.axisRight.isEnabled = false

        // Temperature dataset
        tempDataSet = LineDataSet(ArrayList(), "").apply {
            color = Color.RED
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)

            fillColor = Color.RED
            fillAlpha = 80
        }

        // Humidity dataset
        humidDataSet = LineDataSet(ArrayList(), "").apply {
            color = Color.BLUE
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = Color.BLUE
            fillAlpha = 80
        }

        waterIndicatorDataSet = LineDataSet(ArrayList(), "").apply {
            color = Color.parseColor("#4CAF50")
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = Color.parseColor("#4CAF50")
            fillAlpha = 80
        }

        lineData = LineData(tempDataSet, humidDataSet, waterIndicatorDataSet)
        chart.data = lineData
        chart.invalidate()
    }
    private fun listenSensorData() {
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.child("temp").getValue(Double::class.java)?.toFloat()
                val humid = snapshot.child("humid").getValue(Double::class.java)?.toFloat()
                val waterIndicator = snapshot.child("rain").getValue(Double::class.java)?.toFloat()

                if (temp != null && humid != null && waterIndicator != null) {
                    // Only update if view is still alive
                    _binding?.let { binding ->
                        // Add entries
                        tempDataSet.addEntry(Entry(index, temp))
                        humidDataSet.addEntry(Entry(index, humid))
                        waterIndicatorDataSet.addEntry(Entry(index, waterIndicator ))
                        index += 1f

                        // Notify chart
                        lineData.notifyDataChanged()
                        binding.sensorChart.notifyDataSetChanged()
                        binding.sensorChart.invalidate()

                        // Scroll to latest
                        binding.sensorChart.moveViewToX(tempDataSet.entryCount.toFloat())

                        // Update text
                        binding.tempData.text = "Temperature: ${temp}°C"
                        binding.humidData.text = "Humidity: ${humid}%"
                        binding.waterData.text = "Water Level: ${waterIndicator}%"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun tempHumidDescription() {
        sensorRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.child("temp").getValue(Double::class.java)?.toFloat()
                val humid = snapshot.child("humid").getValue(Double::class.java)?.toFloat()
                val waterIndicator = snapshot.child("rain").getValue(Double::class.java)?.toFloat()
                // Only proceed if temp and humid are not null
                if (temp != null && humid != null && waterIndicator != null) {
                    val temperature = temp.toInt()
                    val humidity = humid.toInt()
                    val water = waterIndicator.toInt()

                    // Only update if values changed
                    if (temperature != lastTemperature || humidity != lastHumidity || water != lastWaterIndicator) {

                        // Temperature message
                        val tempMsg = when {
                            temperature > 33 -> "The current temperature is ${temperature}°C. Hot environment. Increase airflow, provide shade, and watch for signs of heat stress."
                            else -> "The current temperature is normal."
                        }

                        // Humidity message
                        val humidMsg = when {
                            humidity > 90 -> "The current humidity is ${humidity}% High humidity detected. Increase airflow and keep bedding dry to avoid disease risk."
                            else -> "The current humidity is in normal range."
                        }

                        val waterMsg = when {
                            waterIndicator > 70 -> "The water is above average"
                            waterIndicator < 20 -> "The water is below average"
                            else -> "The water is at a normal level"
                        }

                        // Set the TextViews
                        binding.tempDescription.text = tempMsg
                        binding.humidDescription.text = humidMsg
                        binding.waterDescription.text = waterMsg

                        // Update last values
                        lastTemperature = temperature
                        lastHumidity = humidity
                        lastWaterIndicator = water
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Optional: log error
                Log.e("Firebase", "Failed to read sensor data", error.toException())
            }
        })
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

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardSensorFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
