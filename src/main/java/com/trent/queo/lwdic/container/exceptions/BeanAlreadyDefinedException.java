package com.trent.queo.lwdic.container.exceptions;

public class BeanAlreadyDefinedException extends RuntimeException {

	public BeanAlreadyDefinedException(String beanName) {
		super("Cannot create bean '" + beanName + "' \n.A bean with the name '" + beanName + "' is already defined.");
	}
}
