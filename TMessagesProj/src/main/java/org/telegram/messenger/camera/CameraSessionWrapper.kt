package org.telegram.messenger.camera

import androidx.annotation.Nullable
import org.telegram.messenger.AndroidUtilities
import java.util.concurrent.CountDownLatch

class CameraSessionWrapper {

    @JvmField
    var camera1Session: CameraSession? = null

    @JvmField
    var camera2Session: Camera2Session? = null

    fun isInitiated(): Boolean {
        return when {
            camera2Session != null -> camera2Session!!.isInitiated()
            camera1Session != null -> camera1Session!!.isInitied()
            else -> false
        }
    }

    fun getWorldAngle(): Int {
        return when {
            camera2Session != null -> camera2Session!!.getWorldAngle()
            camera1Session != null -> camera1Session!!.getWorldAngle()
            else -> 0
        }
    }

    fun getCurrentOrientation(): Int {
        return when {
            camera2Session != null -> camera2Session!!.getCurrentOrientation()
            camera1Session != null -> camera1Session!!.getCurrentOrientation()
            else -> 0
        }
    }

    fun getDisplayOrientation(): Int {
        return when {
            camera2Session != null -> camera2Session!!.getDisplayOrientation()
            camera1Session != null -> camera1Session!!.getDisplayOrientation()
            else -> 0
        }
    }

    @Deprecated("")
    fun getCameraId(): Int {
        return when {
            camera2Session != null -> camera2Session!!.cameraId.hashCode()
            camera1Session != null -> camera1Session!!.cameraInfo.cameraId
            else -> 0
        }
    }

    fun stopVideoRecording() {
        if (camera2Session != null) {
            camera2Session!!.setRecordingVideo(false)
        } else if (camera1Session != null) {
            camera1Session!!.stopVideoRecording()
        }
    }

    fun setOptimizeForBarcode(optimize: Boolean) {
        if (camera2Session != null) {
            camera2Session!!.setScanningBarcode(optimize)
        } else if (camera1Session != null) {
            camera1Session!!.setOptimizeForBarcode(optimize)
        }
    }

    fun setCurrentFlashMode(flashMode: String?) {
        if (camera2Session != null) {
            camera2Session!!.setFlashMode(flashMode)
        } else if (camera1Session != null) {
            camera1Session!!.setCurrentFlashMode(flashMode)
        }
    }

    fun getCurrentFlashMode(): String? {
        return if (camera2Session != null) {
            camera2Session!!.getFlashMode()
        } else if (camera1Session != null) {
            camera1Session!!.getCurrentFlashMode()
        } else {
            null
        }
    }

    fun getNextFlashMode(): String? {
        return if (camera2Session != null) {
            camera2Session!!.getNextFlashMode()
        } else if (camera1Session != null) {
            camera1Session!!.getNextFlashMode()
        } else {
            null
        }
    }

    fun hasFlashModes(): Boolean {
        return if (camera2Session != null) {
            camera2Session!!.hasFlashModes()
        } else if (camera1Session != null) {
            camera1Session!!.availableFlashModes.isNotEmpty()
        } else {
            false
        }
    }

    fun setFlipFront(flip: Boolean) {
        if (camera2Session != null) {
            camera2Session!!.setFlipFront(flip)
        } else if (camera1Session != null) {
            camera1Session!!.setFlipFront(flip)
        }
    }

    fun isSameTakePictureOrientation(): Boolean {
        return if (camera2Session != null) {
            camera2Session!!.isSameTakePictureOrientation()
        } else if (camera1Session != null) {
            camera1Session!!.isSameTakePictureOrientation()
        } else {
            true
        }
    }

    fun updateRotation() {
        if (camera2Session != null) {
            camera2Session!!.updateRotation()
        } else if (camera1Session != null) {
            camera1Session!!.updateRotation()
        }
    }

    fun setZoom(zoom: Float) {
        if (camera2Session != null) {
            val session = camera2Session!!
            session.setZoom(AndroidUtilities.lerp(session.getMinZoom(), session.getMaxZoom(), zoom))
        } else if (camera1Session != null) {
            camera1Session!!.setZoom(zoom)
        }
    }

    fun focusToRect(focusRect: android.graphics.Rect?, meteringRect: android.graphics.Rect?) {
        if (camera2Session != null) {
            camera2Session!!.focusToRect(focusRect, meteringRect)
        } else if (camera1Session != null) {
            camera1Session!!.focusToRect(focusRect, meteringRect)
        }
    }

    fun destroy(async: Boolean, before: Runnable?, after: Runnable?) {
        if (camera2Session != null) {
            before?.run()
            camera2Session!!.destroy(async, after)
        } else if (camera1Session != null) {
            CameraController.getInstance().close(camera1Session, if (!async) CountDownLatch(1) else null, before, after)
        }
    }

    fun getObject(): Any? {
        return when {
            camera2Session != null -> camera2Session
            camera1Session != null -> camera1Session
            else -> null
        }
    }

    override fun equals(@Nullable obj: Any?): Boolean {
        return when (obj) {
            is CameraSession -> obj === camera1Session
            is Camera2Session -> obj === camera2Session
            is CameraSessionWrapper -> obj === this || (obj.camera1Session === camera1Session && obj.camera2Session === camera2Session)
            else -> false
        }
    }

    companion object {
        @JvmStatic
        fun of(session: CameraSession?): CameraSessionWrapper {
            val wrapper = CameraSessionWrapper()
            wrapper.camera1Session = session
            return wrapper
        }

        @JvmStatic
        fun of(session: Camera2Session?): CameraSessionWrapper {
            val wrapper = CameraSessionWrapper()
            wrapper.camera2Session = session
            return wrapper
        }
    }
}
