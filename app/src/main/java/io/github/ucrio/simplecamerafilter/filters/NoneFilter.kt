package io.github.ucrio.simplecamerafilter.filters

import android.graphics.Bitmap
import android.widget.LinearLayout
import org.opencv.core.Mat

class NoneFilter(name: String): AbsFilter(name) {

    override fun doFilter(src: Bitmap): Bitmap {
        return src
    }

    override fun initialize(parent: LinearLayout) {
        //Do Nothing
    }
    
}