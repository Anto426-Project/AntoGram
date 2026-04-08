package org.telegram.ui.Stories.recorder

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class StoriesQrMlKitScanner {

    data class Detection(
        val link: String,
        val points: Array<PointF>
    )

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    fun detect(bitmap: Bitmap, prefix: String): Detection? {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) {
            return null
        }

        val image = InputImage.fromBitmap(bitmap, 0)
        val barcodes = Tasks.await(scanner.process(image))
        for (barcode in barcodes) {
            var link = barcode.rawValue ?: continue
            link = link.trim()
            if (!link.startsWith(prefix) && !link.startsWith("https://$prefix") && !link.startsWith("http://$prefix")) {
                continue
            }

            val corners = barcode.cornerPoints ?: continue
            if (corners.isEmpty()) {
                continue
            }

            return Detection(
                link = link,
                points = normalizePoints(corners, w, h)
            )
        }

        return null
    }

    private fun normalizePoints(points: Array<Point>, width: Int, height: Int): Array<PointF> {
        return Array(points.size) { index ->
            val point = points[index]
            PointF(point.x / width.toFloat(), point.y / height.toFloat())
        }
    }
}
