package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/GetAlbumsList")
public class GetAlbumsList extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public GetAlbumsList() {
		super();
	}
	
	
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	public void destroy() {		
		try {
			ConnectionHandler.closeConnection(connection);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		
	
		AlbumDAO aDao = new AlbumDAO(connection);
		ArrayList<Album> albums = null;
		ArrayList<Album> albumsMine = null;
		
		HttpSession session = request.getSession();
		
		User usr = (User) session.getAttribute("user");	
		Integer usrId = usr.getId();
		
		try {
			albums = aDao.getAlbumsByNotUserID(usrId);
			albumsMine = aDao.getAlbumsByUserID(usrId);
		}
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover albums");
			return;
		}
		
		
		/* Add albums as json parameter */
		
		String json1 = new Gson().toJson(albums); 
		String json2 = new Gson().toJson(albumsMine); 
		String json3 = new Gson().toJson(usr); // return also username
		response.setContentType("application/json"); 
		response.setCharacterEncoding("utf-8"); 
		String bothJson = "["+json1+","+json2+","+json3+"]"; //Put both objects in an array of 2 elements
		response.getWriter().write(bothJson);
		
	}
}
