package it.polito.mad.g01_timebanking

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.ImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileHelper {
    companion object{
        fun readImage(profilePicturePath: String, profilePicture: ImageView) {
            val takenImage = BitmapFactory.decodeFile(profilePicturePath)
            val ei = ExifInterface(profilePicturePath)
            val orientation: Int = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            var rotatedBitmap: Bitmap? = null
            rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(takenImage, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(takenImage, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(takenImage, 270)
                ExifInterface.ORIENTATION_NORMAL -> takenImage
                else -> takenImage
            }
            profilePicture.setImageBitmap(rotatedBitmap)

        }
        private fun rotateImage(source: Bitmap, angle: Int): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            return Bitmap.createBitmap(
                source, 0, 0, source.width, source.height,
                matrix, true
            )
        }

        //@Throws(IOException::class)
        fun createImageFile(context: Context): File {
            // Create an image file name
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            //val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)  ///storage/sdcard0/Pictures
            var baseFolder = if(isExternalStorageWritable()) {
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath ?: ""  //storage/sdcard0/Pictures
            }
            // revert to using internal storage
            else {
                //getFilesDir()
                //Returns the absolute path to the directory on the filesystem where files created with openFileOutput are stored
                context.filesDir.absolutePath
            }

            return File(baseFolder,"JPEG_${timeStamp}.jpg")
/*        return File.createTempFile(
            "JPEG_${timeStamp}_", *//* prefix *//*
            ".jpg", *//* suffix *//*
            storageDir *//* directory *//*
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            //eg. /storage/emulated/0/Android/data/it.polito.mad.g01_timebanking/files/Pictures/JPEG_20220329_123453_7193664665067830656.jpg
            profilePicturePath = absolutePath
        }*/
        }

        fun getRealPathFromURI(uri: Uri?, context: Context): String {
            var filePath = ""
            //image:33 or primary:Download/download.jpeg
            val wholeID = DocumentsContract.getDocumentId(uri)
            // Split at colon, use second item in the array
            val id = wholeID.split(":").toTypedArray()[1]

            val type = wholeID.split(":").toTypedArray()[0]

            //When picture is choosen from the external dir e.g. sdk_gphone.../Download
            //The uri in the result data is in the form: content://com.android.externalstorage.documents/document/primary%3ADownload%2Fdownload.jpeg
            //So the authority is externalstorage and the path to return is built through the absolute path of Environment.get...
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().absolutePath + "/" + id;
                //e.g. /storage/emulated/0/Download/download.jpeg
            }
            //The picture is choosen from recent or download or any anpther suggested pseudo-folder of the gallery
            //The uri in the result data is in the form: content://com.android.providers.media.documents/document/image%3A33
            else {  //type is image
                val column = arrayOf(MediaStore.Images.Media.DATA)

                // where id is equal to
                val sel = MediaStore.Images.Media._ID + "=?"
                val cursor: Cursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column,
                    sel,
                    arrayOf(id),
                    null
                )!!
                val columnIndex = cursor.getColumnIndex(column[0])
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex)
                }
                cursor.close()
                return filePath
            }

        }


        // Checks if a volume containing external storage is available
        // for read and write.
        fun isExternalStorageWritable(): Boolean {
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
        }


    }
}