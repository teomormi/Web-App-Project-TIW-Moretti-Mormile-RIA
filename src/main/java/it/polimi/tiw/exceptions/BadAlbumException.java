package it.polimi.tiw.exceptions;

public class BadAlbumException extends Exception {
	
	private static final long serialVersionUID = 1L;
	public BadAlbumException(String message) {
		super(message);
	}
}
