package it.polimi.tiw.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.beans.Image;
import it.polimi.tiw.exceptions.BadImageException;

public class ImageDAO {
	private Connection connection;
	
	public ImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public ArrayList<Image> getImagesFromAlbum(int idAlbum) throws SQLException{
		ArrayList<Image> images = new ArrayList<Image>();
		String query = "SELECT image.* FROM image,albumimages WHERE album = ? AND image.id = albumimages.image ORDER BY date DESC";
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, idAlbum);
			try(ResultSet result = pstatement.executeQuery()){
				while(result.next()) {
					Image img = new Image();
					img.setId(result.getInt("id"));
					img.setDescription(result.getString("description"));
					img.setPath(result.getString("path"));
					img.setTitle(result.getString("title"));
					img.setDate(result.getDate("date"));
					img.setUserId(result.getInt("user"));
					images.add(img);
				}
			}
		}
		return images;
	}
	
	public Image getImageByID(int id) throws SQLException{
		String query = "SELECT * FROM image WHERE id = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery()){
				if(!result.isBeforeFirst())
					return null;
				result.next();
				Image img = new Image();
				img.setId(result.getInt("id"));
				img.setDescription(result.getString("description"));
				img.setPath(result.getString("path"));
				img.setTitle(result.getString("title"));
				img.setUserId(result.getInt("user"));
				img.setDate(result.getDate("date"));
				return img;
			}
		}
		
	}
	
	public Image getImageByPath(String path) throws SQLException{
		
		String query = "SELECT * FROM image WHERE path = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setString(1, path);
			try(ResultSet result = pstatement.executeQuery()){
				if(!result.isBeforeFirst())
					return null;
				result.next();
				Image img = new Image();
				img.setId(result.getInt("id"));
				img.setDescription(result.getString("description"));
				img.setPath(result.getString("path"));
				img.setTitle(result.getString("title"));
				img.setUserId(result.getInt("user"));
				img.setDate(result.getDate("date"));
				return img;
			}
		}	
	}
	
	
	public void createImage(String path, String description,String title,int idUser) throws BadImageException, SQLException {
		if(path==null || path.equals(""))
			throw new BadImageException("Path isn't valid");
		if(description==null || description.equals(""))
			throw new BadImageException("Description isn't valid");
		if(title==null || title.equals(""))
			throw new BadImageException("Title isn't valid");
		
		String query = "INSERT into image (path, description, title, user) VALUES (?, ?, ?, ?)";
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setString(1, path);
			pstatement.setString(2, description);
			pstatement.setString(3, title);
			pstatement.setInt(4, idUser);
			pstatement.executeUpdate();		
		}
	}
	
}
