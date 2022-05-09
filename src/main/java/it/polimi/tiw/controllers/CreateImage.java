package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.dao.AlbumImagesDAO;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.ImageDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CreateImage")
@MultipartConfig
public class CreateImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	String folderPath = "";
	
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		folderPath = getServletContext().getInitParameter("outputpath");
	}
	
	public void destroy() {		
		try {
			ConnectionHandler.closeConnection(connection);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession(false);
		
		Integer idUser = null;
		String title = null;
		String description = null;
		String[] checkedIds = null;
		List<Integer> listIds = new ArrayList<Integer>();
		
		Part filePart = request.getPart("file"); 
		User usr = (User) session.getAttribute("user");
		idUser = usr.getId();
		
		try {
			title = StringEscapeUtils.escapeJava(request.getParameter("title"));
			description = StringEscapeUtils.escapeJava(request.getParameter("description"));
			checkedIds = request.getParameterValues("albums");
			
			if(title.equals("") || title==null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Your title cannot be empty");
				return;
			}
			if(description.equals("") || description==null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Your description cannot be empty");
				return;
			}
			if (checkedIds == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Image should have an album");
				return;
			}
			
			// Check that the ids sent are valid == correspond to courses of actual user
			AlbumDAO aDao = new AlbumDAO(connection);
			
			for (String s : checkedIds) {
				Integer id = Integer.parseInt(s);
				if(aDao.getAlbumByID(id).getUserId()!=idUser) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Violated access to album!");
					return;
				}
				
				listIds.add(id);
			}
			if (listIds.size() == 0) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "At least one album must be selected");
					return;
			}
			
			//check the file			
			if (filePart == null || filePart.getSize() <= 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file in request!");
				return;
			}
			
			// Then we check the parameter is valid (in this case right format img)
			String contentType = filePart.getContentType();
			
			if (!contentType.startsWith("image")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File format not permitted");
				return;
			}
		}
		catch (NumberFormatException | SQLException | NullPointerException e ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}
		
		
		
		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
		// folderPath is saved in web.xml
		
		File folder = new File(folderPath);
		if (!folder.exists()) {
			System.out.println("Create folder for image");
			folder.mkdirs();
		}
		
		String outputPath = folderPath + File.separator + fileName;
		System.out.println("Output path: " + outputPath);
		

		File file = new File(outputPath);
		ImageDAO iDao = new ImageDAO(connection);
		AlbumImagesDAO aiDao = new AlbumImagesDAO(connection);
		Image img = null;

		try{
			// check if file with same path already exist
			if(file.exists()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File name already exist");
				return;
			}
			
			InputStream fileContent = filePart.getInputStream(); // get file content
			Files.copy(fileContent, file.toPath()); // copy file
			
			// create image and referenced to album
			connection.setAutoCommit(false);
			iDao.createImage(fileName, description, title, idUser.intValue());
			
			img = iDao.getImageByPath(fileName);
			
			for(Integer id : listIds) {
				aiDao.addImageToAlbum(img.getId(), id);
			}
			connection.commit();
			
			System.out.println("File saved correctly!");
			response.setStatus(HttpServletResponse.SC_OK);	
			
		} catch (Exception e) {
			try {
					connection.rollback();
			} catch (SQLException errorSQL) { errorSQL.printStackTrace();}
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while saving file");
		}		
		
		
	}

}

