package com.msb.email.services;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class JsonToExcelConverter {
	public static void main(String[] args) {
		String jsonFilePath = "D:/MsbApiDetailAALendingSRV815LC-v1.0.0-swagger.json";
		String excelFilePath = "D:/output.xlsx";
		convertJsonToExcel(jsonFilePath, excelFilePath);
	}

	public static void convertJsonToExcel(String jsonFilePath, String excelFilePath) {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("CustomerProspectBody");
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));
			JsonNode propertiesNode = rootNode.path("definitions").path("MsbApiDetailAALendingSRV815LCResponseBody")
					.path("items").path("properties");

			int rowNum = 0;
			Row headerRow = sheet.createRow(rowNum++);
			headerRow.createCell(0).setCellValue("Field Name");
			headerRow.createCell(1).setCellValue("Data Type (Max Length)");
			headerRow.createCell(2).setCellValue("Description");
			processJsonNode(sheet, propertiesNode, rowNum);

			try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
				workbook.write(fileOut);
			}
			System.out.println("Excel file created: " + excelFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int processJsonNode(Sheet sheet, JsonNode node, int rowNum) {
		Iterator<String> fieldNames = node.fieldNames();
		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			JsonNode fieldNode = node.get(fieldName);
			String dataType = fieldNode.has("type") ? StringUtils.capitalize(fieldNode.get("type").asText())
					: "Unknown";
			String maxLength = fieldNode.has("maxLength") ? fieldNode.get("maxLength").asText() : "";
			String description = fieldNode.has("description") ? fieldNode.get("description").asText() : "";
			addFieldInfo(sheet, rowNum++, fieldName, dataType, maxLength, description);
		}
		return rowNum;
	}

	private static void addFieldInfo(Sheet sheet, int rowNum, String fieldName, String dataType, String maxLength,
			String description) {
		Row row = sheet.createRow(rowNum);
		row.createCell(0).setCellValue(fieldName);
		row.createCell(1).setCellValue(dataType + (maxLength.isEmpty() ? "" : " (" + maxLength + ")"));
		row.createCell(2).setCellValue(description);
	}
}
