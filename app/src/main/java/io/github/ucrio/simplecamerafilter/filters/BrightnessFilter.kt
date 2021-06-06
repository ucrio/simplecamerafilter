package io.github.ucrio.simplecamerafilter.filters

import android.graphics.Bitmap
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.material.slider.Slider
import io.github.ucrio.simplecamerafilter.R
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.util.*

class BrightnessFilter (name: String): AbsFilter(name) {

    lateinit var sliderAlpha: Slider
    lateinit var sliderBeta: Slider

    private val MIN_ALPHA = 0f
    private val DEFAULT_ALPHA = 1.0f
    private val MAX_ALPHA = 2.0f

    private val MIN_BETA = -100f
    private val DEFAULT_BETA = 0.0f
    private val MAX_BETA = 100f


    var alpha = DEFAULT_ALPHA
    var beta = DEFAULT_BETA

    override fun doFilter(src: Bitmap): Bitmap {

        this.alpha = this.sliderAlpha.value
        this.beta = this.sliderBeta.value

        val mat = Mat(src.height, src.width, CvType.CV_8UC3)
        Utils.bitmapToMat(src, mat)

        Core.multiply(mat, Scalar(this.alpha.toDouble(), this.alpha.toDouble(), this.alpha.toDouble(), 1.0), mat)
        Core.add(mat, Scalar(this.beta.toDouble(), this.beta.toDouble(), this.beta.toDouble(), 1.0), mat)
        val result = Bitmap.createBitmap(mat.cols(), mat.rows(), src.config);
        Utils.matToBitmap(mat, result)
        return result
    }

    override fun initialize(parent: LinearLayout) {
        this.sliderAlpha = parent.findViewById<LinearLayout>(R.id.brtSliderSetAlpha).findViewById<Slider>(R.id.slider)
        this.sliderBeta = parent.findViewById<LinearLayout>(R.id.brtSliderSetBeta).findViewById<Slider>(R.id.slider)

        this.sliderAlpha.valueFrom = MIN_ALPHA
        this.sliderAlpha.valueTo = MAX_ALPHA
        this.sliderAlpha.value = this.alpha

        this.sliderBeta.valueFrom = MIN_BETA
        this.sliderBeta.valueTo = MAX_BETA
        this.sliderBeta.value = this.beta

        parent.findViewById<LinearLayout>(R.id.brtSliderSetAlpha).findViewById<TextView>(R.id.label).text = parent.resources.getString(R.string.brt_contrast)
        parent.findViewById<LinearLayout>(R.id.brtSliderSetBeta).findViewById<TextView>(R.id.label).text = parent.resources.getString(R.string.brt_brightness)

        parent.findViewById<Button>(R.id.buttonDefault).setOnClickListener {
            default()
        }

    }

    private fun default() {
        this.sliderAlpha.value = DEFAULT_ALPHA
        this.sliderBeta.value = DEFAULT_BETA
    }
}