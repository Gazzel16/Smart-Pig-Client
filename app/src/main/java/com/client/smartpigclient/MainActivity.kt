package com.client.smartpigclient

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.client.smartpigclient.Cages.Fragments.CagesFragment
import com.client.smartpigclient.Dashboard.Fragments.DashBoardFragment
import com.client.smartpigclient.Pigs.Fragments.PigFragment
import com.client.smartpigclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Show DashboardFragment first
        replaceFragment(DashBoardFragment())

        // Bottom navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    replaceFragment(DashBoardFragment())
                    true
                }
                R.id.nav_cages -> {
                    replaceFragment(CagesFragment())
                    true
                }
//                R.id.nav_settings -> {
//                    replaceFragment(DashboardFragment())
//                    true
//                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}
