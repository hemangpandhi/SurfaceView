package com.example.aaosmaptest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aaosmaptest.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var virtualDisplayManager: MapVirtualDisplayManager? = null
    private var isDrivingState = false
    private var viewSavedState: Bundle? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        viewSavedState = savedInstanceState?.getBundle("viewSavedState")
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        virtualDisplayManager = MapVirtualDisplayManager(requireContext())

        binding.mapSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                virtualDisplayManager?.onSurfaceCreated(
                    holder.surface,
                    binding.mapSurfaceView.width,
                    binding.mapSurfaceView.height,
                    resources.displayMetrics.densityDpi,
                    viewSavedState
                )
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                virtualDisplayManager?.onSurfaceChanged(width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                virtualDisplayManager?.onSurfaceDestroyed()
            }
        })
        
        binding.mapSurfaceView.setOnTouchListener { v, event ->
            if (isDrivingState) {
                true 
            } else {
                false
            }
        }
    }
    
    // SDK lifecycles (onStart, onResume, onPause, etc.) are no longer needed 
    // to be manually forwarded here, because we are launching a completely independent
    // Activity via Intent. The AAOS WindowManager manages that Activity's lifecycle.

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Basic state save if needed
    }

    fun setDrivingState(driving: Boolean) {
        isDrivingState = driving
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        virtualDisplayManager?.onSurfaceDestroyed()
        virtualDisplayManager = null
    }
}
