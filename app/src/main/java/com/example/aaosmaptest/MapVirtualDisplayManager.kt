package com.example.aaosmaptest

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.Bundle
import android.view.Surface

/**
 * Manages the VirtualDisplay lifecycle corresponding to the SurfaceView lifecycle,
 * and launches a Presentation onto it for rendering.
 */
class MapVirtualDisplayManager(private val context: Context) {

    private var virtualDisplay: VirtualDisplay? = null
    private var presentation: MapPresentation? = null

    fun onSurfaceCreated(surface: Surface, width: Int, height: Int, densityDpi: Int, savedInstanceState: Bundle?) {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        
        virtualDisplay = displayManager.createVirtualDisplay(
            "MapVirtualDisplay",
            width,
            height,
            densityDpi,
            surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
        )
        
        virtualDisplay?.display?.let { display ->
            presentation = MapPresentation(context, display).apply {
                show()
            }
        }
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        virtualDisplay?.resize(width, height, context.resources.displayMetrics.densityDpi)
    }

    fun onSurfaceDestroyed() {
        presentation?.dismiss()
        presentation = null
        virtualDisplay?.release()
        virtualDisplay = null
    }
}
