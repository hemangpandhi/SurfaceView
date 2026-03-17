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
        
        binding.btnSanFrancisco.setOnClickListener { onLocationClicked("geo:37.7749,-122.4194?q=San+Francisco") }
        binding.btnNewYork.setOnClickListener { onLocationClicked("geo:40.7128,-74.0060?q=New+York+City") }
        binding.btnLondon.setOnClickListener { onLocationClicked("geo:51.5074,-0.1278?q=London") }
        binding.btnTokyo.setOnClickListener { onLocationClicked("geo:35.6762,139.6503?q=Tokyo") }
    }
    
    private fun onLocationClicked(geoUri: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(geoUri))
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragmentContainer) as? MapFragment
        mapFragment?.updateMapLocation(intent)
    }
}
