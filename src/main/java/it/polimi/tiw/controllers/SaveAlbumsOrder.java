package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.HashSet;
import java.lang.String;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.beans.Album;
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

		String csvString = request.getParameter("objarray");
		
		String[] numberString = csvString.split("\\s*,\\s*");
		int[] numbers = new int[numberString.length];
		
		int index = 0;
		for(int i = 0;i < numberString.length;i++){
		    try{
		        numbers[index] = Integer.parseInt(numberString[i]);
		        index++;
		    }catch (Exception e){
		    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Please insert a valid album id");
				return;
		    }
		}
		
		if (numbers.length == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}
		
		// Id duplicati
		Set<String> items = new HashSet<>();
		Set<String> duplicateItems = Arrays.asList(numberString).stream()
			 		.filter(id -> !items.add(id)) // Set.add() returns false if the element was already in the set.
			 		.collect(Collectors.toSet());
		
		if(duplicateItems.size( )> 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Duplicate album id");
			return;
		}
		
		AlbumDAO aDao = new AlbumDAO(connection);
		Album album;
		
		// owner album
		try {
			
			for(int i = 0;i < numbers.length;i++){
		   		album = aDao.getAlbumByID(numbers[i]);
				if(album.getUserId() != usrId) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Violated access to album");
					return;
			    }
			}
			
		} catch (SQLException e) {
			// id di un album non esistente
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Album value not correct");
			return;
		}
		
		Integer sorting = 1;
		
		try{
			connection.setAutoCommit(false);
			for(int i = 0;i < numbers.length;i++){
			    aDao.saveOrder(numbers[i], sorting);
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