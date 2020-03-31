package com.studentrade.graph.exception;

public class TextbookException extends Exception {
	public TextbookException() {
	}

	public TextbookException(String message) {
		super(message);
	}

	public TextbookException(String message, Throwable cause) {
		super(message, cause);
	}

	public TextbookException(Throwable cause) {
		super(cause);
	}

	public TextbookException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
