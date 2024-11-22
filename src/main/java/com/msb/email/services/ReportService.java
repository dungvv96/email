package com.msb.email.services;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.msb.email.model.Luong;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

@Service
public class ReportService {

	public ByteArrayInputStream export() throws JRException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "jasper/payroll_summary.jrxml";
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(fileName).getFile());
		JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenDonViQuanLy", "Công ty quản lý bay miền bắc");
		parameters.put("hoVaTen", "Phạm Văn Hợi");
		parameters.put("maNhanVien", "055231");
		parameters.put("chucDanh", "Trưởng phòng");
		parameters.put("donViCongTac", "Trung tâm quản lý luồng không lưu");
		parameters.put("total", "122,123,123");
		parameters.put("totalTruocThue", "122,123,123");
		parameters.put("totalThueTncn", "122,123,123");
		String date = "Từ ngày: 10/05/2024 đến ngày: 10/11/2024";
		parameters.put("fromDate", date);
		parameters.put("ngayXuat", "06/11/2024");
		try (InputStream logoStream = new ClassPathResource("logo.png").getInputStream()) {
			Image logoImage = ImageIO.read(logoStream);
			parameters.put("logo", logoImage);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Luong> items = new ArrayList<>();
		items.add(new Luong("1", "25/08/2024", "12,230,000", "1,230,000", "12,630,000"));
		items.add(new Luong("1", "25/09/2024", "12,630,000", "1,630,000", "12,630,000"));
		items.add(new Luong("1", "25/10/2024", "12,630,000", "630,000", "12,630,000"));

		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);
		parameters.put("ItemLuong", dataSource);

		JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
		ExporterInput exporterInput = new SimpleExporterInput(print);
		JRPdfExporter exporter = new JRPdfExporter();
		exporter.setExporterInput(exporterInput);
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
		SimplePdfExporterConfiguration config = new SimplePdfExporterConfiguration();
		exporter.setConfiguration(config);
		exporter.exportReport();
//		
//		JRDocxExporter exporter = new JRDocxExporter();
//		exporter.setExporterInput(exporterInput);
//		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
//		SimpleDocxReportConfiguration config = new SimpleDocxReportConfiguration();
//		exporter.setConfiguration(config);
//		exporter.exportReport();

//		HtmlExporter exporter = new HtmlExporter();
//	    exporter.setExporterInput(exporterInput);
//	    exporter.setExporterOutput(new SimpleHtmlExporterOutput(baos));
//	    
//	    exporter.exportReport();
		return new ByteArrayInputStream(baos.toByteArray());
	}
}
