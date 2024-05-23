package com.taizo.exception;

/**
 * Exception thrown when particular resource does not exist. 
 *
 */
public class ResourceNotFoundException extends Exception {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message){
		super(message);
	}
}
