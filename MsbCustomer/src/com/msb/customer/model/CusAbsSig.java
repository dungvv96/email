package com.msb.customer.model;

public class CusAbsSig {

	private String customerNo;
	private long dateTime;
	private String inputter;
	public String getCustomerNo() {
		return customerNo;
	}
	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}
	public long getDateTime() {
		return dateTime;
	}
	public void setDateTime(long dateTime) {
		this.dateTime = dateTime;
	}
	public String getInputter() {
		return inputter;
	}
	public void setInputter(String inputter) {
		this.inputter = inputter;
	}
	public CusAbsSig(String customerNo, long dateTime, String inputter) {
		super();
		this.customerNo = customerNo;
		this.inputter = inputter;
		this.dateTime = dateTime;
	}
	public CusAbsSig() {
	}
	@Override
	public String toString() {
		return customerNo + "#@" + dateTime + "#@" + inputter;
	}
}
