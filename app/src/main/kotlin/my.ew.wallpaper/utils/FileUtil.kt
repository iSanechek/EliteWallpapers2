package my.ew.wallpaper.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.system.Os.rename
import java.io.*
import java.nio.charset.Charset


/**
 * Created by isanechek on 3/13/17.
 */
object FileUtil {

    val FILES_PATH = "Compressor"
    private val EOF = -1
    private val DEFAULT_BUFFER_SIZE = 1024 * 4

    @Throws(IOException::class)
    fun from(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(context, uri)
        val splitName = splitFileName(fileName)
        var tempFile = File.createTempFile(splitName[0], splitName[1])
        tempFile = rename(tempFile, fileName)
        tempFile.deleteOnExit()
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(tempFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        if (inputStream != null) {
            out?.let { copy(inputStream, it) }
            inputStream.close()
        }

        if (out != null) out.close()
        return tempFile
    }

    fun splitFileName(fileName: String): Array<String> {
        var name = fileName
        var extension = ""
        val i = fileName.lastIndexOf(".")
        if (i != -1) {
            name = fileName.substring(0, i)
            extension = fileName.substring(i)
        }
        return arrayOf(name, extension)
    }

    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf(File.separator)
            if (cut != -1) result = result.substring(cut + 1)
        }
        return result
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null) {
            return contentUri.getPath()
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            val realPath = cursor.getString(index)
            cursor.close()
            return realPath
        }
    }

    fun rename(file: File, newName: String): File {
        val newFile = File(file.getParent(), newName)
        if (newFile != file) {
            if (newFile.exists()) {
                if (newFile.delete()) {
                    Log.d("FileUtil", "Delete old $newName file")
                }
            }
            if (file.renameTo(newFile)) {
                Log.d("FileUtil", "Rename file to " + newName)
            }
        }
        return newFile
    }

    @Throws(IOException::class)
    fun copy(input: InputStream, output: OutputStream): Int {
        val count = copyLarge(input, output)
        if (count > Integer.MAX_VALUE) {
            return -1
        }
        return count.toInt()
    }

    @Throws(IOException::class)
    fun copyLarge(input: InputStream, output: OutputStream) : Long {
        return copyLarge(input, output, ByteArray(DEFAULT_BUFFER_SIZE))
    }

    @Throws(IOException::class)
    fun copyLarge(input: InputStream, output: OutputStream, buffer: ByteArray): Long {
        var count: Long = 0
        val n: Int = input.read(buffer)
        while (n != EOF) {
            output.write(buffer, 0, n)
            count += n.toLong()
        }
        return count
    }
}