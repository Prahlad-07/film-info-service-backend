package com.cinema.exception;

public class EmptyFileException extends RuntimeException {
   
	/**
	 * @author Prahlad_07
	 */
	private static final long serialVersionUID = 1L;

	public EmptyFileException(String message) {
		super(message);
	}
}
