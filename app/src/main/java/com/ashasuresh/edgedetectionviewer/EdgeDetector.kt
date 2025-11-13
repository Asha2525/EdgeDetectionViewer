package com.ashasuresh.edgedetectionviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class EdgeDetector {

    companion object {
        private const val TAG = "EdgeDetector"

        fun processFrame(imageProxy: ImageProxy): Bitmap? {
            return try {
                // Convert ImageProxy to Bitmap
                val bitmap = imageProxyToBitmap(imageProxy)

                if (bitmap != null) {
                    // Apply edge detection
                    detectEdges(bitmap)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing frame: ${e.message}")
                null
            }
        }

        private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
            val yBuffer = imageProxy.planes[0].buffer
            val uBuffer = imageProxy.planes[1].buffer
            val vBuffer = imageProxy.planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
            val imageBytes = out.toByteArray()

            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }

        private fun detectEdges(bitmap: Bitmap): Bitmap {
            // Convert Bitmap to OpenCV Mat
            val src = Mat()
            Utils.bitmapToMat(bitmap, src)

            // Convert to grayscale
            val gray = Mat()
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY)

            // Apply Gaussian Blur to reduce noise
            val blurred = Mat()
            Imgproc.GaussianBlur(gray, blurred, org.opencv.core.Size(5.0, 5.0), 0.0)

            // Apply Canny Edge Detection
            val edges = Mat()
            Imgproc.Canny(blurred, edges, 50.0, 150.0)

            // Convert back to Bitmap
            val resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888)
            Imgproc.cvtColor(edges, edges, Imgproc.COLOR_GRAY2RGBA)
            Utils.matToBitmap(edges, resultBitmap)

            // Release resources
            src.release()
            gray.release()
            blurred.release()
            edges.release()

            return resultBitmap
        }
    }
}