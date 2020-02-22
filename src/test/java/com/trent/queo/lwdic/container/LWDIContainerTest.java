package com.trent.queo.lwdic.container;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LWDIContainerTest {

	@Test
	public void testTestsWork() {
		assertTrue(true);
	}

	@Test
	public void testAddPackageToContainer() {
		String packageName = "com.trent.queo.lwdic.support";
		LWDIContainer container = new LWDIContainer();
		container.addPackage(packageName);

		Integer beanA = 0;
		Integer beanB = 1;
		container.addBean("a", beanA);
		container.addBean("b", beanB);
		container.scanPackage(packageName);

		Map<String, Object> containerBeans = container.getBeans();
		assertEquals(3, containerBeans.keySet().size());
	}

	@Test
	public void testNamedInjection() {
		Integer beanA = 0;
		Integer beanB = 1;

		LWDIContainer container = new LWDIContainer();
	}

}