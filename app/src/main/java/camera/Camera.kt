package camera


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.os.Bundle
import android.print.PrintAttributes
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.util.Log.d
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.internal.Camera2CameraCaptureResult
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.impl.CameraFilter
import androidx.camera.core.impl.CaptureProcessor
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraView
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.navigation.NavController
import com.example.gettext.MainActivity
import com.example.gettext.R
import com.google.common.util.concurrent.ListenableFuture
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.camera.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class Camera:AppCompatActivity() {
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private lateinit var imageAnalysis: ImageAnalysis
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>


    private  var cropedImage: ByteArray ? = null
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera)
         cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        startCamera()
           /*     fotoapparat = Fotoapparat(
                    context = this,
                    view = layout_camera,
                     scaleType = ScaleType.CenterCrop
                )*/
        val fabOpenAnim = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        val fabCloseAnim = AnimationUtils.loadAnimation(this, R.anim.fab_close)
/*

        fotoapparat.start()
*/

        btn_take_photo.setOnClickListener{
            takePhoto()
            btn_accept.startAnimation(fabOpenAnim)
            btn_decline.startAnimation(fabOpenAnim)
            btn_accept.isVisible = true
            btn_decline.isVisible= true
            CameraX.unbind(preview)
         //  pictureTaken= fotoapparat.takePicture()
           // fotoapparat.stop()
            analizeOnce()




        }

        btn_accept.setOnClickListener{

            val intent = Intent(this,MainActivity::class.java)
            /*
          // val viewSsCroped= cropImage(viewScreenShot!!,layout_camera,layout_camera_small)
            val picture = fotoapparat.takePicture()
            val trans = ResolutionTransformer
            picture.toBitmap()
*/
            intent.putExtra("imageCapture",cropedImage)

            startActivity(intent)

        }
        btn_decline.setOnClickListener{
            btn_accept.startAnimation(fabCloseAnim)
            btn_decline.startAnimation(fabCloseAnim)
            btn_accept.isVisible = false
            btn_decline.isVisible= false
            startCamera()
           // fotoapparat.start()

        }

        if (allPermissionsGranted()) {
           startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        /*outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()*/
    }



    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
           // val aspectRatio = Rational(layout_camera.width,layout_camera.height)
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenAspectRatio = Rational(displayMetrics.widthPixels, displayMetrics.heightPixels)
            val screenSize = Size(displayMetrics.widthPixels,displayMetrics.heightPixels)
        /*     val characteristics: CameraCharacteristics =
                cameraManager.getCameraCharacteristics()
            val supportedPreviewSizes =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(SurfaceTexture::class.java)*/
           d("camerainfo","duapsd")
            imageAnalysis = ImageAnalysis.Builder().apply {

            }.build()


            preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()

            // Select back camera

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
              //  layout_camera.preferredImplementationMode = PreviewView.ImplementationMode.TEXTURE_VIEW
                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture,imageAnalysis)
                preview?.setSurfaceProvider(layout_camera.createSurfaceProvider())

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

