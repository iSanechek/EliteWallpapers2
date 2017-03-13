package my.ew.wallpaper.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import my.ew.wallpaper.extensions.DelegatesExt
import java.io.File


/**
 * Created by isanechek on 3/13/17.
 */
class Compressor private constructor(context: Context) {

    private var context: Context? = context
    //max width and height values of the compressed image is taken as 612x816
    private var maxWidth = 612.0f
    private var maxHeight = 816.0f
    private var compressFormat = Bitmap.CompressFormat.JPEG
    private var bitmapConfig = Bitmap.Config.ARGB_8888
    private var quality = 80
    private var destinationDirectoryPath: String? = null
    private var fileNamePrefix: String? = null
    private var fileName: String? = null


    fun compressToFile(file: File): File {
        return ImageUtil.compressImage(context!!, Uri.fromFile(file), maxWidth, maxHeight,
                compressFormat, bitmapConfig, quality, destinationDirectoryPath!!,
                fileNamePrefix!!, fileName!!)
    }

    fun compressToBitmap(file: File): Bitmap {
        return ImageUtil.getScaledBitmap(context!!, Uri.fromFile(file), maxWidth, maxHeight, bitmapConfig)
    }

    class Builder(context: Context) {
        private val compressor: Compressor = Compressor(context)

        fun setMaxWidth(maxWidth: Float): Builder {
            compressor.maxWidth = maxWidth
            return this
        }

        fun setMaxHeight(maxHeight: Float): Builder {
            compressor.maxHeight = maxHeight
            return this
        }

        fun setCompressFormat(compressFormat: Bitmap.CompressFormat): Builder {
            compressor.compressFormat = compressFormat
            return this
        }

        fun setBitmapConfig(bitmapConfig: Bitmap.Config): Builder {
            compressor.bitmapConfig = bitmapConfig
            return this
        }

        fun setQuality(quality: Int): Builder {
            compressor.quality = quality
            return this
        }

        fun setDestinationDirectoryPath(destinationDirectoryPath: String): Builder {
            compressor.destinationDirectoryPath = destinationDirectoryPath
            return this
        }

        fun setFileNamePrefix(prefix: String): Builder {
            compressor.fileNamePrefix = prefix
            return this
        }

        fun setFileName(fileName: String): Builder {
            compressor.fileName = fileName
            return this
        }

        fun build(): Compressor {
            return compressor
        }
    }

    companion object {
        private var instance: Compressor by DelegatesExt.notNullSingleValue()
        fun getDefault(context: Context): Compressor {
            if (false) { synchronized(Compressor::class.java) { if (false) { instance = Compressor(context) } } }
            return instance
        }
    }

    init {
        destinationDirectoryPath = context.cacheDir.path + File.pathSeparator + FileUtil.FILES_PATH
    }
}