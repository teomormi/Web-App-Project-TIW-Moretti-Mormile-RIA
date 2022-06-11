package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import it.polimi.tiw.dao.AlbumImagesDAO;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.ImageDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.InputValidator;

@WebServlet("/CreateImage")
@MultipartConfig
public class CreateImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	String folderPath = "";
	
	public CreateImage() {
		super();
	}
	
	
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
		
		HttpSession session = request.getSession();
		
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
			
			if(!InputValidator.isStringValid(title)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Your title cannot be empty");
				
				return;
			}
			if(!InputValidator.isStringValid(description)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Your description cannot be empty");
				return;
			}
			if (checkedIds == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Image should have an album");
				return;
			}
			
			// Check that the ids sent are valid == correspond to album of actual user
			AlbumDAO aDao = new AlbumDAO(connection);
			
			for (String s : checkedIds) {
				Integer id = Integer.parseInt(s);
				if(aDao.getAlbumByID(id).getUserId()!=idUser) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Violated access to album!");
					return;
				}
				
				listIds.add(id);
			}
			
			// Id duplicati
			Set<String> items = new HashSet<>();
			Set<String> duplicateItems = Arrays.asList(checkedIds).stream()
				 		.filter(id -> !items.add(id)) // Set.add() returns false if the element was already in the set.
				 		.collect(Collectors.toSet());
			
			if(duplicateItems.size( )> 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(" Duplicate album id");
				return;
			}
			
			if (listIds.size() == 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println( "At least one album must be selected");
			}
			
			//check the file			
			if (filePart == null || filePart.getSize() <= 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Missing file in request!");
				return;
			}
			
			// Then we check the parameter is valid (in this case right format img)
			String contentType = filePart.getContentType();
			
			if (!contentType.startsWith("image")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("File format not permitted");
				return;
			}
		}
		catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
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
		Integer newImgId = null;

		try{
			// check if file with same path already exist
			if(file.exists()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println( "File name already exist");
				return;
			}
			
			InputStream fileContent = filePart.getInputStream(); // get file content
			Files.copy(fileContent, file.toPath()); // copy file
			
			// create image on db and referenced to album
			connection.setAutoCommit(false);
			newImgId = iDao.createImage(fileName, description, title, idUser.intValue());
			
			for(Integer id : listIds) {
				aiDao.addImageToAlbum(newImgId, id);
			}
			connection.commit();
			
			System.out.println("File saved correctly!");	
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(listIds.get(0));	
			
		} catch (Exception e) {
			try {
					connection.rollback();
			} catch (SQLException errorSQL) { errorSQL.printStackTrace();}
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error while saving file");
		}		
		
		
	}

}

