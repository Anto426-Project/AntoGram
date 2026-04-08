package org.telegram.ui

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import org.telegram.messenger.camera.CameraXController

class CameraXQrScanner(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val listener: Listener
) {

    data class DetectionResult(
        val text: String,
        val bounds: RectF?,
        val cornerPoints: Array<PointF>?
    )

    interface Listener {
        fun onQrDetected(result: DetectionResult)
        fun onError(error: Throwable)
    }

    private val session = CameraXController.createQrSession(
        context = context,
        lifecycleOwner = lifecycleOwner,
        previewView = previewView,
        listener = object : CameraXController.QrListener {
            override fun onQrDetected(result: CameraXController.DetectionResult) {
                listener.onQrDetected(
                    DetectionResult(
                        text = result.text,
                        bounds = result.bounds,
                        cornerPoints = result.cornerPoints
                    )
                )
            }

            override fun onError(error: Throwable) {
                listener.onError(error)
            }
        }
    )

    fun start() {
        session.start()
    }

    fun stop() {
        session.stop()
    }

    fun setTorchEnabled(enabled: Boolean): Boolean {
        return session.setTorchEnabled(enabled)
    }
}
