package models;

import java.sql.Date;

public class Borrow {
	
	private int borrowID;
	private String student;
	private String book;
	private Date start;
	private Date due;
	private String status;
	
	public Borrow(int borrowID,String student, String book, Date start, Date due, String status) {
		super();
		this.borrowID = borrowID;
		this.student = student;
		this.book = book;
		this.start = start;
		this.due = due;
		this.status = status;
	}

	public int getBorrowID() {
		return borrowID;
	}

	public String getStudent() {
		return student;
	}

	public String getBook() {
		return book;
	}

	public Date getStart() {
		return start;
	}

	public Date getDue() {
		return due;
	}

	public String getStatus() {
		return status;
	}

	public void setBorrowID(int borrowID) {
		this.borrowID = borrowID;
	}

	public void setStudent(String student) {
		this.student = student;
	}

	public void setBook(String book) {
		this.book = book;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void setDue(Date due) {
		this.due = due;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
