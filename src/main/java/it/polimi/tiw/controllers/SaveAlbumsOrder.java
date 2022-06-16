package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.lang.String;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;


@WebServlet("/SaveAlbumsOrder")
@MultipartConfig
public class SaveAlbumsOrder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public SaveAlbumsOrder() {
		super();
	}
	
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		User usr = (User) session.getAttribute("user");	
		Integer usrId = usr.getId();
		
		List<Integer> listIds = new ArrayList<Integer>();
		AlbumDAO aDao = new AlbumDAO(connection);
		
		try {
			String[] albumsId = request.getParameter("objarray").split("\\s*,\\s*");
			
			// Check that the ids sent are integer, owned by user and not duplicated
			Set<String> duplicateitems = new HashSet<>();		
							
			for (String stringId : albumsId) {
				Integer id = Integer.parseInt(stringId);
				listIds.add(id);
				
				if(aDao.getAlbumByID(id).getUserId()!=usrId) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Violated access to album!");
					return;
				}
						
				if (!duplicateitems.add(stringId)) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(" Duplicate album id");
					return;
				}
			}
		}catch(Exception ex) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}

		
		if (listIds.size() == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}
		

		Integer sorting = 1;		
		try{
			connection.setAutoCommit(false);
			for(Integer id : listIds){
			    aDao.saveOrder(id, sorting);
				sorting++;
			}
			connection.commit();
			response.setStatus(HttpServletResponse.SC_OK);
			
		} catch (Exception e) {
			try {
					connection.rollback();
			} catch (SQLException errorSQL) {}
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error while saving order");
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