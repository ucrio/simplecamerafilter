package io.github.ucrio.simplecamerafilter.filters

import android.graphics.Bitmap
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import io.github.ucrio.simplecamerafilter.R
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgcodecs.Imgcodecs

class PassFilter(name: String): AbsFilter(name) {

    lateinit var sliderRed: RangeSlider
    lateinit var sliderGreen: RangeSlider
    lateinit var sliderBlue: RangeSlider
    lateinit var sliderGray: Slider

    private val DEFAULT_REDS = listOf(0f, 255f)
    private val DEFAULT_GREENS = listOf(0f, 255f)
    private val DEFAULT_BLUES = listOf(0f, 255f)
    private val DEFAULT_GRAY = 50.0f

    var reds = DEFAULT_REDS
    var greens = DEFAULT_GREENS
    var blues = DEFAULT_BLUES
    var gray = DEFAULT_GRAY

    var isHsv = false

    override fun doFilter(src: Bitmap): Bitmap {

        this.reds = sliderRed.values
        this.greens = sliderGreen.values
        this.blues = sliderBlue.values
        this.gray = sliderGray.value

        val lower = Scalar(reds[0].toDouble(), greens[0].toDouble(), blues[0].toDouble(), 0.0)
        val upper = Scalar(reds[1].toDouble(), greens[1].toDouble(), blues[1].toDouble(), 255.0)

        val mat = Mat(src.height, src.width, CvType.CV_8UC3)
        val mask = Mat(src.height, src.width, CvType.CV_8UC1)
        val matInv = Mat(src.height, src.width, CvType.CV_8UC1)

        Utils.bitmapToMat(src, mat)
        if (isHsv) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HSV)
            Core.inRange(mat, lower, upper, mask)
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_HSV2RGB)
        } else {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB)
            Core.inRange(mat, lower, upper, mask)
        }

        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2RGB)
        val foreground = Mat(src.height, src.width, CvType.CV_8UC3)
        val background = Mat(src.height, src.width, CvType.CV_8UC3)

        Core.bitwise_and(mat, mask, foreground)
        Core.bitwise_not(mask, matInv)

        Imgproc.cvtColor(mat, background, Imgproc.COLOR_RGB2GRAY)
        val aggregateGray = Scalar(255.0 * (this.gray / (this.sliderGray.valueTo / 2.0) - 1.0))
        Core.add(background, aggregateGray, background)
        Core.min(background, Scalar(255.0), background)
        Core.max(background, Scalar(0.0), background)
        Imgproc.cvtColor(background, background, Imgproc.COLOR_GRAY2RGB)
        Core.bitwise_and(background, matInv, background)

        val resultMat = Mat(src.height, src.width, CvType.CV_8UC3)
        Core.addWeighted(background, 1.0, foreground, 1.0, 0.0, resultMat)

        //Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_BGR2RGBA)

        val result = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), src.config);
        Utils.matToBitmap(resultMat, result)
        return result

    }

    override fun initialize(parent: LinearLayout) {
        this.sliderRed = parent.findViewById<LinearLayout>(R.id.passSliderSetRed).findViewById<RangeSlider>(R.id.slider)
        this.sliderGreen = parent.findViewById<LinearLayout>(R.id.passSliderSetGreen).findViewById<RangeSlider>(R.id.slider)
        this.sliderBlue = parent.findViewById<LinearLayout>(R.id.passSliderSetBlue).findViewById<RangeSlider>(R.id.slider)
        this.sliderGray = parent.findViewById<LinearLayout>(R.id.passSliderSetGray).findViewById<Slider>(R.id.slider)

        this.sliderRed.values = reds
        this.sliderGreen.values = greens
        this.sliderBlue.values = blues
        this.sliderGray.value = gray

        setLabels(parent, isHsv)

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
        this.sliderRed.values = DEFAULT_REDS
        this.sliderGreen.values = DEFAULT_GREENS
        this.sliderBlue.values = DEFAULT_BLUES
    }

    private fun setLabels(parent: LinearLayout, isHsv: Boolean) {
        val labelRed = parent.findViewById<LinearLayout>(R.id.passSliderSetRed).findViewById<TextView>(R.id.label)
        val labelGreen = parent.findViewById<LinearLayout>(R.id.passSliderSetGreen).findViewById<TextView>(R.id.label)
        val labelBlue = parent.findViewById<LinearLayout>(R.id.passSliderSetBlue).findViewById<TextView>(R.id.label)
        val labelGray = parent.findViewById<LinearLayout>(R.id.passSliderSetGray).findViewById<TextView>(R.id.label)

        if (isHsv) {
            labelRed.text = parent.resources.getString(R.string.pass_hue)
            labelGreen.text = parent.resources.getString(R.string.pass_saturation)
            labelBlue.text = parent.resources.getString(R.string.pass_value)
        } else {
            labelRed.text = parent.resources.getString(R.string.pass_red)
            labelGreen.text = parent.resources.getString(R.string.pass_green)
            labelBlue.text = parent.resources.getString(R.string.pass_blue)
        }
        labelGray.text = parent.resources.getString(R.string.pass_gray)
    }
}