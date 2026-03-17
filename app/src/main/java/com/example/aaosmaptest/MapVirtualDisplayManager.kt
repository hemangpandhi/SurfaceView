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
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
        )
        
        virtualDisplay?.display?.let { display ->
            launchActualGasMapActivity(display.displayId)
        }
    }

    private fun launchActualGasMapActivity(displayId: Int) {
        try {
            // Intent to launch the default map application (Actual Google Maps from AAOS GAS bundle)
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MAPS)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
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

    fun onSurfaceChanged(width: Int, height: Int) {
        virtualDisplay?.resize(width, height, context.resources.displayMetrics.densityDpi)
    }

    fun onSurfaceDestroyed() {
        virtualDisplay?.release()
        virtualDisplay = null
    }
}
