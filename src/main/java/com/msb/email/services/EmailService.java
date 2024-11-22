package com.msb.email.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	public void sendHtmlEmail(String toEmail, String subject, String htmlBody) throws MessagingException {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
		helper.setTo(toEmail);
		helper.setSubject(subject);
		helper.setText(generatePayslipHtml(), true); // true để hỗ trợ HTML
		helper.setFrom("kzdung4@gmail.com");
		mailSender.send(mimeMessage);
	}

	private String generatePayslipHtml() {
		return """
				<!DOCTYPE html>
				<html lang="en">
				<head>
				    <meta charset="UTF-8">
				    <title>Employee Payslip</title>
				    <style>
				        body {
				            font-family: Arial, sans-serif;
				            margin: 20px;
				        }
				        .payslip-container {
				            border: 1px solid #ddd;
				            padding: 20px;
				            width: 100%;
				            max-width: 600px;
				            margin: 0 auto;
				        }
				        .header {
				            text-align: center;
				            margin-bottom: 20px;
				        }
				        .header h2 {
				            margin: 0;
				            font-size: 24px;
				        }
				        .header p {
				            margin: 5px 0;
				            font-size: 14px;
				            color: #555;
				        }
				        .details, .salary-table {
				            width: 100%;
				            margin-bottom: 20px;
				        }
				        .details th, .details td, .salary-table th, .salary-table td {
				            text-align: left;
				            padding: 8px;
				            border-bottom: 1px solid #ddd;
				        }
				        .details th {
				            width: 30%;
				        }
				        .salary-table th {
				            background-color: #f2f2f2;
				        }
				        .total-row td {
				            font-weight: bold;
				            border-top: 2px solid #ddd;
				        }
				        .footer {
				            text-align: center;
				            font-size: 12px;
				            color: #777;
				        }
				    </style>
				</head>
				<body>

				<div class="payslip-container">
				    <div class="header">
				        <h2>Monthly Payslip</h2>
				        <p>Employee: John Doe | Employee ID: 123456</p>
				        <p>Period: September 2024</p>
				    </div>

				    <table class="details">
				        <tr>
				            <th>Employee Name</th>
				            <td>John Doe</td>
				        </tr>
				        <tr>
				            <th>Employee ID</th>
				            <td>123456</td>
				        </tr>
				        <tr>
				            <th>Department</th>
				            <td>Finance</td>
				        </tr>
				        <tr>
				            <th>Position</th>
				            <td>Accountant</td>
				        </tr>
				    </table>

				    <table class="salary-table">
				        <thead>
				            <tr>
				                <th>Description</th>
				                <th>Amount (USD)</th>
				            </tr>
				        </thead>
				        <tbody>
				            <tr>
				                <td>Basic Salary</td>
				                <td>$3,000</td>
				            </tr>
				            <tr>
				                <td>House Rent Allowance (HRA)</td>
				                <td>$500</td>
				            </tr>
				            <tr>
				                <td>Medical Allowance</td>
				                <td>$150</td>
				            </tr>
				            <tr>
				                <td>Other Allowances</td>
				                <td>$200</td>
				            </tr>
				            <tr>
				                <td>Tax Deduction</td>
				                <td>-$350</td>
				            </tr>
				            <tr>
				                <td>Provident Fund</td>
				                <td>-$300</td>
				            </tr>
				            <tr class="total-row">
				                <td>Net Salary</td>
				                <td>$3,200</td>
				            </tr>
				        </tbody>
				    </table>

				    <div class="footer">
				        <p>Thank you for your dedication and hard work!</p>
				    </div>
				</div>

				</body>
				</html>
				            """;
	}
}
