package com.msb.email.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {

	@GetMapping("/image")
	public ResponseEntity<Resource> getImage() {
		try {
			Path imagePath = Paths.get("D:/logo.png");
			Resource resource = new UrlResource(imagePath.toUri());
			if (resource.exists() || resource.isReadable()) {
				String mimeType = Files.probeContentType(imagePath);
				MediaType mediaType = mimeType != null ? MediaType.parseMediaType(mimeType)
						: MediaType.APPLICATION_OCTET_STREAM;
				return ResponseEntity.ok().contentType(mediaType)
						.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}
