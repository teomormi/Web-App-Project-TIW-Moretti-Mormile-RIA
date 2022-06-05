package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.dao.ImageDAO;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/GetImagesList")
@MultipartConfig
public class GetImagesListByAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public GetImagesListByAlbum() {
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
	
		ImageDAO iDao = new ImageDAO(connection);
		Integer IdAlbum;
		
		try {
			IdAlbum = Integer.parseInt(request.getParameter("albumid"));				
		}
		catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}
		
		List<Image> listImages = null;
		
		try {
			listImages = iDao.getImagesFromAlbum(IdAlbum);
		} 
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover images from album");
			return;
		}
		
	    /* Add images as json parameter */
		String json = new Gson().toJson(listImages);		
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		
	}
}
