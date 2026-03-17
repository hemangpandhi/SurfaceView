package com.example.aaosmaptest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.window.embedding.RuleController
import com.example.aaosmaptest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var isDrivingState = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Activity Embedding rules from XML
        val ruleController = RuleController.getInstance(this)
        ruleController.setRules(RuleController.parseRules(this, R.xml.main_split_config))
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.drivingStateSwitch.setOnCheckedChangeListener { _, isChecked ->
            isDrivingState = isChecked
            // Notify the fragment
            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragmentContainer) as? MapFragment
            mapFragment?.setDrivingState(isDrivingState)
        }
    }
}
