package com.sapienapps.christisus.excelreader

import android.content.Context
import android.net.Uri
import android.util.Log
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.IOException

object ExcelReader {

    private const val TAG = "ExcelReader"

    fun readExcelFile(context: Context, uri: Uri): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        try {
            val realPath = FileUtilsV2.getRealPathFromURIAPI19(context, uri)
            val workbook = WorkbookFactory.create(realPath?.let { File(it) })
            val sheet: Sheet = workbook.getSheetAt(0)

            var isFirstRow = true // Flag to track the first row

            for (row in sheet) {
                if (isFirstRow) {
                    isFirstRow = false // Skip the first row
                    continue
                }
                val rowData = mutableListOf<String>()
                if(row.getCell(0).toString().isNotBlank()){
                    for (cell in row) {
                        val value: String = when (cell.cellType) {
                            CellType.STRING -> cell.stringCellValue.toString()
                            CellType.NUMERIC -> cell.numericCellValue.toString()
                            CellType.BOOLEAN -> cell.booleanCellValue.toString()
                            CellType.FORMULA -> cell.cellFormula.toString()
                            else -> cell.toString()
                        }
                        rowData.add(value)
                    }
                }
                rows.add(rowData.toList())
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading Excel file", e)
        }
        return rows
    }
}

