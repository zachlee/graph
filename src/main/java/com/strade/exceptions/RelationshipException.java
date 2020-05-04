package com.strade.exceptions;

public class RelationshipException extends Exception {
	public RelationshipException() {
	}

	public RelationshipException(String message) {
		super(message);
	}

	public RelationshipException(String message, Throwable cause) {
		super(message, cause);
	}

	public RelationshipException(Throwable cause) {
		super(cause);
	}

	public RelationshipException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
