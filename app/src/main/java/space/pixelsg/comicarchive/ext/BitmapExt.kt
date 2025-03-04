package space.pixelsg.comicarchive.ext

import android.graphics.Bitmap

fun Bitmap.scaleToMaxWidthOrHeight(maxSideLength: Int = 800): Bitmap {
    val width = this.width
    val height = this.height

    if (width <= maxSideLength && height <= maxSideLength) {
        return this // No scaling needed
    }

    val scaleFactor: Float = if (width > height) {
        maxSideLength.toFloat() / width.toFloat()
    } else {
        maxSideLength.toFloat() / height.toFloat()
    }

    val newWidth = (width * scaleFactor).toInt()
    val newHeight = (height * scaleFactor).toInt()

    return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
}
