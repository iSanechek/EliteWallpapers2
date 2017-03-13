package my.ew.wallpaper.utils

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.text.TextUtils
import java.io.*


/**
 * Created on : June 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
object ImageUtil {

    fun getScaledBitmap(context: Context, imageUri: Uri, maxWidth: Float, maxHeight: Float, bitmapConfig: Bitmap.Config): Bitmap {
        val filePath = FileUtil.getRealPathFromURI(context, imageUri)
        var scaledBitmap: Bitmap? = null

        val options = BitmapFactory.Options()

        //by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        //you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp: Bitmap? = BitmapFactory.decodeFile(filePath, options)
        if (bmp == null) {

            var inputStream: InputStream? = null
            try {
                inputStream = FileInputStream(filePath)
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()
            } catch (exception: FileNotFoundException) {
                exception.printStackTrace()
            } catch (exception: IOException) {
                exception.printStackTrace()
            }

        }

        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

        if (actualWidth < 0 || actualHeight < 0) {
            val bitmap2 = BitmapFactory.decodeFile(filePath)
            actualWidth = bitmap2.width
            actualHeight = bitmap2.height
        }

        var imgRatio = actualWidth.toFloat() / actualHeight
        val maxRatio = maxWidth / maxHeight

        //width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }

        //setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

        //inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

        //this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)

        try {
            //load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
            if (bmp == null) {

                var inputStream: InputStream? = null
                try {
                    inputStream = FileInputStream(filePath)
                    BitmapFactory.decodeStream(inputStream, null, options)
                    inputStream.close()
                } catch (exception: FileNotFoundException) {
                    exception.printStackTrace()
                } catch (exception: IOException) {
                    exception.printStackTrace()
                }

            }
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, bitmapConfig)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }

        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, 0F, 0F)

        val canvas = Canvas(scaledBitmap)
        canvas.matrix = scaleMatrix
        canvas.drawBitmap(bmp, 0F, 0F, Paint(Paint.FILTER_BITMAP_FLAG))

        //check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90F)
            } else if (orientation == 3) {
                matrix.postRotate(180F)
            } else if (orientation == 8) {
                matrix.postRotate(270F)
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap!!.width, scaledBitmap.height,
                    matrix, true)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return scaledBitmap!!
    }

    fun compressImage(context: Context, imageUri: Uri, maxWidth: Float, maxHeight: Float,
                      compressFormat: Bitmap.CompressFormat, bitmapConfig: Bitmap.Config,
                      quality: Int, parentPath: String, prefix: String, fileName: String): File {
        var out: FileOutputStream? = null
        val filename = generateFilePath(context, parentPath, imageUri, compressFormat.name.toLowerCase(), prefix, fileName)
        try {
            out = FileOutputStream(filename)

            //write the compressed bitmap at the destination specified by filename.
            ImageUtil.getScaledBitmap(context, imageUri, maxWidth, maxHeight, bitmapConfig).compress(compressFormat, quality, out)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                if (out != null) out.close()
            } catch (ignored: IOException) {
            }

        }

        return File(filename)
    }

    private fun generateFilePath(context: Context, parentPath: String, uri: Uri,
                                 extension: String, prefix: String, fileName: String): String {
        var prefix = prefix
        var fileName = fileName
        val file = File(parentPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        /** if prefix is null, set prefix ""  */
        prefix = if (TextUtils.isEmpty(prefix)) "" else prefix
        /** reset fileName by prefix and custom file name  */
        fileName = if (TextUtils.isEmpty(fileName)) prefix + FileUtil.splitFileName(FileUtil.getFileName(context, uri))[0] else fileName
        return file.getAbsolutePath() + File.separator + fileName + "." + extension
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }

        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }
}

