package com.trent.queo.lwdic.container.exceptions;

/**
 * Thrown by {@link com.trent.queo.lwdic.container.LWDIContainer} when attempting to use the same bean name twice.
 */
public class BeanAlreadyDefinedException extends RuntimeException {

	public BeanAlreadyDefinedException(String beanName) {
		super("Cannot create bean '" + beanName + "' \n.A bean with the name '" + beanName + "' is already defined.");
	}
}
