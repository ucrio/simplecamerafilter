package io.github.ucrio.simplecamerafilter.filters

import android.graphics.Bitmap
import android.widget.LinearLayout
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class Inverter (name: String): AbsFilter(name) {

    override fun doFilter(src: Bitmap): Bitmap {

        val mat = Mat(src.height, src.width, CvType.CV_8UC3)
        Utils.bitmapToMat(src, mat)

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB)
        Core.bitwise_not(mat, mat)

        val ret = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, ret)

        return ret
    }

    override fun initialize(parent: LinearLayout) {
        // Do Nothing
    }
}