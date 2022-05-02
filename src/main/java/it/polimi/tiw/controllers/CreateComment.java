package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumImagesDAO;
import it.polimi.tiw.dao.CommentDAO;
import it.polimi.tiw.dao.ImageDAO;
import it.polimi.tiw.exceptions.BadCommentException;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CreateComment")
public class CreateComment extends HttpServlet{
	
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
		Integer imageId = null;
		Integer albumId = null;
		Integer usrID = null;
		String text = null;
		
		User usr = (User) session.getAttribute("user");	
		usrID = usr.getId();
		
		try {
			imageId = Integer.parseInt(request.getParameter("image")); 
			albumId = Integer.parseInt(request.getParameter("album"));
			text = StringEscapeUtils.escapeJava(request.getParameter("text"));
			
			if(text.equals("") || text==null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Your comment cannot be empty");
				return;
			}
		}
		catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}
			
		ImageDAO iDao = new ImageDAO(connection);
		Image img;
		AlbumImagesDAO IaDao = new AlbumImagesDAO(connection);
		CommentDAO cDao = new CommentDAO(connection);
		
		try {
			img = iDao.getImageByID(imageId);
			if(img == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
				return;
			}
			if(!IaDao.checkImageInAlbum(imageId, albumId)) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Mismatching value from album and image, cannot return to album page");
				return;
			}
			cDao.createComment(imageId, text , usrID);
		}
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create comment");
			return;
		} catch (BadCommentException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
			return;
		}
		// OPPURE associare commento a id della tabella albumimages -> da li ho poi imageid e albumid
		
		response.sendRedirect(getServletContext().getContextPath() + "/Album?album=" + albumId + "&image=" + imageId);
		
	}

}
	
