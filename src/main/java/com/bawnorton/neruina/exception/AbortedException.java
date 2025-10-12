package com.bawnorton.neruina.exception;

public class AbortedException extends InterruptedException {
	public AbortedException() {
	}

	public AbortedException(String message) {
		super(message);
	}
}
