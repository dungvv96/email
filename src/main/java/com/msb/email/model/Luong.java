package com.msb.email.model;

public class Luong {

	private String stt;
	private String ngayThanhToan;
	private String thuNhapTruocThue;
	private String thueTncnDaKhauTru;
	private String thuNhapSauThue;

	/**
	 * @return the stt
	 */
	public String getStt() {
		return stt;
	}

	/**
	 * @param stt the stt to set
	 */
	public void setStt(String stt) {
		this.stt = stt;
	}

	/**
	 * @return the ngayThanhToan
	 */
	public String getNgayThanhToan() {
		return ngayThanhToan;
	}

	/**
	 * @param ngayThanhToan the ngayThanhToan to set
	 */
	public void setNgayThanhToan(String ngayThanhToan) {
		this.ngayThanhToan = ngayThanhToan;
	}

	/**
	 * @return the thuNhapTruocThue
	 */
	public String getThuNhapTruocThue() {
		return thuNhapTruocThue;
	}

	/**
	 * @param thuNhapTruocThue the thuNhapTruocThue to set
	 */
	public void setThuNhapTruocThue(String thuNhapTruocThue) {
		this.thuNhapTruocThue = thuNhapTruocThue;
	}

	/**
	 * @return the thueTncnDaKhauTru
	 */
	public String getThueTncnDaKhauTru() {
		return thueTncnDaKhauTru;
	}

	/**
	 * @param thueTncnDaKhauTru the thueTncnDaKhauTru to set
	 */
	public void setThueTncnDaKhauTru(String thueTncnDaKhauTru) {
		this.thueTncnDaKhauTru = thueTncnDaKhauTru;
	}

	/**
	 * @return the thuNhapSauThue
	 */
	public String getThuNhapSauThue() {
		return thuNhapSauThue;
	}

	/**
	 * @param thuNhapSauThue the thuNhapSauThue to set
	 */
	public void setThuNhapSauThue(String thuNhapSauThue) {
		this.thuNhapSauThue = thuNhapSauThue;
	}
	
	public Luong(String stt, String ngayThanhToan, String thuNhapTruocThue, String thueTncnDaKhauTru,
			String thuNhapSauThue) {
		super();
		this.stt = stt;
		this.ngayThanhToan = ngayThanhToan;
		this.thuNhapTruocThue = thuNhapTruocThue;
		this.thueTncnDaKhauTru = thueTncnDaKhauTru;
		this.thuNhapSauThue = thuNhapSauThue;
	}

	public Luong() {
		super();
	}

}
