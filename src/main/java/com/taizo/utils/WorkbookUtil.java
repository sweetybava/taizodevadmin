package com.taizo.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class WorkbookUtil {

	public static void writeCell(Row row, int columnNo, String value) {
		Cell cell = row.createCell(columnNo);
		cell.setCellValue(value);
	}
}
