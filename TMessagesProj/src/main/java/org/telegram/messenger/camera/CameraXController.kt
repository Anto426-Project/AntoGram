package org.telegram.messenger.camera

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.telegram.messenger.FileLog
import java.util.concurrent.atomic.AtomicBoolean

object CameraXController {

    private val warming = AtomicBoolean(false)

    interface QrListener {
        fun onQrDetected(result: DetectionResult)
        fun onError(error: Throwable)
    }

    data class DetectionResult(
        val text: String,
        val bounds: RectF?,
        val cornerPoints: Array<PointF>?
    )

    @JvmStatic
    fun warmUp(context: Context?) {
        if (context == null || !warming.compareAndSet(false, true)) {
            return
        }

        try {
            val appContext = context.applicationContext
            val future = ProcessCameraProvider.getInstance(appContext)
            future.addListener(
                {
                    try {
                        future.get()
                    } catch (t: Throwable) {
                        FileLog.e(t)
                    } finally {
                        warming.set(false)
                    }
                },
                ContextCompat.getMainExecutor(appContext)
            )
        } catch (t: Throwable) {
            warming.set(false)
            FileLog.e(t)
        }
    }

    @JvmStatic
    fun createQrSession(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        listener: QrListener
    ): QrSession {
        return QrSession(context.applicationContext, lifecycleOwner, previewView, listener)
    }

    class QrSession internal constructor(
        private val context: Context,
        private val lifecycleOwner: LifecycleOwner,
        private val previewView: PreviewView,
        private val listener: QrListener
    ) {
        private var cameraProvider: ProcessCameraProvider? = null
        private var imageAnalysis: ImageAnalysis? = null
        private var boundCamera: Camera? = null

        private val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        fun start() {
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener(
                {
                    try {
                        val provider = providerFuture.get()
                        cameraProvider = provider
                        bindUseCases(provider)
                    } catch (t: Throwable) {
                        listener.onError(t)
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        }

        fun stop() {
            try {
                imageAnalysis?.clearAnalyzer()
                cameraProvider?.unbindAll()
            } catch (_: Throwable) {
            }
            boundCamera = null
            imageAnalysis = null
        }

        fun setTorchEnabled(enabled: Boolean): Boolean {
            val camera = boundCamera ?: return false
            return try {
                if (!camera.cameraInfo.hasFlashUnit()) {
                    return false
                }
                camera.cameraControl.enableTorch(enabled)
                true
            } catch (t: Throwable) {
                listener.onError(t)
                false
            }
        }

        private fun bindUseCases(provider: ProcessCameraProvider) {
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    imageProxy.close()
                    return@setAnalyzer
                }

                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val first = barcodes.firstOrNull { !it.rawValue.isNullOrEmpty() }
                        if (first != null) {
                            listener.onQrDetected(
                                DetectionResult(
                                    text = first.rawValue.orEmpty(),
                                    bounds = normalizeBounds(first.boundingBox, imageProxy.width, imageProxy.height),
                                    cornerPoints = normalizePoints(first.cornerPoints, imageProxy.width, imageProxy.height)
                                )
                            )
                        }
                    }
                    .addOnFailureListener { listener.onError(it) }
                    .addOnCompleteListener { imageProxy.close() }
            }

            imageAnalysis = analysis
            provider.unbindAll()
            boundCamera = provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )
        }

        private fun normalizeBounds(bounds: Rect?, width: Int, height: Int): RectF? {
            if (bounds == null || width <= 0 || height <= 0) {
                return null
            }
            return RectF(
                bounds.left / width.toFloat(),
                bounds.top / height.toFloat(),
                bounds.right / width.toFloat(),
                bounds.bottom / height.toFloat()
            )
        }

        private fun normalizePoints(points: Array<Point>?, width: Int, height: Int): Array<PointF>? {
            if (points == null || points.isEmpty() || width <= 0 || height <= 0) {
                return null
            }
            return Array(points.size) { index ->
                val point = points[index]
                PointF(point.x / width.toFloat(), point.y / height.toFloat())
            }
        }
    }
}
