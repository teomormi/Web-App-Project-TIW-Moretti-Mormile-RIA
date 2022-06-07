package it.polimi.tiw.utils;

public class InputValidator {
	public static boolean isStringValid(String s) {
		if(s==null || s.equals(""))
			return false;
		if(s.trim().length() == 0)
			return false;
		return true;
	}
}
