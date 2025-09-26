package com.cinema.exception;

public class FileExistsException extends RuntimeException {

	/**
	 * @author Prahlad_07
	 */
	private static final long serialVersionUID = 1L;

	public FileExistsException(String message) {
		super(message);
	}

	
}
