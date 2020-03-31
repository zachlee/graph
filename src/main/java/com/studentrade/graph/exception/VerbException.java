package com.studentrade.graph.exception;

public class VerbException extends Exception {
	public VerbException(String message) {
		super(message);
	}

	public VerbException(String message, Throwable cause) {
		super(message, cause);
	}

	public VerbException(Throwable cause) {
		super(cause);
	}

	public VerbException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public VerbException() {
	}
}
