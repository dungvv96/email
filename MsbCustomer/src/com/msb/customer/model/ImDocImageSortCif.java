package com.msb.customer.model;

public class ImDocImageSortCif {

	private String refNo;
	private String application;
	private String shortDesc;
	private String desc;
	private long effDate;
	private long expDate;
	private String imageId;
	private long dateTime;
	private int currNo;
	private long cifId;
	private String inputter;

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public long getEffDate() {
		return effDate;
	}

	public void setEffDate(long effDate) {
		this.effDate = effDate;
	}

	public long getExpDate() {
		return expDate;
	}

	public void setExpDate(long expDate) {
		this.expDate = expDate;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public long getDateTime() {
		return dateTime;
	}

	public void setDateTime(long dateTime) {
		this.dateTime = dateTime;
	}

	public int getCurrNo() {
		return currNo;
	}

	public void setCurrNo(int currNo) {
		this.currNo = currNo;
	}

	public long getCifId() {
		return cifId;
	}

	public void setCifId(long cifId) {
		this.cifId = cifId;
	}
	
	public String getInputter() {
		return inputter;
	}

	public void setInputter(String inputter) {
		this.inputter = inputter;
	}

	public ImDocImageSortCif(String refNo, String application, String shortDesc, String desc, long effDate,
			long expDate, String imageId, long dateTime, int currNo, long cifId, String inputter) {
		super();
		this.refNo = refNo;
		this.application = application;
		this.shortDesc = shortDesc;
		this.desc = desc;
		this.effDate = effDate;
		this.expDate = expDate;
		this.imageId = imageId;
		this.dateTime = dateTime;
		this.currNo = currNo;
		this.cifId = cifId;
		this.inputter = inputter;
	}

	public ImDocImageSortCif() {
		super();
	}

	@Override
	public String toString() {
		return refNo + "#@" + application + "#@" + shortDesc + "#@" + desc + "#@" + effDate + "#@" + expDate + "#@"
				+ imageId + "#@" + dateTime + "#@" + currNo + "#@" + cifId + "#@" + inputter;
	}

}
