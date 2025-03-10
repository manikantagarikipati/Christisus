package com.sapienapps.christisus.excelreader

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.sapienapps.christisus.fillstudent.StudentInfoViewData
import com.sapienapps.christisus.planner.ClassRoom
import com.sapienapps.christisus.planner.Student
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random


object FileUtilsV2 {

    @SuppressLint("NewApi")
    fun getRealPathFromURIAPI19(context: Context, uri: Uri): String? {
        val isKitKat = true

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]

                // This is for checking Main Memory
                return if ("primary".equals(type, ignoreCase = true)) {
                    if (split.size > 1) {
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else {
                        Environment.getExternalStorageDirectory().toString() + "/"
                    }
                    // This is for checking SD Card
                } else {
                    "storage" + "/" + docId.replace(":", "/")
                }
            } else if (isDownloadsDocument(uri)) {
                val fileName: String? = getFilePath(context, uri)
                if (fileName != null) {
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/Download/" + fileName
                }

                var id = DocumentsContract.getDocumentId(uri)
                if (id.startsWith("raw:")) {
                    id = id.replaceFirst("raw:".toRegex(), "")
                    val file = File(id)
                    if (file.exists()) return id
                }

                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    id.toLong()
                )
                return getDataColumn(context!!, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )

                return getDataColumn(context!!, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Return the remote address

            if (isGooglePhotosUri(uri)) return uri.lastPathSegment

            return getDataColumn(context!!, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }

        return null
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )

        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    private fun getFilePath(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )

        try {
            cursor = context.contentResolver.query(
                uri, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun createMasterFile(context:Context,studentViewInfo: List<StudentInfoViewData>):String{
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("MasterStudentList")
        val headerRow = sheet.createRow(0)

        headerRow.createCell(0).setCellValue("Name")
        headerRow.createCell(1).setCellValue("First Name")
        headerRow.createCell(2).setCellValue("Profile")
        headerRow.createCell(3).setCellValue("Language")
        headerRow.createCell(4).setCellValue("Non Friend1")
        headerRow.createCell(5).setCellValue("Non Friend2")
        headerRow.createCell(6).setCellValue("Friend1")
        headerRow.createCell(7).setCellValue("Friend2")

        for(i in studentViewInfo.indices){
            val row = sheet.createRow(i+1)
            row.createCell(0).setCellValue(studentViewInfo[i].name)
            row.createCell(1).setCellValue(studentViewInfo[i].firstName)
            row.createCell(2).setCellValue(studentViewInfo[i].Profile)
            row.createCell(3).setCellValue(studentViewInfo[i].language)
            row.createCell(4).setCellValue(studentViewInfo[i].unFriend1)
            row.createCell(5).setCellValue(studentViewInfo[i].unFriend2)
            row.createCell(6).setCellValue(studentViewInfo[i].friend1)
            row.createCell(7).setCellValue(studentViewInfo[i].friend2)
        }

        val fileNumber = Random.nextInt(2000)
        val fileName = "ChristiusMasterStudentFileWithFriendInfo${fileNumber}.xlsx"

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use {
            workbook.write(it)
        }
        workbook.close()
        return file.absolutePath // Return the file path
    }

    fun writeResultsToExcel(context: Context,
                            outputPath: String,
                            classes:List<ClassRoom>,
                            conflictedStudents:List<Student>,
                            fileName:String = "ChristiusFinalClassList"):String {
        val workbook = XSSFWorkbook()

        // Create sheet for assigned students
        val assignedSheet = workbook.createSheet("Assigned Classes")
        var rowNum = 0

        // Write header
        val headerRow = assignedSheet.createRow(rowNum++)
        headerRow.createCell(0).setCellValue("Class")
        headerRow.createCell(1).setCellValue("First Name")
        headerRow.createCell(2).setCellValue("Last Name")
        headerRow.createCell(3).setCellValue("Profile")
        headerRow.createCell(4).setCellValue("Language")
        headerRow.createCell(5).setCellValue("Non Friend1")
        headerRow.createCell(6).setCellValue("Non Friend2")
        headerRow.createCell(7).setCellValue("Friend1")
        headerRow.createCell(8).setCellValue("Friend Match")
        headerRow.createCell(9).setCellValue("Friend2")
        headerRow.createCell(10).setCellValue("Friend Match")


        // Write assigned students
        for (classroom in classes) {
            for (student in classroom.students) {
                val row = assignedSheet.createRow(rowNum++)
                row.createCell(0).setCellValue("${classroom.id}")
                row.createCell(1).setCellValue(student.firstName)
                row.createCell(2).setCellValue(student.lastName)
                row.createCell(3).setCellValue(student.profile.toString())
                row.createCell(4).setCellValue(student.language.toString())
                row.createCell(5).setCellValue(student.nonFriendsList.getOrNull(0).orEmpty())
                row.createCell(6).setCellValue(student.nonFriendsList.getOrNull(1).orEmpty())
                val friend1 = student.friendsList.getOrNull(0)
                row.createCell(7).setCellValue(friend1.orEmpty())
                if(friend1.isNullOrEmpty().not() && classroom.students.find { friend1!!.contains(it.fullName()) || friend1.contains(it.reverseFullName()) }!=null ){
                    row.createCell(8).setCellValue("+")
                }
                val friend2 = student.friendsList.getOrNull(1)
                row.createCell(9).setCellValue(friend2.orEmpty())
                if(friend2.isNullOrEmpty().not() && classroom.students.find { friend2!!.contains(it.fullName()) || friend2.contains(it.reverseFullName()) }!=null ){
                    row.createCell(10).setCellValue("+")
                }
            }
        }

        // Create sheet for conflicted students
        val conflictSheet = workbook.createSheet("Conflicts")
        rowNum = 0

        // Write header for conflicts
        val conflictHeaderRow = conflictSheet.createRow(rowNum++)
        conflictHeaderRow.createCell(0).setCellValue("First Name")
        conflictHeaderRow.createCell(1).setCellValue("Last Name")
        conflictHeaderRow.createCell(2).setCellValue("Profile")
        conflictHeaderRow.createCell(3).setCellValue("Language")
        conflictHeaderRow.createCell(4).setCellValue("Reason")

        // Write conflicted students
        for (student in conflictedStudents) {
            val row = conflictSheet.createRow(rowNum++)
            row.createCell(0).setCellValue(student.firstName)
            row.createCell(1).setCellValue(student.lastName)
            row.createCell(2).setCellValue(student.profile.toString())
            row.createCell(3).setCellValue(student.language.toString())
            row.createCell(4).setCellValue("Could not assign to any class")
        }


        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "${fileName}.xlsx")

        // Write the workbook to file
        FileOutputStream(file).use {
            workbook.write(it)
        }
        workbook.close()
        return file.absolutePath // Return the file path
    }
}
