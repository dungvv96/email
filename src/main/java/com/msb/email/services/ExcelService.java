
package com.msb.email.services;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;


@Service
public class ExcelService {

	private static final String API_RESOURCES_FILE_PATH = "D:/api_resources.txt";
	private static final String FUNCTIONS_FILE_PATH = "D:/functions.txt";
	private static final String FUNCTION_API_RESOURCES_FILE_PATH = "D:/function_api_resources.txt";

	private Map<Integer, List<String>> mapFunctions = new HashMap<>();
	private Map<Integer, List<String>> mapApiResources = new HashMap<>();

	@PostConstruct
	public void genQuery() {
		try {
			readExcel();
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(API_RESOURCES_FILE_PATH, true))) {
				for (int i = 2; i <= mapApiResources.size(); i++) {
					List<String> list = mapApiResources.get(i);
					String data = "insert into api_resources (id, name, url) values ( {0}, {1}, {2});";
					data = data.replace("{0}", list.get(0));
					data = data.replace("{1}", "'" + list.get(1) + "'");
					data = data.replace("{2}", "'" + list.get(2) + "'");
					writer.write(data);
					writer.newLine();
				}
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(FUNCTIONS_FILE_PATH, true));
					BufferedWriter writer2 = new BufferedWriter(
							new FileWriter(FUNCTION_API_RESOURCES_FILE_PATH, true))) {
				for (int i = 2; i <= mapFunctions.size(); i++) {
					List<String> list = mapFunctions.get(i);
					String data = "insert into functions (id, function_name, parent_id, icon) values ( {0}, {1}, {2}, {3});";
					data = data.replace("{0}", list.get(0));
					data = data.replace("{1}", "'" + list.get(2) + "'");
					data = data.replace("{2}", StringUtils.isEmpty(list.get(3)) ? "null" : list.get(3));
					data = data.replace("{3}", "'" + list.get(4) + "'");
					writer.write(data);
					writer.newLine();
					Long[] apiResources = Stream.of(list.get(5).split(",")).map(Long::valueOf).toArray(Long[]::new);
					for (Long apiResource : apiResources) {
						String data2 = "insert into function_api_resources (function_id, api_resource_id) values ( {0}, {1});";
						data2 = data2.replace("{0}", list.get(0));
						data2 = data2.replace("{1}", apiResource.toString());
						writer2.write(data2);
						writer2.newLine();
					}
				}
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

	}

	public void readExcel() {
		try (FileInputStream file = new FileInputStream("D:/api_function.xlsx");
				ReadableWorkbook wb = new ReadableWorkbook(file)) {
			Sheet sheet = wb.getFirstSheet();
			try (Stream<Row> rows = sheet.openStream()) {
				rows.forEach(r -> {
					mapFunctions.put(r.getRowNum(), new ArrayList<>());
					for (Cell cell : r) {
						if (cell == null) {
							mapFunctions.get(r.getRowNum()).add("");
						} else {
							mapFunctions.get(r.getRowNum()).add(cell.getRawValue());
						}
					}
				});
			}
			Sheet sheet2 = wb.getSheet(1).get();
			try (Stream<Row> rows = sheet2.openStream()) {
				rows.forEach(r -> {
					mapApiResources.put(r.getRowNum(), new ArrayList<>());
					for (Cell cell : r) {
						if (cell == null) {
							mapApiResources.get(r.getRowNum()).add("");
						} else {
							mapApiResources.get(r.getRowNum()).add(cell.getRawValue());
						}
					}
				});
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		System.out.println(mapFunctions);
		System.out.println(mapApiResources);
	}
}
