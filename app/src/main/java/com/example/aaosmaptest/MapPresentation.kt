package com.example.aaosmaptest

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display

class MapPresentation(outerContext: Context, display: Display) : Presentation(outerContext, display) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.presentation_map)
    }
}
