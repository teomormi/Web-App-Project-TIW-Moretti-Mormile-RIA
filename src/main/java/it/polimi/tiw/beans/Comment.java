package it.polimi.tiw.beans;

import java.util.Date;

public class Comment {
	private int id;
	private String text;
	private String user;
	private int imageId;
	private Date date;
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getUser() { return user; }
	public void setUser(String user) { this.user = user; }

	public String getText() { return text; }
	public void setText(String text) { this.text = text; }
	
	public int getImageId() { return imageId; }
	public void setImageId(int image) { this.imageId = image; }
	
	public Date getDate() { return date; }
	public void setDate(Date date) { this.date = date; }
}
