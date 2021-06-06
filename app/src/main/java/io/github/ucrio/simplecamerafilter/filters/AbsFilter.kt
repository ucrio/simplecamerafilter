package io.github.ucrio.simplecamerafilter.filters

import android.graphics.Bitmap
import android.widget.LinearLayout
import org.opencv.core.Mat

abstract class AbsFilter(val name: String) {

    abstract fun doFilter(src: Bitmap): Bitmap
    abstract fun initialize(parent: LinearLayout)
}