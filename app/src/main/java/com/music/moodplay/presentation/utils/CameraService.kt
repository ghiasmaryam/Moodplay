package com.music.moodplay.presentation.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraAccessException
import com.music.moodplay.presentation.activities.MainActivity
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.view.Surface
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*

// constructor takes two parameters:
class CameraService(
    private val activity: Activity,
    private val pulseRateHandler: PulseRateHandler
) {

    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var previewSession: CameraCaptureSession? = null
    private var previewCaptureRequestBuilder: CaptureRequest.Builder? = null

    fun start(previewSurface: Surface) {
        val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = Objects.requireNonNull(cameraManager).cameraIdList[0]
        } catch (e: CameraAccessException) {
            Log.e("camera", "No access to camera", e)
            pulseRateHandler.updatingData(
                StaticFields.MESSAGE_CAMERA_NOT_AVAILABLE,
                "No access to camera....",
                0.0F
            )
        } catch (e: NullPointerException) {
            Log.e("camera", "No access to camera", e)
            pulseRateHandler.updatingData(

                StaticFields.MESSAGE_CAMERA_NOT_AVAILABLE,
                "No access to camera....",
                0.0f
            )
        } catch (e: ArrayIndexOutOfBoundsException) {
            Log.e("camera", "No access to camera", e)
            pulseRateHandler.updatingData(
                StaticFields.MESSAGE_CAMERA_NOT_AVAILABLE,
                "No access to camera....", 0.0F
            )
        }
        try {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.println(Log.ERROR, "camera", "No permission to take photos")
                pulseRateHandler.updatingData(
                    StaticFields.MESSAGE_CAMERA_NOT_AVAILABLE,
                    "No permission to take photos",
                    0.0F
                )
                return
            }

            // message has been sent to MainActivity, this method can return.
            if (cameraId == null) {
                return
            } //camera capturing session
            Objects.requireNonNull(cameraManager)
                .openCamera(cameraId!!, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraDevice = camera
                        val stateCallback: CameraCaptureSession.StateCallback =
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: CameraCaptureSession) {
                                    previewSession = session
                                    try {
                                        previewCaptureRequestBuilder =
                                            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                        previewCaptureRequestBuilder!!.addTarget(previewSurface) // this is previewSurface
                                        previewCaptureRequestBuilder!!.set(
                                            CaptureRequest.FLASH_MODE,
                                            CaptureRequest.FLASH_MODE_TORCH
                                        )
                                        val thread = HandlerThread("CameraPreview")
                                        thread.start()
                                        previewSession!!.setRepeatingRequest(
                                            previewCaptureRequestBuilder!!.build(),
                                            null,
                                            null
                                        )
                                    } catch (e: CameraAccessException) {
                                        if (e.message != null) {
                                            Log.println(Log.ERROR, "camera", e.message!!)
                                        }
                                    }
                                }

                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                    Log.println(Log.ERROR, "camera", "Session configuration failed")
                                }
                            }
                        try {

                            camera.createCaptureSession(
                                listOf(previewSurface),
                                stateCallback,
                                null
                            ) //1
                        } catch (e: CameraAccessException) {
                            if (e.message != null) {
                                Log.println(Log.ERROR, "camera", e.message!!)
                            }
                        }
                    }

                    override fun onDisconnected(camera: CameraDevice) {}
                    override fun onError(camera: CameraDevice, error: Int) {}
                }, null)
        } catch (e: CameraAccessException) {
            if (e.message != null) {
                Log.println(Log.ERROR, "camera", e.message!!)
                pulseRateHandler.updatingData(
                    StaticFields.MESSAGE_CAMERA_NOT_AVAILABLE,
                    e.message.toString(),
                    0.0F

                )
            }
        } catch (e: SecurityException) {
            if (e.message != null) {
                Log.println(Log.ERROR, "camera", e.message!!)
                pulseRateHandler.updatingData(
                    StaticFields.MESSAGE_CAMERA_NOT_AVAILABLE,
                    e.message.toString(),
                    0.0f
                )
            }
        }
    }

    fun stop() {
        try {
            cameraDevice!!.close()
        } catch (e: Exception) {
            Log.println(Log.ERROR, "camera", "cannot close camera device" + e.message)
        }
    }
}