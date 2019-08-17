package com.strade.exceptions;

public class UserDoesntOwnTextbookException extends UserException {
	public UserDoesntOwnTextbookException(String message) {
		super(message);
	}
}
