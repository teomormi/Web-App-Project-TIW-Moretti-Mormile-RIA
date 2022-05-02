package it.polimi.tiw.exceptions;

public class BadImageException extends Exception {
	private static final long serialVersionUID = 1L;
	public BadImageException(String message) {
		super(message);
	}	
}