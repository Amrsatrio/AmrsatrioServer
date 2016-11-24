package com.amrsatrio.server;

@SuppressWarnings("serial")
public class WrongUsageException extends Exception {
	public WrongUsageException(String a) {
		super(a);
	}
	
	public WrongUsageException() {
		super();
	}
}
