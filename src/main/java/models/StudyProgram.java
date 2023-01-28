package models;

public class StudyProgram {
	
	private int studyprogramID;
	private String name;
	private int duration;
	private String faculty;
	private int noStudents;
	
	public StudyProgram(int studyprogramID, String name, int duration, String faculty, int noStudents) {
		super();
		this.studyprogramID = studyprogramID;
		this.name = name;
		this.duration = duration;
		this.faculty = faculty;
		this.noStudents = noStudents;
	}

	public int getStudyprogramID() {
		return studyprogramID;
	}

	public String getName() {
		return name;
	}

	public int getDuration() {
		return duration;
	}

	public String getFaculty() {
		return faculty;
	}

	public int getNoStudents() {
		return noStudents;
	}

	public void setNoStudents(int noStudents) {
		this.noStudents = noStudents;
	}

	public void setStudyprogramID(int studyprogramID) {
		this.studyprogramID = studyprogramID;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setFaculty(String faculty) {
		this.faculty = faculty;
	}
	
}
