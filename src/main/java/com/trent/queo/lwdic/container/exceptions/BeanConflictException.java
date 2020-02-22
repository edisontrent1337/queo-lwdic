package com.trent.queo.lwdic.container.exceptions;

/**
 * Thrown, when the {@link com.trent.queo.lwdic.container.LWDIContainer} can not decide which object
 * should be injected to a class.
 */
public class BeanConflictException extends RuntimeException {
	public BeanConflictException(String beanName) {
		super("More than one bean was found for class " + beanName + ".");
	}
}
