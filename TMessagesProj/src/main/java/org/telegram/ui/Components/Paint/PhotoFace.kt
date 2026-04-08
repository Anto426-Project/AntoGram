package org.telegram.ui.Components.Paint

import android.graphics.Bitmap
import android.graphics.PointF
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import org.telegram.ui.Components.Point
import org.telegram.ui.Components.Size
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class PhotoFace(face: Face, sourceBitmap: Bitmap, targetSize: Size, sideward: Boolean) {

    private var width = 0f
    private var angle = 0f

    private var foreheadPoint: Point? = null
    private var eyesCenterPoint: Point? = null
    private var eyesDistance = 0f
    private var mouthPoint: Point? = null
    private var chinPoint: Point? = null

    init {
        var leftEyePoint: Point? = null
        var rightEyePoint: Point? = null
        var leftMouthPoint: Point? = null
        var rightMouthPoint: Point? = null

        for (landmark in face.allLandmarks) {
            val point = landmark.position
            when (landmark.landmarkType) {
                FaceLandmark.LEFT_EYE -> {
                    leftEyePoint = transposePoint(point, sourceBitmap, targetSize, sideward)
                }

                FaceLandmark.RIGHT_EYE -> {
                    rightEyePoint = transposePoint(point, sourceBitmap, targetSize, sideward)
                }

                FaceLandmark.MOUTH_LEFT -> {
                    leftMouthPoint = transposePoint(point, sourceBitmap, targetSize, sideward)
                }

                FaceLandmark.MOUTH_RIGHT -> {
                    rightMouthPoint = transposePoint(point, sourceBitmap, targetSize, sideward)
                }
            }
        }

        if (leftEyePoint != null && rightEyePoint != null) {
            if (leftEyePoint!!.x < rightEyePoint!!.x) {
                val temp = leftEyePoint
                leftEyePoint = rightEyePoint
                rightEyePoint = temp
            }

            eyesCenterPoint = Point(
                0.5f * leftEyePoint!!.x + 0.5f * rightEyePoint!!.x,
                0.5f * leftEyePoint.y + 0.5f * rightEyePoint.y
            )
            eyesDistance = hypot(
                rightEyePoint.x - leftEyePoint.x,
                rightEyePoint.y - leftEyePoint.y
            )
            angle = Math.toDegrees(
                Math.PI + atan2(
                    rightEyePoint.y - leftEyePoint.y,
                    rightEyePoint.x - leftEyePoint.x
                ).toDouble()
            ).toFloat()

            width = eyesDistance * 2.35f

            val foreheadHeight = 0.8f * eyesDistance
            val upAngle = Math.toRadians((angle - 90).toDouble()).toFloat()
            val center = eyesCenterPoint!!
            foreheadPoint = Point(
                center.x + foreheadHeight * cos(upAngle),
                center.y + foreheadHeight * sin(upAngle)
            )
        }

        if (leftMouthPoint != null && rightMouthPoint != null) {
            if (leftMouthPoint!!.x < rightMouthPoint!!.x) {
                val temp = leftMouthPoint
                leftMouthPoint = rightMouthPoint
                rightMouthPoint = temp
            }
            mouthPoint = Point(
                0.5f * leftMouthPoint!!.x + 0.5f * rightMouthPoint!!.x,
                0.5f * leftMouthPoint.y + 0.5f * rightMouthPoint.y
            )

            val chinDepth = 0.7f * eyesDistance
            val downAngle = Math.toRadians((angle + 90).toDouble()).toFloat()
            val mouth = mouthPoint!!
            chinPoint = Point(
                mouth.x + chinDepth * cos(downAngle),
                mouth.y + chinDepth * sin(downAngle)
            )
        }
    }

    fun isSufficient(): Boolean {
        return eyesCenterPoint != null
    }

    private fun transposePoint(point: PointF, sourceBitmap: Bitmap, targetSize: Size, sideward: Boolean): Point {
        val bitmapW = if (sideward) sourceBitmap.height.toFloat() else sourceBitmap.width.toFloat()
        val bitmapH = if (sideward) sourceBitmap.width.toFloat() else sourceBitmap.height.toFloat()
        return Point(targetSize.width * point.x / bitmapW, targetSize.height * point.y / bitmapH)
    }

    fun getPointForAnchor(anchor: Int): Point? {
        return when (anchor) {
            0 -> foreheadPoint
            1 -> eyesCenterPoint
            2 -> mouthPoint
            3 -> chinPoint
            else -> null
        }
    }

    fun getWidthForAnchor(anchor: Int): Float {
        return if (anchor == 1) eyesDistance else width
    }

    fun getAngle(): Float {
        return angle
    }
}
