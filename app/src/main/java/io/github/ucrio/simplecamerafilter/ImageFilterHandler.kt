package io.github.ucrio.simplecamerafilter

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import android.view.TextureView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.constraintlayout.widget.ConstraintLayout
import io.github.ucrio.simplecamerafilter.filters.*
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class ImageFilterHandler(val activity: Activity): ImageAnalysis.Analyzer {

    private var isInitialized = false

    private val noneFilter = NoneFilter(activity.resources.getStringArray(R.array.filter_array)[0])
    private val rgbFilter = RgbFilter(activity.resources.getStringArray(R.array.filter_array)[1])
    private val passFilter = PassFilter(activity.resources.getStringArray(R.array.filter_array)[2])
    private val brightnessFilter = BrightnessFilter(activity.resources.getStringArray(R.array.filter_array)[3])
    private val inverter = Inverter(activity.resources.getStringArray(R.array.filter_array)[4])

    var currentfilter: AbsFilter = noneFilter
    val textureView = this.activity.findViewById<TextureView>(R.id.texture)
    val imageViewMain = this.activity.findViewById<ImageView>(R.id.imageViewMain)

    fun changeFilter(filter: String) {
        if (this.currentfilter.name == filter) {
            return
        }

        val parent = this.activity.findViewById<LinearLayout>(R.id.filterParent)
        val view = parent.findViewById<LinearLayout>(R.id.filterViews)
        if (view != null) {
            parent.removeView(view)
        }

        when(filter) {
            noneFilter.name -> {
                this.currentfilter = noneFilter()
            }
            rgbFilter.name -> {
                activity.layoutInflater.inflate(R.layout.filter_rgb, parent)
                this.currentfilter = rgbFilter()
            }
            passFilter.name -> {
                activity.layoutInflater.inflate(R.layout.filter_pass, parent)
                this.currentfilter = passFilter()
            }
            brightnessFilter.name -> {
                activity.layoutInflater.inflate(R.layout.filter_brightness, parent)
                this.currentfilter = brightnessFilter()
            }
            inverter.name -> {
                activity.layoutInflater.inflate(R.layout.filter_inverter, parent)
                this.currentfilter = inverter()
            }
        }
        this.currentfilter.initialize(parent)
    }

    private fun noneFilter(): NoneFilter {
        return this.noneFilter
    }
    private fun rgbFilter(): RgbFilter {
        return this.rgbFilter
    }

    private fun passFilter(): PassFilter {
        return this.passFilter
    }

    private fun brightnessFilter(): BrightnessFilter {
        return this.brightnessFilter
    }

    private fun inverter(): Inverter {
        return this.inverter
    }

    fun doFilter(src: Bitmap): Bitmap {
        try {
            return currentfilter.doFilter(src)
        } catch (e: Exception) {
            Log.e("ImageFilterHandler", "Exception occurred", e)
            return src
        }
    }

    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {

        if (!isInitialized) {
            this.activity.runOnUiThread {
                imageViewMain.layoutParams.width = textureView.width
                imageViewMain.layoutParams.height = textureView.height

                val parent = imageViewMain.parent as ConstraintLayout
                parent.removeView(imageViewMain)
                parent.addView(imageViewMain)
            }

            isInitialized = OpenCVLoader.initDebug()
            return
        }

        val bmp = textureView.bitmap
        if (bmp != null) {
            val filtered = doFilter(bmp)
            this.activity.runOnUiThread {
                imageViewMain.setImageBitmap(filtered)
            }
        }

    }

    private fun imageToMat(image: ImageProxy): Mat {
        val y = image.planes[0].buffer
        val u = image.planes[1].buffer
        val v = image.planes[2].buffer

        val ySize = y.remaining()
        val uSize = u.remaining()
        val vSize = v.remaining()

        val data = ByteArray(ySize + uSize + vSize)
        y.get(data, 0, ySize)
        v.get(data, ySize, vSize)
        u.get(data, ySize+vSize, uSize)

        val yuvMat = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
        yuvMat.put(0,0, data)
        val result = Mat()
        Imgproc.cvtColor(yuvMat, result, Imgproc.COLOR_YUV2BGR_NV21, 3)
        Core.rotate(result, result, Core.ROTATE_90_CLOCKWISE)

        return result
    }
}