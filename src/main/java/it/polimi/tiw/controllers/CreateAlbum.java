package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;


import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.exceptions.BadAlbumException;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CreateAlbum")
@MultipartConfig
public class CreateAlbum extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
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
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		
		HttpSession session = request.getSession(false);
		
		Integer idUser = null;
		String title = null;
		
		User usr = (User) session.getAttribute("user");
		idUser = usr.getId();
		
		try {
			title = StringEscapeUtils.escapeJava(request.getParameter("title"));
			if(title.equals("") || title==null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Your comment cannot be empty");
				return;
			}
		}
		catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}
		
		// Create album in DB
		
		AlbumDAO aDao = new AlbumDAO(connection);
		int newAlbumId;
		try {
			newAlbumId = aDao.createAlbum(title,idUser);
		} catch (SQLException |BadAlbumException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to create album");
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(newAlbumId);
			
	}

}
