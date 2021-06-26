package io.github.ucrio.simplecamerafilter.filters

import android.graphics.Bitmap
import android.widget.*
import com.google.android.material.slider.Slider
import io.github.ucrio.simplecamerafilter.R
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*

class RgbFilter (name: String): AbsFilter(name) {

    lateinit var sliderRed: Slider
    lateinit var sliderGreen: Slider
    lateinit var sliderBlue: Slider

    private val DEFAULT_RED = 0f
    private val DEFAULT_GREEN = 0f
    private val DEFAULT_BLUE = 0f

    var red = DEFAULT_RED
    var green = DEFAULT_GREEN
    var blue = DEFAULT_BLUE

    var isHsv = false

    override fun doFilter(src: Bitmap): Bitmap {
        this.red = this.sliderRed.value
        this.green = this.sliderGreen.value
        this.blue = this.sliderBlue.value

        val mat = Mat(src.height, src.width, CvType.CV_8UC3)
        Utils.bitmapToMat(src, mat)

        if (isHsv) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HSV)
        }

        val list: List<Mat> = ArrayList()
        Core.split(mat, list)

        //val aggregateRed = Scalar(255.0 * (this.red / (this.sliderRed.valueTo / 2.0) - 1.0))
        //val aggregateGreen = Scalar(255.0 * (this.green / (this.sliderGreen.valueTo / 2.0) - 1.0))
        //val aggregateBlue = Scalar(255.0 * (this.blue / (this.sliderBlue.valueTo / 2.0) - 1.0))

        val aggregateRed = Scalar(255.0 * (this.red / this.sliderRed.valueTo))
        val aggregateGreen = Scalar(255.0 * (this.green / this.sliderGreen.valueTo))
        val aggregateBlue = Scalar(255.0 * (this.blue / this.sliderBlue.valueTo))

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

        if (isHsv) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_HSV2RGB)
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2RGBA)
        }
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

        this.sliderRed.valueFrom = -50f
        this.sliderGreen.valueFrom = -50f
        this.sliderBlue.valueFrom = -50f
        this.sliderRed.valueTo = 50f
        this.sliderGreen.valueTo = 50f
        this.sliderBlue.valueTo = 50f


        parent.findViewById<LinearLayout>(R.id.rgbSliderSetRed).findViewById<TextView>(R.id.label).text = parent.resources.getString(R.string.adder_red)
        parent.findViewById<LinearLayout>(R.id.rgbSliderSetGreen).findViewById<TextView>(R.id.label).text = parent.resources.getString(R.string.adder_green)
        parent.findViewById<LinearLayout>(R.id.rgbSliderSetBlue).findViewById<TextView>(R.id.label).text = parent.resources.getString(R.string.adder_blue)

        parent.findViewById<Button>(R.id.buttonDefault).setOnClickListener {
            default()
        }

        parent.findViewById<CheckBox>(R.id.checkHSV).setOnCheckedChangeListener { compoundButton, b ->
            if (isHsv != b) {
                default()
                setLabels(parent, b)
                isHsv = b
            }
        }
    }

    private fun default() {
        this.sliderRed.value = DEFAULT_RED
        this.sliderGreen.value = DEFAULT_GREEN
        this.sliderBlue.value = DEFAULT_BLUE
    }

    private fun setLabels(parent: LinearLayout, isHsv: Boolean) {
        val labelRed = parent.findViewById<LinearLayout>(R.id.rgbSliderSetRed).findViewById<TextView>(R.id.label)
        val labelGreen = parent.findViewById<LinearLayout>(R.id.rgbSliderSetGreen).findViewById<TextView>(R.id.label)
        val labelBlue = parent.findViewById<LinearLayout>(R.id.rgbSliderSetBlue).findViewById<TextView>(R.id.label)

        if (isHsv) {
            labelRed.text = parent.resources.getString(R.string.adder_hue)
            labelGreen.text = parent.resources.getString(R.string.adder_saturation)
            labelBlue.text = parent.resources.getString(R.string.adder_value)
        } else {
            labelRed.text = parent.resources.getString(R.string.adder_red)
            labelGreen.text = parent.resources.getString(R.string.adder_green)
            labelBlue.text = parent.resources.getString(R.string.adder_blue)
        }
    }
}