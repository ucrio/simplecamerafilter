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
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.util.*

class RgbFilter (name: String): AbsFilter(name) {

    lateinit var sliderRed: Slider
    lateinit var sliderGreen: Slider
    lateinit var sliderBlue: Slider

    private val DEFAULT_RED = 50.0f
    private val DEFAULT_GREEN = 50.0f
    private val DEFAULT_BLUE = 50.0f

    var red = DEFAULT_RED
    var green = DEFAULT_GREEN
    var blue = DEFAULT_BLUE

    override fun doFilter(src: Bitmap): Bitmap {

        this.red = this.sliderRed.value
        this.green = this.sliderGreen.value
        this.blue = this.sliderBlue.value

        val mat = Mat(src.height, src.width, CvType.CV_8UC3)
        Utils.bitmapToMat(src, mat)

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HSV)

        val list: List<Mat> = ArrayList()
        Core.split(mat, list)

        val aggregateRed = Scalar(255.0 * (this.red / (this.sliderRed.valueTo / 2.0) - 1.0))
        val aggregateGreen = Scalar(255.0 * (this.green / (this.sliderGreen.valueTo / 2.0) - 1.0))
        val aggregateBlue = Scalar(255.0 * (this.blue / (this.sliderBlue.valueTo / 2.0) - 1.0))

        Core.add(list[0], aggregateRed, list[0])
        Core.add(list[1], aggregateGreen, list[1])
        Core.add(list[2], aggregateBlue, list[2])

        Core.min(list[0], Scalar(255.0), list[0])
        Core.min(list[1], Scalar(255.0), list[1])
        Core.min(list[2], Scalar(255.0), list[2])

        Core.max(list[0], Scalar(0.0), list[0])
        Core.max(list[1], Scalar(0.0), list[1])
        Core.max(list[2], Scalar(0.0), list[2])

        Core.merge(list, mat)

        val result = Bitmap.createBitmap(mat.cols(), mat.rows(), src.config);

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_HSV2RGB)
        Utils.matToBitmap(mat, result)

        return result
    }

    override fun initialize(parent: LinearLayout) {
        this.sliderRed = parent.findViewById<LinearLayout>(R.id.rgbSliderSetRed).findViewById<Slider>(R.id.slider)
        this.sliderGreen = parent.findViewById<LinearLayout>(R.id.rgbSliderSetGreen).findViewById<Slider>(R.id.slider)
        this.sliderBlue = parent.findViewById<LinearLayout>(R.id.rgbSliderSetBlue).findViewById<Slider>(R.id.slider)

        this.sliderRed.value = red
        this.sliderGreen.value = green
        this.sliderBlue.value = blue

        parent.findViewById<LinearLayout>(R.id.rgbSliderSetRed).findViewById<TextView>(R.id.label).text = parent.resources.getString(R.string.adder_red)
        parent.findViewById<LinearLayout>(R.id.rgbSliderSetGreen).findViewById<TextView>(R.id.label).text = parent.resources.getString(R.string.adder_green)
        parent.findViewById<LinearLayout>(R.id.rgbSliderSetBlue).findViewById<TextView>(R.id.label).text = parent.resources.getString(R.string.adder_blue)

        parent.findViewById<Button>(R.id.buttonDefault).setOnClickListener {
            default()
        }

    }

    private fun default() {
        this.sliderRed.value = DEFAULT_RED
        this.sliderGreen.value = DEFAULT_GREEN
        this.sliderBlue.value = DEFAULT_BLUE
    }
}