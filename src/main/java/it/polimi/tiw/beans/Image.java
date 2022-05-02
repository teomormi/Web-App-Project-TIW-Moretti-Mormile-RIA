package it.polimi.tiw.beans;

import java.util.Date;

public class Image {
	private int id;
	private String title;
	private String description;
	private Date date;
	private String path;
	private int userId; 
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	
	public Date getDate() { return date; }
	public void setDate(Date date) { this.date = date; }
	
	public String getPath() { return path; }
	public void setPath(String src) { this.path = src; }
	
	public int getUserId() { return userId; }
	public void setUserId(int album) { this.userId = album; }		
}
