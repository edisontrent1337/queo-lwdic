package com.trent.queo.lwdic.container;

import com.trent.queo.lwdic.container.exceptions.BeanAlreadyDefinedException;
import com.trent.queo.lwdic.examples.*;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LWDIContainerTest {

	private static final String TEST_PACKAGE = "com.trent.queo.lwdic.examples";

	@Test
	public void testContainerPackageScan() {
		LWDIContainer container = new LWDIContainer();
		container.scanPackage(TEST_PACKAGE);
		Map<String, Object> containerBeans = container.getBeans();
		assertEquals("The container does not manage the expected number of beans.", 6, containerBeans.keySet().size());

		DemoInjectionTargetBean injectionTarget = container.getBeanByType(DemoInjectionTargetBean.class);
		assertNotNull("The bean was not registered correctly.", injectionTarget);

		assertInterfaceCompatibility(container);
		assertSuperClassCompatibility(container);

	}

	private void assertInterfaceCompatibility(LWDIContainer container) {
		IDemoBean interfaceDemoBean = container.getBeanByType(IDemoBean.class);
		assertNotNull("The bean was not registered correctly, as this interface implemented by another bean was disregarded " +
				"during registration.", interfaceDemoBean);

		ImplementingDemoBean implementingDemoBean = container.getBeanByType(ImplementingDemoBean.class);
		assertNotNull("The bean was not registered correctly.", implementingDemoBean);
		assertEquals("A bean has to be compatible with the interfaces it implements.", implementingDemoBean, interfaceDemoBean);
	}

	private void assertSuperClassCompatibility(LWDIContainer container) {
		AbstractDemoBean abstractDemoBean = container.getBeanByType(AbstractDemoBean.class);
		assertNotNull("The bean was not registered correctly, as this super class of another bean was disregarded " +
				"during registration.", abstractDemoBean);

		ConcreteDemoBean concreteDemoBean = container.getBeanByType(ConcreteDemoBean.class);
		assertNotNull("The bean was not registered correctly.", concreteDemoBean);
		assertEquals("A bean has to be compatible with it's super class.", abstractDemoBean, concreteDemoBean);

	}

	@Test
	public void testProgrammaticCreationOfBeans() {
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
		container.scanPackage(TEST_PACKAGE);
		container.addBean("a", beanA);
		container.addBean("b", beanB);
		container.injectBeans();

		DemoInjectionTargetBean injectionTargetBean = container.getBeanByType(DemoInjectionTargetBean.class);
		assertNotNull(injectionTargetBean.valueA);
		assertEquals(beanA, injectionTargetBean.valueA);
		assertNotNull(injectionTargetBean.valueB);
		assertEquals(beanB, injectionTargetBean.valueB);

		assertNotNull(injectionTargetBean.abstractDemoBean);

	}


}