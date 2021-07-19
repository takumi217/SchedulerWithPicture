package com.example.myscheduler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.myscheduler.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        val naviController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(naviController)

        binding.fab.setOnClickListener { view ->
            naviController.navigate(R.id.action_to_scheduleEditFragment)
        }
    }

    override fun onSupportNavigateUp()
            = findNavController(R.id.nav_host_fragment).navigateUp()

    fun setFabVisible(visibility: Int) {
        binding.fab.visibility = visibility
    }
}