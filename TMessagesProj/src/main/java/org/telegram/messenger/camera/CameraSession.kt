package org.telegram.messenger.camera

import android.app.Activity
import android.graphics.Rect
import android.media.MediaRecorder
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.Utilities
import java.util.ArrayList

class CameraSession(
    @JvmField var cameraInfo: CameraInfo,
    private val previewSize: Size,
    private val pictureSize: Size,
    private val pictureFormat: Int,
    private val isRound: Boolean
) {

    private var currentFlashMode: String
    private var initied = false
    private var currentOrientation = 0
    private var worldAngle = 0
    private var sameTakePictureOrientation = true
    private var flipFront = true
    private var currentZoom = 0f
    private var destroyed = false
    private var useTorch = false

    @JvmField
    var availableFlashModes: ArrayList<String> = arrayListOf(
        FLASH_MODE_OFF,
        FLASH_MODE_AUTO,
        FLASH_MODE_ON
    )

    init {
        val sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("camera", Activity.MODE_PRIVATE)
        currentFlashMode = sharedPreferences.getString(
            if (cameraInfo.frontCamera != 0) "flashMode_front" else "flashMode",
            FLASH_MODE_OFF
        ) ?: FLASH_MODE_OFF
    }

    companion object {
        const val FLASH_MODE_OFF = "off"
        const val FLASH_MODE_ON = "on"
        const val FLASH_MODE_AUTO = "auto"
        const val FLASH_MODE_TORCH = "torch"
    }

    fun setOptimizeForBarcode(value: Boolean) {
        // Camera1 path removed.
    }

    fun checkFlashMode(mode: String?) {
        if (!availableFlashModes.contains(currentFlashMode)) {
            setCurrentFlashMode(mode)
        }
    }

    fun setCurrentFlashMode(mode: String?) {
        currentFlashMode = mode ?: FLASH_MODE_OFF
        val sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("camera", Activity.MODE_PRIVATE)
        sharedPreferences.edit().putString(
            if (cameraInfo.frontCamera != 0) "flashMode_front" else "flashMode",
            currentFlashMode
        ).apply()
    }

    fun setTorchEnabled(enabled: Boolean) {
        useTorch = enabled
        currentFlashMode = if (enabled) FLASH_MODE_TORCH else FLASH_MODE_OFF
    }

    fun getCurrentFlashMode(): String {
        return currentFlashMode
    }

    fun getNextFlashMode(): String {
        for (a in availableFlashModes.indices) {
            val mode = availableFlashModes[a]
            if (mode == currentFlashMode) {
                return if (a < availableFlashModes.size - 1) availableFlashModes[a + 1] else availableFlashModes[0]
            }
        }
        return currentFlashMode
    }

    fun setInitied() {
        initied = true
    }

    fun isInitied(): Boolean {
        return initied
    }

    fun getCurrentOrientation(): Int {
        return currentOrientation
    }

    fun isFlipFront(): Boolean {
        return flipFront
    }

    fun setFlipFront(value: Boolean) {
        flipFront = value
    }

    fun getWorldAngle(): Int {
        return worldAngle
    }

    fun isSameTakePictureOrientation(): Boolean {
        return sameTakePictureOrientation
    }

    fun configureRoundCamera(initial: Boolean): Boolean {
        return true
    }

    fun updateRotation() {
        // Camera1 path removed.
    }

    fun configurePhotoCamera() {
        // Camera1 path removed.
    }

    fun focusToRect(focusRect: Rect?, meteringRect: Rect?) {
        // Camera1 path removed.
    }

    fun setZoom(value: Float) {
        currentZoom = Utilities.clamp(value, 1f, 0f)
    }

    fun onStartRecord() {
        // Camera1 path removed.
    }

    fun stopVideoRecording() {
        if (useTorch) {
            useTorch = false
            currentFlashMode = FLASH_MODE_OFF
        }
    }

    fun configureRecorder(quality: Int, recorder: MediaRecorder) {
        // Camera1 path removed.
    }

    fun getDisplayOrientation(): Int {
        return currentOrientation
    }

    fun destroy() {
        initied = false
        destroyed = true
    }

    fun getCurrentPreviewSize(): Size {
        return previewSize
    }

    fun getCurrentPictureSize(): Size {
        return pictureSize
    }
}

