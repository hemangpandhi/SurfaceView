package com.example.aaosmaptest

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.Bundle
import android.util.Log
import android.view.Surface

/**
 * Manages the VirtualDisplay lifecycle corresponding to the SurfaceView lifecycle,
 * and launches the actual AAOS GAS Google Maps application onto it.
 */
class MapVirtualDisplayManager(private val context: Context) {

    private var virtualDisplay: VirtualDisplay? = null

    fun onSurfaceCreated(surface: Surface, width: Int, height: Int, densityDpi: Int, savedInstanceState: Bundle?) {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        
        virtualDisplay = displayManager.createVirtualDisplay(
            "MapVirtualDisplay",
            width,
            height,
            densityDpi,
            surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or 
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION or 
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or
            256 // VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL (hidden public flag)
        )
        
        virtualDisplay?.display?.let { display ->
            launchActualGasMapActivity(display.displayId)
        }
    }

    private fun launchActualGasMapActivity(displayId: Int) {
        try {
            // Target the specific EmbeddedClusterActivity from Google Maps for isolated secondary displays
            val intent = Intent().apply {
                setClassName("com.google.android.apps.maps", "com.google.android.apps.gmm.car.embedded.auxiliarymap.EmbeddedClusterActivity")
                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }

            // Setting the target display ID to push the GAS map onto the SurfaceView
            val options = ActivityOptions.makeBasic().apply {
                launchDisplayId = displayId
            }

            context.startActivity(intent, options.toBundle())
            Log.d("MapVirtualDisplayManager", "Launched Actual GAS Maps Activity on display ID $displayId")
        } catch (e: Exception) {
            Log.e("MapVirtualDisplayManager", "Failed to launch Actual GAS Maps Activity on Virtual Display", e)
        }
    }

    fun updateMapLocation(intent: Intent) {
        val displayId = getDisplayId()
        if (displayId != null && displayId != android.view.Display.INVALID_DISPLAY) {
            try {
                // Ensure the intent is strictly targeted at our Map Activity
                intent.setClassName("com.google.android.apps.maps", "com.google.android.apps.gmm.car.embedded.auxiliarymap.EmbeddedClusterActivity")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                
                // Instruct the system to push this updated Intent directly to the target Virtual Display
                val options = ActivityOptions.makeBasic().apply {
                    launchDisplayId = displayId
                }
                context.startActivity(intent, options.toBundle())
                Log.d("MapVirtualDisplayManager", "Updated map location on display ID $displayId")
            } catch (e: Exception) {
                 Log.e("MapVirtualDisplayManager", "Failed to update map location", e)
            }
        }
    }

    fun getDisplayId(): Int? {
        return virtualDisplay?.display?.displayId
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        virtualDisplay?.resize(width, height, context.resources.displayMetrics.densityDpi)
    }

    fun onSurfaceDestroyed() {
        virtualDisplay?.release()
        virtualDisplay = null
    }
}
