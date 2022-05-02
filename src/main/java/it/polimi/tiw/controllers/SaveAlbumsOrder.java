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


		for (String s : checkedIds) {
			Integer id = Integer.parseInt(s);
			System.out.println(id);
			// funziona, bisogna aggiornare repo
		}
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}