package com.msb.customer.model;

public class CusAbsSigResult {

	private String companyBook;
	private String customerNo;
	private String datetime;
	private String inputter;
	public String getCompanyBook() {
		return companyBook;
	}
	public void setCompanyBook(String companyBook) {
		this.companyBook = companyBook;
	}
	public String getCustomerNo() {
		return customerNo;
	}
	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}
	public String getDatetime() {
		return datetime;
	}
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	public String getInputter() {
		return inputter;
	}
	public void setInputter(String inputter) {
		this.inputter = inputter;
	}
	
	@Override
	public String toString() {
		return customerNo + "#@" + datetime + "#@" + inputter;
	}
}
