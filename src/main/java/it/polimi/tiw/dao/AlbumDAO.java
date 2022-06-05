package it.polimi.tiw.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import it.polimi.tiw.beans.Album;


public class AlbumDAO {
	private Connection connection;
	
	public AlbumDAO(Connection connection) {
		this.connection=connection;
	}
	
	public ArrayList<Album> getAlbumsByNotUserID(int id) throws SQLException{
		String query = "SELECT * FROM album WHERE user != ? ORDER BY date DESC";
		ArrayList<Album> albums = new ArrayList<Album>(); 
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery()) {
				while(result.next()) {
					Album album = new Album();
					album.setId(result.getInt("id"));
					album.setTitle(result.getString("title"));
					album.setDate(result.getDate("date"));
					album.setUserId(result.getInt("user"));
					albums.add(album);
				}
			}
		}
		return albums;
	}
	
	public Album getAlbumByID(int id) throws SQLException{
		String query = "SELECT * FROM album WHERE id = ? ORDER BY date DESC";
		Album album = new Album();
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery()) {
				if(!result.isBeforeFirst())
					return null;
				result.next();
				album.setId(result.getInt("id"));
				album.setTitle(result.getString("title"));
				album.setDate(result.getDate("date"));
				album.setUserId(result.getInt("user"));
			}
		}
		return album;
	}
	
	public ArrayList<Album> getAlbumsByUserID(int id) throws SQLException{
		String query = "SELECT * FROM album WHERE user = ? order by case when sorting is null then 1 else 0 end, sorting,id";
		ArrayList<Album> albums = new ArrayList<Album>(); 
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery()) {
				while(result.next()) {
					Album album = new Album();
					album.setId(result.getInt("id"));
					album.setTitle(result.getString("title"));
					album.setDate(result.getDate("date"));
					album.setUserId(result.getInt("user"));
					albums.add(album);
				}
			}
		}
		return albums;
	}
	
	public int createAlbum(String title, int idUser) throws SQLException{
		String query = "INSERT INTO album (title,user) VALUES (?,?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			pstatement.setString(1, title);
			pstatement.setInt(2, idUser);
			pstatement.executeUpdate();
			ResultSet generatedKeys = pstatement.getGeneratedKeys();
			if (generatedKeys.next()) {
				return generatedKeys.getInt(1);
			} else {
				throw new SQLException("Creating album failed, no ID obtained.");
			}
		}
	}
	
	public void saveOrder(int albumId,int sorting) throws SQLException {
		String query = "UPDATE album SET sorting = ? WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			pstatement.setInt(1, sorting);
			pstatement.setInt(2, albumId);
			pstatement.executeUpdate();
		}
	}
	
}
