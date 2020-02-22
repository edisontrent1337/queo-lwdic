package com.trent.queo.lwdic.container;

import com.trent.queo.lwdic.container.exceptions.BeanAlreadyDefinedException;
import com.trent.queo.lwdic.examples.DemoImplementingBean;
import com.trent.queo.lwdic.examples.DemoInjectionTargetBean;
import com.trent.queo.lwdic.examples.IDemoBean;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class LWDIContainerTest {

	@Test
	public void testContainerPackageScan() {
		String packageName = "com.trent.queo.lwdic.examples";
		LWDIContainer container = new LWDIContainer();
		container.scanPackage(packageName);
		Map<String, Object> containerBeans = container.getBeans();
		assertEquals("The container does not manage the expected number of beans.", 4, containerBeans.keySet().size());

		DemoInjectionTargetBean injectionTarget = container.getBeanByType(DemoInjectionTargetBean.class);
		assertNotNull("The bean was not registered correctly.", injectionTarget);

		IDemoBean interfaceDemoBean = container.getBeanByType(IDemoBean.class);
		assertNotNull("The bean was not registered correctly, as interfaces implemented by beans were disregarded " +
				"during registration.", interfaceDemoBean);

		DemoImplementingBean demoImplementingBean = container.getBeanByType(DemoImplementingBean.class);
		assertNotNull("The bean was not registered correctly.", demoImplementingBean);
		assertEquals(demoImplementingBean,interfaceDemoBean);
	}

	@Test
	public void testProgrammaticAdditionOfBeans() {
		LWDIContainer container = new LWDIContainer();
		Integer beanA = 0;
		Integer beanB = 1;
		container.addBean("a", beanA);
		container.addBean("b", beanB);

		Map<String, Object> containerBeans = container.getBeans();
		assertEquals("The container does not manage the expected number of beans.", 2, containerBeans.keySet().size());

		assertEquals(beanA, container.getBeanByNameAndType("a", Integer.class));
		assertEquals(beanB, container.getBeanByNameAndType("b", Integer.class));
	}

	@Test(expected = BeanAlreadyDefinedException.class)
	public void testFailureOnDuplicateBeanRegistration() {
		LWDIContainer container = new LWDIContainer();
		Integer beanA = 0;
		Integer beanB = 1;
		container.addBean("a", beanA);
		container.addBean("a", beanB);
	}

	@Test
	public void testNamedInjection() {
		Integer beanA = 0;
		Integer beanB = 1;

		LWDIContainer container = new LWDIContainer();
	}

}