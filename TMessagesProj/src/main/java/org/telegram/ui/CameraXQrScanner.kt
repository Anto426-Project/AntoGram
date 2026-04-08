package org.telegram.ui

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
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

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    fun start() {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                cameraProvider = providerFuture.get()
                bindUseCases()
            } catch (t: Throwable) {
                listener.onError(t)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stop() {
        try {
            imageAnalysis?.clearAnalyzer()
            cameraProvider?.unbindAll()
        } catch (_: Throwable) {
        }
        imageAnalysis = null
    }

    private fun bindUseCases() {
        val provider = cameraProvider ?: return

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
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
            }

        provider.unbindAll()
        provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
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
