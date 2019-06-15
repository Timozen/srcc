package com.srccproject.srcc_camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    /** Helper to ask camera permission.  */
    object CameraPermissionHelper {
        private const val CAMERA_PERMISSION_CODE = 0
        private const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA

        /** Check to see we have the necessary permissions for this app.  */
        fun hasCameraPermission(activity: Activity): Boolean {
            return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }

        /** Check to see we have the necessary permissions for this app, and ask for them if we don't.  */
        fun requestCameraPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_CODE)
        }

        /** Check to see if we need to show the rationale for this permission.  */
        fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION)
        }

        /** Launch Application Setting to grant permission.  */
        fun launchPermissionSettings(activity: Activity) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }

        recreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }

        surfaceView.holder.addCallback(surfaceReadyCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startCameraSession() {

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        if (cameraManager.cameraIdList.isEmpty()) {
            // no cameras
            return
        }

        val firstCamera = cameraManager.cameraIdList[0]

        cameraManager.openCamera(firstCamera, object: CameraDevice.StateCallback() {
            override fun onDisconnected(p0: CameraDevice) { }
            override fun onError(p0: CameraDevice, p1: Int) { }

            override fun onOpened(cameraDevice: CameraDevice) {
                // use the camera
                val cameraCharacteristics =    cameraManager.getCameraCharacteristics(cameraDevice.id)

                cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let { streamConfigurationMap ->
                    streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)?.let { yuvSizes ->
                        val previewSize = yuvSizes.last()
// cont.
                        val displayRotation = windowManager.defaultDisplay.rotation

                        val swappedDimensions = areDimensionsSwapped(displayRotation, cameraCharacteristics)

// swap width and height if needed
                        val rotatedPreviewWidth = if (swappedDimensions) previewSize.height else previewSize.width
                        val rotatedPreviewHeight = if (swappedDimensions) previewSize.width else previewSize.height

                        surfaceView.holder.setFixedSize(rotatedPreviewWidth, rotatedPreviewHeight)

                        val previewSurface = surfaceView.holder.surface

                        val captureCallback = object : CameraCaptureSession.StateCallback()
                        {
                            override fun onConfigureFailed(session: CameraCaptureSession) {}

                            override fun onConfigured(session: CameraCaptureSession) {
                                // session configured
                                val previewRequestBuilder =   cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                    .apply {
                                        addTarget(previewSurface)
                                    }

                                session.setRepeatingRequest(
                                    previewRequestBuilder.build(),
                                    object: CameraCaptureSession.CaptureCallback() {},
                                    Handler { true }
                                )
                            }
                        }


                        cameraDevice.createCaptureSession(mutableListOf(previewSurface), captureCallback, Handler { true })
                    }

                }
            }
        }, Handler { true })


    }

    val surfaceReadyCallback = object: SurfaceHolder.Callback {
        override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) { }
        override fun surfaceDestroyed(p0: SurfaceHolder?) { }

        override fun surfaceCreated(p0: SurfaceHolder?) {
            startCameraSession()
        }
    }

    private fun areDimensionsSwapped(displayRotation: Int, cameraCharacteristics: CameraCharacteristics): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 90 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 0 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                // invalid display rotation
            }
        }
        return swappedDimensions
    }
}
