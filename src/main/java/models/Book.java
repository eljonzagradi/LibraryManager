package models;

public class Book {

	private int bookID;
	private String title;
	private String category;
	private int pages;
	private String language;
	private String author;
	private String publisher;
	private String barcode;
	private int copies;

	public Book(int bookID, String title, String category,
			int pages, String language,
			String author, String publisher,String barcode, int copies) {
		super();
		this.bookID = bookID;
		this.title = title;
		this.category = category;
		this.pages = pages;
		this.language = language;
		this.author = author;
		this.publisher = publisher;
		this.barcode = barcode;
		this.copies = copies;

	}

	public int getBookID() {
		return bookID;
	}

	public String getTitle() {
		return title;
	}

	public String getCategory() {
		return category;
	}

	public int getPages() {
		return pages;
	}

	public String getLanguage() {
		return language;
	}

	public String getAuthor() {
		return author;
	}

	public String getPublisher() {
		return publisher;
	}

	public int getCopies() {
		return copies;
	}

	public void setBookID(int bookID) {
		this.bookID = bookID;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public void setCopies(int copies) {
		this.copies = copies;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

}
