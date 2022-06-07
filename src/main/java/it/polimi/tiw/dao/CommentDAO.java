package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.lang.String;
import java.nio.charset.Charset;


import it.polimi.tiw.beans.Comment;

public class CommentDAO {
	private Connection connection;
	
	public CommentDAO(Connection connection) {
		this.connection = connection;
	}
	
	public ArrayList<Comment> getCommentsFromImages(int idImg) throws SQLException{
		String query = "SELECT c.id,text,date,image,username FROM comment as c,user as u where c.user=u.id and image = ? ORDER BY date DESC";
		ArrayList<Comment> comments = new ArrayList<Comment>();
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, idImg);
			try(ResultSet result = pstatement.executeQuery()){
				while(result.next()) {
					Comment comment = new Comment();
					comment.setId(result.getInt("id"));
					comment.setText(result.getString("text"));
					comment.setDate(result.getDate("date"));
					comment.setImageId(result.getInt("image"));
					comment.setUser(result.getString("username"));
					comments.add(comment);
				}
			}
		}
		return comments;
	}
	
	public void createComment(int idImg, String comment, int idUser) throws SQLException {
		String query = "INSERT into comment (user, text, image) VALUES (?, ?, ?)";
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, idUser);
			pstatement.setString(2,new String(comment.getBytes(Charset.forName("UTF-8")), Charset.forName("UTF-8")));
			pstatement.setInt(3, idImg);
			pstatement.executeUpdate();
		}	
	}

}
