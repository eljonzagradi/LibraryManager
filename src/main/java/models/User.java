package models;

public class User {
	
	private int userID;
	private String username;
	private String email;
	private String category;
	private boolean isModified;
	
	public User(int userID, String username, String email, String category) {
		super();
		this.userID = userID;
		this.username = username;
		this.email = email;
		this.category = category;
		this.isModified = false;
	}

	public int getUserID() {
		return userID;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public String getCategory() {
		return category;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public boolean isModified() {
		return isModified;
	}

	public void setModified(boolean isModified) {
		this.isModified = isModified;
	}
	
}
