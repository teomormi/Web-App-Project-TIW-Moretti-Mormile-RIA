package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AlbumImagesDAO {
private Connection connection;
	
	public AlbumImagesDAO(Connection connection) {
		this.connection = connection;
	}

	public void addImageToAlbum(int imgId, int albumId) throws SQLException {
		String query = "INSERT into albumimages (image, album) VALUES (?, ?)";
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, imgId);
			pstatement.setInt(2, albumId);
			pstatement.executeUpdate();
		}
	}
	
	public boolean checkImageInAlbum(int imgId,int albumId) throws SQLException {
		String query = "SELECT * FROM albumimages WHERE image = ? AND album = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, imgId);
			pstatement.setInt(2, albumId);
			try(ResultSet result = pstatement.executeQuery()){
				if(!result.isBeforeFirst())
					return false;
				return true;
			}	
		}
	}
}
