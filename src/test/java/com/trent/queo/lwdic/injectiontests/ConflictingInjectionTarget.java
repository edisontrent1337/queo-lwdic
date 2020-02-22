package com.trent.queo.lwdic.injectiontests;

import com.trent.queo.lwdic.annotations.Bean;
import com.trent.queo.lwdic.annotations.Inject;

@Bean
public class ConflictingInjectionTarget {

	@Inject
	public BeanA beanA;

	@Inject
	public BeanB beanB;

	// This dependency injection will fail because BeanA and BeanB are both implementing TestInterface.
	@Inject
	public TestInterface testInterface;


}