private fun analizeOnce(){
    imageAnalysis.setAnalyzer(executor,LuminosityAnalyzer())
}
    private fun takePhoto() {

/*         Get a stable reference of the modifiable image capture use case*/
        val imageCapture = imageCapture ?: return
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), object: ImageCapture
        .OnImageCapturedCallback() {
            @SuppressLint("UnsafeExperimentalUsageError")
            override fun onCaptureSuccess(image: ImageProxy) {
                d("imageinfo",image.imageInfo.toString())


                image.image?.let {
                    val rotationDegrees = image.imageInfo.rotationDegrees
                    val bitmap = it.toBitmap(rotationDegrees)
                    val resizedBitmap = resizeBitmap(bitmap,layout_camera.width,layout_camera.height)
                    cropedImage = cropImage(resizedBitmap,layout_camera,layout_camera_small)
                    super.onCaptureSuccess(image)
                }
              //  val resizedBitmap = resizeBitmap(imageBitmap,layout_camera.width,layout_camera.height)


                 //cropedImage = cropImage(resizedBitmap,layout_camera,layout_camera_small)
                   // cropedImage = imageImage!!.cropRect.set(Rect(left,top,left,top)) as ByteArray


              /* val bundle = Bundle()
                bundle.putByteArray("cropedImage",cropedImage)
                val mainFragment = MainFragment()
                mainFragment.arguments = bundle*/
                image.close()

            }

            override fun onError(exc: ImageCaptureException) {
                cropedImage=null!!
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }
        })
    }
    private fun Image.toBitmap(rotationDegrees: Int): Bitmap {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
/*    fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }*/
private fun resizeBitmap(bitmap:Bitmap, width:Int, height:Int):Bitmap{
    /*
        *** reference source developer.android.com ***
        Bitmap createScaledBitmap (Bitmap src, int dstWidth, int dstHeight, boolean filter)
            Creates a new bitmap, scaled from an existing bitmap, when possible. If the specified
            width and height are the same as the current width and height of the source bitmap,
            the source bitmap is returned and no new bitmap is created.

        Parameters
            src Bitmap : The source bitmap.
                This value must never be null.

        dstWidth int : The new bitmap's desired width.
        dstHeight int : The new bitmap's desired height.
        filter boolean : true if the source should be filtered.

        Returns
            Bitmap : The new scaled bitmap or the source bitmap if no scaling is required.

        Throws
            IllegalArgumentException : if width is <= 0, or height is <= 0
    */
    return Bitmap.createScaledBitmap(
        bitmap,
        width,
        height,
        false
    )
}

    companion object {
        private const val TAG = "CameraXBasic"
      //  private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
               startCamera()

            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    }
    private class LuminosityAnalyzer : ImageAnalysis.Analyzer {

        var isAnalized : Boolean = false

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {

            // Calculate the average luma no more often than every second
            if (!isAnalized) {
                // Since format in ImageAnalysis is YUV, image.planes[0]
                // contains the Y (luminance) plane
                d("imageHeightAnalyzer",image.height.toString())
                d("imageWidthAnalyzer",image.width.toString())
                val buffer = image.planes[0].buffer
                // Extract image data from callback object
                val data = buffer.toByteArray()
                // Convert the data into an array of pixel values
                val pixels = data.map { it.toInt() and 0xFF }
                // Compute average luminance for the image
                val luma = pixels.average()
                // Log the new luma value
                Log.d("CameraXApp", "Average luminosity: $luma")
                // Update timestamp of last analyzed frame
                isAnalized = true
            }
            image.close()
        }
    }


     fun cropImage(bitmap: Bitmap, frame: View, reference: View): ByteArray {

        val heightOriginal = frame.height
        d("frame.height",heightOriginal.toString())
        val widthOriginal = frame.width
        val heightFrame = reference.height
        val widthFrame = reference.width
        val leftFrame = reference.left
        val topFrame = reference.top

        d("heightframe",heightFrame.toString())
        d("widthfram",widthFrame.toString())
        d("heightbiger",heightOriginal.toString())
        d("widthbiger",widthOriginal.toString())

        val heightReal = bitmap.height
        val widthReal = bitmap.width
        d("heightimage",heightReal.toString())
        d("widthimage",widthReal.toString())
        val widthFinal = widthFrame * widthReal / widthOriginal
       val heightFinal = heightFrame * heightReal / heightOriginal
        val leftFinal =( widthReal / 2) - 150
        val topFinal = (heightReal / 2) - 150
         d("widthFinal",widthFinal.toString())
         d("heightFinal",heightFinal.toString())
        d("leftFrame",leftFrame.toString())
        d("leftFinal",leftFinal.toString())
        d("topFrame",topFrame.toString())
        d("topFinal",topFinal.toString())
        d("widthtest",(widthOriginal/2-150).toString())


        val bitmapFinal = Bitmap.createBitmap(
            bitmap,
            leftFinal  , topFinal, 300, 300
        )

        val stream = ByteArrayOutputStream()
        bitmapFinal.compress(
            Bitmap.CompressFormat.PNG,
            100,
            stream
        ) //100 is the best quality possibe
        return stream.toByteArray()
    }
}
