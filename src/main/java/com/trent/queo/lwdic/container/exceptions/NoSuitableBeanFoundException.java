package com.trent.queo.lwdic.container.exceptions;

/**
 * Thrown by {@link com.trent.queo.lwdic.container.LWDIContainer} if no {@link com.trent.queo.lwdic.annotations.Bean}
 * was found that either matches a given custom name or classname.
 */
public class NoSuitableBeanFoundException extends RuntimeException {
	public NoSuitableBeanFoundException(String s) {
		super(s);
	}
}
