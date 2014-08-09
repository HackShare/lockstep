package com.picostuff.lockstep.exception;

public class BadPathException extends Exception {
	public BadPathException(String path) {
		super(path);
	}
}
