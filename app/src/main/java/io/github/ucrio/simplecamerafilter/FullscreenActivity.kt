package io.github.ucrio.simplecamerafilter

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.android.synthetic.main.activity_fullscreen.*
import kotlinx.android.synthetic.main.camera_view.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class FullscreenActivity : AppCompatActivity() {

    val DESIRED_PREVIEW_SIZE: Size = Size(1080, 1440)

    private val PERMISSIONS_REQUEST = 1

    private lateinit var shutterButton : Button
    private lateinit var filterHandler: ImageFilterHandler


    var sdf = SimpleDateFormat("yyyyMMdd_hhmmssSSS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        setContentView(R.layout.activity_fullscreen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        MobileAds.initialize(this)
        val requestCfg = RequestConfiguration.Builder()
            .setTestDeviceIds(Arrays.asList(getString(R.string.test_device)))
            .build()
        MobileAds.setRequestConfiguration(requestCfg)

        // set ad
        val adView = findViewById<AdView>(R.id.ad_camera)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        filterHandler = ImageFilterHandler(this)
        spinnerFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                val selected = parent.getItemAtPosition(position) as String
                filterHandler.changeFilter(selected)

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val adaptor = ArrayAdapter<String>(this, R.layout.spinner, resources.getStringArray(R.array.filter_array))
        adaptor.setDropDownViewResource(R.layout.spinner_dropdown)
        spinnerFilter.adapter = adaptor


        shutterButton = findViewById(R.id.Shutter);

        shutterButton.setOnClickListener {
            val bmp = (imageViewMain.drawable as BitmapDrawable).bitmap
            saveBitmap(bmp, sdf.format(Calendar.getInstance().time), contentResolver)
        }

        textPrivacyPolicy.text = HtmlCompat.fromHtml("<a href=\"https://ucrio.github.io/simplecamerafilter/\">Privacy Policy</a>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        textPrivacyPolicy.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun startCamera() {
        val aspectRatio = calcAspectRatio()
        texture.layoutParams.width = texture.width
        texture.layoutParams.height = (texture.width * aspectRatio[1].toFloat()/aspectRatio[0].toFloat()).toInt()

        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(aspectRatio[0], aspectRatio[1]))
            //setTargetResolution(Size(texture.width, texture.height))
        }.build()

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            val container = findViewById<ConstraintLayout>(R.id.container)
            container.removeView(texture)
            texture.setSurfaceTexture(it.surfaceTexture)
            container.addView(texture)
            //updateTransform()
        }

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            val analyzerThread = HandlerThread(
                "ImageFilterHandler"
            ).apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE
            )
        }.build()

        val imageAnalysis = ImageAnalysis(analyzerConfig).apply {
            analyzer = filterHandler
        }

        //ライフサイクルにbindさせる
        CameraX.bindToLifecycle(this, preview, imageAnalysis)
        //CameraX.bindToLifecycle(this, preview)

    }

    private fun updateTransform() {
        val matrix = Matrix()

        val centerX = texture.width / 2f
        val centerY = texture.height / 2f

        val rotationDegrees = when (texture.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        texture.setTransform(matrix)
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
    }

    private fun saveBitmap(bitmap: Bitmap, filename: String, contentResolver: ContentResolver): Boolean {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        Log.i("SimpleCameraFilter", "Saving %dx%d bitmap to %s.".format(bitmap.getWidth(), bitmap.getHeight(), root));
        val dir = File(root);

        if (!dir.mkdirs()) {
            Log.i("SimpleCameraFilter", "Make dir failed");
        }

        val fname = filename + ".jpg";
        val file = File(dir, fname);
        if (file.exists()) {
            // recursive: save with another name and return
            return saveBitmap(bitmap, filename + "_01", contentResolver);
        }
        try {
            val out = FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            Toast.makeText(this, "Saved: " + fname, Toast.LENGTH_SHORT).show()

            val contentValues = ContentValues();
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put("_data", file.getAbsolutePath());

            contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            return true;
        } catch (e: Exception) {
            Log.e("SimpleCameraFilter", "Failed to save image to %s.".format(root), e);
            return false;
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(
                    this,
                    "Camera permission is required for this app",
                    Toast.LENGTH_LONG
                ).show()
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(
                    this,
                    "Storage access permission is required for this app",
                    Toast.LENGTH_LONG
                ).show()
            }
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), PERMISSIONS_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST) {
            if (allPermissionsGranted(grantResults)) {
                texture.post{ startCamera() }
            } else {
                finish()
            }
        }
    }

    private fun allPermissionsGranted(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }

    private fun calcAspectRatio(): List<Int> {
        val width = texture.width
        val height = texture.height
        Log.d("SimpleCameraFilter", String.format("%d, %d", width, height))

        val ratio = height.toFloat()/width.toFloat()
        if (ratio >= 16f/9f) {
            Log.d("SimpleCameraFilter", "16:9")
            return listOf(9, 16)
        } else {
            Log.d("SimpleCameraFilter", "4:3")
            return listOf(3, 4)
        }

    }

    private fun getDisplaySize(): List<Int> {

        var width = 0
        var height = 0
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = this.windowManager.currentWindowMetrics
            width = windowMetrics.bounds.width();
            height = windowMetrics.bounds.height()
        } else {
            val wm = this.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
            val disp = wm.defaultDisplay
            val point = Point()
            disp.getRealSize(point)
            width = point.x
            height = point.y
        }

        return listOf(width, height)
    }
}
