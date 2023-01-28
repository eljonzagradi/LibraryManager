package models;

import java.sql.Date;

public class Student {
	
	private int studentID;
	private String fullName;
	private String studyprogram;
	private int year;
	private String barcode;
	private Date registrationDate;
	private String status;
	
	public Student(int studentID, String fullName, String studyprogram, int year, 
			String barcode, Date registrationDate) {
		super();
		this.studentID = studentID;
		this.fullName = fullName;
		this.studyprogram = studyprogram;
		this.year = year;
		this.barcode = barcode;
		this.registrationDate = registrationDate;
	}

	public int getStudentID() {
		return studentID;
	}

	public String getFullName() {
		return fullName;
	}

	public int getYear() {
		return year;
	}

	public String getBarcode() {
		return barcode;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setStudentID(int studentID) {
		this.studentID = studentID;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getStudyprogram() {
		return studyprogram;
	}

	public void setStudyprogram(String studyprogram) {
		this.studyprogram = studyprogram;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
