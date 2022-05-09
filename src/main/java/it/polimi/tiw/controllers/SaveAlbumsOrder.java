package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Arrays;
import java.lang.String;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.dao.AlbumDAO;


@WebServlet("/SaveAlbumsOrder")
public class SaveAlbumsOrder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public SaveAlbumsOrder() {
		super();
	}
	
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String objarray = request.getParameter("objarray").toString();

		System.out.println(objarray);
		
		List<String> checkedIds = Arrays.asList(objarray.split("\\s*,\\s*"));
		
		if (checkedIds == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		AlbumDAO aDao = new AlbumDAO(connection);
		Integer sorting = 1;
		Integer albumId;
		
		try{
			connection.setAutoCommit(false);
			
			for (String s : checkedIds)  {
				albumId = Integer.parseInt(s);
				aDao.saveOrder(albumId, sorting);
				sorting++;
			}
			connection.commit();
			response.setStatus(HttpServletResponse.SC_OK);
			
		} catch (Exception e) {
			try {
					connection.rollback();
			} catch (SQLException errorSQL) { errorSQL.printStackTrace();}
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while saving order");
		}		
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}