package com.sapienapps.christisus.excelreader

import android.content.Context
import android.net.Uri
import android.util.Log
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.IOException

object ExcelReader {

    private const val TAG = "ExcelReader"

    fun readExcelFile(context: Context, uri: Uri, actualRealPath: String?): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        try {
            val realPath = actualRealPath ?: FileUtilsV2.getRealPathFromURIAPI19(context, uri)
            val workbook = WorkbookFactory.create(realPath?.let { File(it) })
            val sheet: Sheet = workbook.getSheetAt(0)

            var isFirstRow = true // Flag to track the first row
            var emptyRowCount = 0 // Counter to track consecutive empty rows
            val emptyRowThreshold = 30 // Stop reading after 30 consecutive empty rows

            for (row in sheet) {
                if (isFirstRow) {
                    isFirstRow = false // Skip the first row
                    continue
                }

                // Corrected empty row check (ensures all cells are checked)
                val isRowEmpty = row.physicalNumberOfCells == 0 || (0 until row.lastCellNum).all {
                    row.getCell(it, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().isBlank()
                }

                if (isRowEmpty) {
                    emptyRowCount++
                    if (emptyRowCount >= emptyRowThreshold) break // Stop if too many empty rows
                    continue
                } else {
                    emptyRowCount = 0 // Reset counter when a valid row is found
                }

                val rowData = mutableListOf<String>()
                val firstCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                if (firstCell.toString().isBlank()) continue // Skip if first cell is empty

                // Read all cells properly, including blank ones
                for (cellIndex in 0 until row.lastCellNum) {
                    val cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                    val value: String = when (cell.cellType) {
                        CellType.STRING -> cell.stringCellValue
                        CellType.NUMERIC -> cell.numericCellValue.toString()
                        CellType.BOOLEAN -> cell.booleanCellValue.toString()
                        CellType.FORMULA -> cell.cellFormula
                        else -> cell.toString()
                    }
                    rowData.add(value)
                }

                if (rowData.isNotEmpty()) {
                    rows.add(rowData.toList())
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading Excel file", e)
        }
        return rows
    }

}

