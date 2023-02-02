package models;

import java.sql.Date;

public class Borrow {
	
	private String student;
	private String book;
	private Date start;
	private Date due;
	private int studentID, bookID;
	
	public Borrow(String student,int studentID, String book, int bookID, Date start, Date due) {
		super();
		this.student = student;
		this.book = book;
		this.start = start;
		this.due = due;
		this.studentID = studentID;
		this.bookID = bookID;
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

	public int getStudentID() {
		return studentID;
	}

	public int getBookID() {
		return bookID;
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

	public void setStudentID(int studentID) {
		this.studentID = studentID;
	}

	public void setBookID(int bookID) {
		this.bookID = bookID;
	}

}
