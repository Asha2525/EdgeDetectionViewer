package com.ashasuresh.edgedetectionviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.sqrt

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
            return try {
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
                yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 50, out)
                val imageBytes = out.toByteArray()

                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting image: ${e.message}")
                null
            }
        }

        private fun detectEdges(bitmap: Bitmap): Bitmap {
            val width = bitmap.width
            val height = bitmap.height

            // Create grayscale array
            val grayscale = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = bitmap.getPixel(x, y)
                    val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                    grayscale[y * width + x] = gray
                }
            }

            // Apply Sobel edge detection
            val edges = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (y in 1 until height - 1) {
                for (x in 1 until width - 1) {
                    // Sobel kernels
                    val gx = (
                            -grayscale[(y-1) * width + (x-1)] + grayscale[(y-1) * width + (x+1)] +
                                    -2 * grayscale[y * width + (x-1)] + 2 * grayscale[y * width + (x+1)] +
                                    -grayscale[(y+1) * width + (x-1)] + grayscale[(y+1) * width + (x+1)]
                            )

                    val gy = (
                            -grayscale[(y-1) * width + (x-1)] - 2 * grayscale[(y-1) * width + x] - grayscale[(y-1) * width + (x+1)] +
                                    grayscale[(y+1) * width + (x-1)] + 2 * grayscale[(y+1) * width + x] + grayscale[(y+1) * width + (x+1)]
                            )

                    val magnitude = sqrt((gx * gx + gy * gy).toDouble()).toInt()
                    val edgeValue = magnitude.coerceIn(0, 255)

                    edges.setPixel(x, y, Color.rgb(edgeValue, edgeValue, edgeValue))
                }
            }

            return edges
        }
    }
}