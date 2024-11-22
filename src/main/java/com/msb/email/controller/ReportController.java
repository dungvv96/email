package com.msb.email.controller;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msb.email.services.ReportService;

import net.sf.jasperreports.engine.JRException;

@RestController
public class ReportController {

	@Autowired
	private ReportService reportService;

	@GetMapping("/export")
	public ResponseEntity<Resource> exportById() throws JRException, SQLException {
		InputStreamResource isr = new InputStreamResource(reportService.export());
		return ResponseEntity.ok().header("filename", isr.getFilename())
				.contentType(MediaType.parseMediaType("application/octet-stream")).body(isr);
	}

}
