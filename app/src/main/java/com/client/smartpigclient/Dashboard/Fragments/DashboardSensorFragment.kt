package com.client.smartpigclient.Dashboard.Fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.client.smartpigclient.R
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



class DashboardSensorFragment : Fragment() {

    private var _binding: FragmentDashboardSensorBinding? = null
    private val binding get() = _binding!!

    private val database = FirebaseDatabase.getInstance()
    private val sensorRef = database.getReference("sensor")
    private var lastTemperature: Int? = null
    private var lastHumidity: Int? = null
    private lateinit var tempDataSet: LineDataSet
    private lateinit var humidDataSet: LineDataSet
    private lateinit var lineData: LineData
    private var index = 0f // X-axis counter

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

        tempHumidDescription()

        setupChart(binding.sensorChart)
        listenSensorData()
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

        lineData = LineData(tempDataSet, humidDataSet)
        chart.data = lineData
        chart.invalidate()
    }
    private fun listenSensorData() {
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.child("temp").getValue(Double::class.java)?.toFloat()
                val humid = snapshot.child("humid").getValue(Double::class.java)?.toFloat()

                if (temp != null && humid != null) {
                    // Only update if view is still alive
                    _binding?.let { binding ->
                        // Add entries
                        tempDataSet.addEntry(Entry(index, temp))
                        humidDataSet.addEntry(Entry(index, humid))
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

                // Only proceed if temp and humid are not null
                if (temp != null && humid != null) {
                    val temperature = temp.toInt()
                    val humidity = humid.toInt()

                    // Only update if values changed
                    if (temperature != lastTemperature || humidity != lastHumidity) {

                        // Temperature message
                        val tempMsg = when {
                            temperature > 33 -> "The current temperature is ${temperature}°C. Hot environment. Increase airflow, provide shade, and watch for signs of heat stress."
                            else -> "The current temperature is ${temperature}°C. Environment is normal."
                        }

                        // Humidity message
                        val humidMsg = when {
                            humidity > 90 -> "The current humidity is ${humidity}% High humidity detected. Increase airflow and keep bedding dry to avoid disease risk."
                            else -> "The current humidity is ${humidity}%. Normal range."
                        }

                        // Set the TextViews
                        binding.tempDescription.text = tempMsg
                        binding.humidDescription.text = humidMsg

                        // Update last values
                        lastTemperature = temperature
                        lastHumidity = humidity
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
