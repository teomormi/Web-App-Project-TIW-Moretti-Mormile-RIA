package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import it.polimi.tiw.beans.Comment;
import it.polimi.tiw.dao.CommentDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/GetCommentsList")
public class GetCommentsList extends HttpServlet {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/* Extracts parameter from GET */		
		Integer imageId = null;
		try {
			imageId = Integer.parseInt(request.getParameter("image"));
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}		

		/* Fetches the selected image comments */
		CommentDAO cDAO = new CommentDAO(connection);
		List<Comment> comments = null;
		try {
			comments = cDAO.getCommentsFromImages(imageId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Not possible to recover comments");			
			return;
		}
			
		/* Add comments as json parameter */
		String json = new Gson().toJson(comments);		
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		
	}
}