package com.cinema.exception;

public class MovieNotFoundException extends RuntimeException {

	/**
	 * @author Prahlad_07
	 */
	private static final long serialVersionUID = 1L;

	public MovieNotFoundException(String message) {
		super(message);
	}
}
