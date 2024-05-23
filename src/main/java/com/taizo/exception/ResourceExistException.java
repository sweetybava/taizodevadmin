package com.taizo.exception;

/**
 * 
 * This exception is thrown when the resource already exist while
 * creating new one.
 *
 */
public class ResourceExistException extends Exception {

	private static final long serialVersionUID = -8990882496319705231L;

	public ResourceExistException(String message) {
		super(message);
	}
}
