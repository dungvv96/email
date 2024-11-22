package com.msb.email.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msb.email.services.EmailService;

import jakarta.mail.MessagingException;

@RestController
public class EmailController {

	@Autowired
	private EmailService emailService;

	@PostMapping("/sendEmail")
	public String sendEmail(@RequestParam String toEmail, @RequestParam String subject, @RequestParam String body)
			throws MessagingException {
		emailService.sendHtmlEmail(toEmail, subject, body);
		return "Email sent successfully!";
	}
}
