/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import java.util.ArrayList

class CameraInfo(id: Int, frontFace: Int) {

    @JvmField
    var cameraId: Int = id

    @JvmField
    val pictureSizes: ArrayList<Size> = ArrayList()

    @JvmField
    val previewSizes: ArrayList<Size> = ArrayList()

    @JvmField
    val frontCamera: Int = frontFace

    @JvmField
    protected var cameraDevice: CameraDevice? = null

    @JvmField
    var cameraCharacteristics: CameraCharacteristics? = null

    @JvmField
    var captureRequestBuilder: CaptureRequest.Builder? = null

    @JvmField
    var cameraCaptureSession: CameraCaptureSession? = null

    fun getCameraId(): Int {
        return cameraId
    }

    fun getPreviewSizes(): ArrayList<Size> {
        return previewSizes
    }

    fun getPictureSizes(): ArrayList<Size> {
        return pictureSizes
    }

    fun isFrontface(): Boolean {
        return frontCamera != 0
    }
}
