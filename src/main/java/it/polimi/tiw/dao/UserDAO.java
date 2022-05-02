package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.exceptions.BadUserException;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public User checkLogin(String username, String email, String password) throws SQLException{
		String query = "SELECT * FROM user where (username = ? OR email = ?)  AND password = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setString(1,username);
			pstatement.setString(2, email);
			pstatement.setString(3,password);
			try(ResultSet result = pstatement.executeQuery();){
				if(!result.isBeforeFirst()) //user not present in DB
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					return user;
				}
			}
		}
		
	}
	
	public User getUserByUsername(String username) throws SQLException {
		String query = "SELECT * FROM user where username = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setString(1,username);
			try(ResultSet result = pstatement.executeQuery();){
				if(!result.isBeforeFirst()) //user not present in DB
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					return user;
				}
			}
		}
		
	}
	
	public boolean isMailAvailable(String mail) throws SQLException {
		String query = "SELECT * FROM user where email = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setString(1,mail);
			try(ResultSet result = pstatement.executeQuery();){
				if(!result.isBeforeFirst())
					return true;
				return false;
			}
		}
	}
	public boolean isUsernameAvailable(String username) throws SQLException {
		String query = "SELECT * FROM user where username = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setString(1,username);
			try(ResultSet result = pstatement.executeQuery();){
				if(!result.isBeforeFirst())
					return true;
				return false;
				
			}
		}
	}
	
	public void registerUser(String username, String email, String password) throws SQLException, BadUserException {
		if(username == null || username.equals(""))
			throw new BadUserException("Not valid username");
		if(email == null || email.equals(""))
			throw new BadUserException("Not valid email");
		if(password == null || password.equals(""))
			throw new BadUserException("Not valid password");
		
			
		
		String query = "INSERT into user (username, email, password) VALUES(?, ?, ?)";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, username);
			pstatement.setString(2, email);
			pstatement.setString(3, password);
			pstatement.executeUpdate();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}
	
	
}
