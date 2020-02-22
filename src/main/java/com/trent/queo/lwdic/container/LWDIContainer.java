package com.trent.queo.lwdic.container;

import com.trent.queo.lwdic.annotations.Bean;
import com.trent.queo.lwdic.annotations.Inject;
import com.trent.queo.lwdic.annotations.Named;
import com.trent.queo.lwdic.container.exceptions.BeanAlreadyDefinedException;
import com.trent.queo.lwdic.container.exceptions.BeanConflictException;
import com.trent.queo.lwdic.container.exceptions.NoSuitableBeanFoundException;
import io.github.classgraph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A light weight dependency injection container written for the queo coding challenge.
 * @author Sinthujan Thanabalasingam
 */
@SuppressWarnings("WeakerAccess")
public class LWDIContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LWDIContainer.class);

	private Map<String, Set<Object>> beans;

	public LWDIContainer() {
		this.beans = new HashMap<>();
	}

	/**
	 * Scans a given package and adds instances for all classes that are annotated with {@link Bean} to the container.
	 *
	 * @param packageName the package to be scanned.
	 */
	public void scanPackage(String packageName) {

		LOGGER.info("Scanning package {}...", packageName);
		String beanAnnotation = Bean.class.getName();
		try (ScanResult scanResult = new ClassGraph()
				.enableAllInfo()
				.whitelistPackages(packageName)
				.scan()) {
			for (ClassInfo beanClassInfo : scanResult.getClassesWithAnnotation(beanAnnotation)) {

				AnnotationParameterValueList beanAnnotationParameters = beanClassInfo.getAnnotationInfo(Bean.class.getName()).getParameterValues();
				if (beanAnnotationParameters != null) {
					Boolean injectable = (Boolean) beanAnnotationParameters.get(0).getValue();
					if (!injectable) {
						LOGGER.info("Skipping bean {} with flag injectable=false.", beanClassInfo.getName());
						continue;
					}
				}

				String beanClassName = beanClassInfo.getName();
				Object instanceOfBean = createInstanceForClass(beanClassName);

				AnnotationInfo namedAnnotationInfo = beanClassInfo.getAnnotationInfo(Named.class.getName());
				if (namedAnnotationInfo != null) {
					processNamedBean(beanClassName, instanceOfBean, namedAnnotationInfo);
				} else {
					LOGGER.info("Registering bean of type {} with name {}.", beanClassName, beanClassName);
					if (beanClassInfo.isInterface() || beanClassInfo.isAbstract()) {
						addInstanceToBean(beanClassName, instanceOfBean);
					} else {
						addBean(beanClassName, instanceOfBean);
					}
				}
				processBeanInterfaces(beanClassInfo, instanceOfBean);
				processBeanSuperclass(beanClassInfo, instanceOfBean);
			}
		}
	}

	private void processBeanSuperclass(ClassInfo beanClassInfo, Object instanceOfBean) {
		ClassInfo superclassInfo = beanClassInfo.getSuperclass();
		if (superclassInfo != null) {
			String superClassName = superclassInfo.getName();
			LOGGER.info("Registering bean of type {} with name {}.", superClassName, superClassName);
			addInstanceToBean(superClassName, instanceOfBean);
		}
	}

	private void processBeanInterfaces(ClassInfo beanClassInfo, Object instanceOfBean) {
		for (ClassInfo interfaceClassInfo : beanClassInfo.getInterfaces()) {
			String interfaceName = interfaceClassInfo.getName();
			LOGGER.info("Registering bean of type {} with name {}.", interfaceName, interfaceName);
			addInstanceToBean(interfaceName, instanceOfBean);
		}
	}

	private void processNamedBean(String beanClassName, Object instanceOfBean, AnnotationInfo namedAnnotationInfo) {
		List<AnnotationParameterValue> namedAnnotationParameterValues = namedAnnotationInfo.getParameterValues();
		if (namedAnnotationParameterValues.size() == 1) {
			String beanName = (String) namedAnnotationParameterValues.get(0).getValue();
			LOGGER.info("Registering bean of type {} with name {}.", beanClassName, beanName);
			addBean(beanName, instanceOfBean);
		}
	}

	/**
	 * Performs dependency injection to all {@link Bean}s that have fields annotated with {@link Inject}.
	 */
	private void injectBeans() {

		Set<Object> beanObjects = new HashSet<>();
		beans.values().forEach(beanObjects::addAll);

		for (Object bean : beanObjects) {
			Field[] fields = bean.getClass().getFields();
			for (Field field : fields) {
				processAnnotations(bean, field);
			}
		}
	}

	private void processAnnotations(Object bean, Field field) {
		Annotation[] annotations = field.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof Inject && annotations.length == 1) {
				injectBean(bean, field);
			}
			if (annotation instanceof Named) {
				injectNamedBean(bean, ((Named) annotation).name(), field);
			}
		}
	}

	private void injectNamedBean(Object targetBean, String name, Field field) {
		Object bean = getBeanByNameAndType(name, field.getType());
		field.setAccessible(true);
		try {
			field.set(targetBean, bean);
		} catch (IllegalAccessException e) {
			LOGGER.error("An error occurred while injecting bean {} to bean {}", name, targetBean);
			e.printStackTrace();
		}
	}

	private void injectBean(Object targetBean, Field field) {
		Object bean = getBeanByType(field.getType());
		field.setAccessible(true);
		try {
			field.set(targetBean, bean);
		} catch (IllegalAccessException e) {
			LOGGER.error("An error occurred while injecting bean {} to bean {}", bean, targetBean);
			e.printStackTrace();
		}
	}

	private Object createInstanceForClass(String className) {
		Class<?> clazz;
		try {
			clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getConstructor();
			return constructor.newInstance();
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			LOGGER.error("An error occurred while creating an instance for bean {}.", className);
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
			LOGGER.error("An error occurred while creating an instance for bean {}.", className);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Starts the container and injects all beans.
	 */
	public void start() {
		injectBeans();
	}

	/**
	 * Registers a bean under the given name.
	 *
	 * @param beanName The name of the bean
	 * @param bean     The bean to be registered.
	 */
	public void addBean(String beanName, Object bean) {
		if (beans.containsKey(beanName)) {
			throw new BeanAlreadyDefinedException(beanName);
		}
		Set<Object> beanObjects = new HashSet<>();
		beanObjects.add(bean);
		beans.put(beanName, beanObjects);
	}

	private void addInstanceToBean(String beanName, Object bean) {
		Set<Object> beanObjects = beans.get(beanName);
		if (beanObjects == null) {
			beanObjects = new HashSet<>();
		}
		beanObjects.add(bean);
		beans.put(beanName, beanObjects);
	}

	public Map<String, Set<Object>> getBeans() {
		return this.beans;
	}

	/**
	 * @param beanType the type of the {@link Bean}
	 * @return the {@link Bean} registered by a given name.
	 */
	public <T> T getBeanByType(Class<T> beanType) {
		String beanClassName = beanType.getName();
		if (!beans.containsKey(beanClassName)) {
			throw new NoSuitableBeanFoundException("No suitable bean was found for " + beanClassName + ".");
		}
		Object result = retrieveBeanObject(beanClassName);
		if (!beanType.isInstance(result)) {
			throw new NoSuitableBeanFoundException("No suitable bean was found for " + beanClassName + ".");
		} else {
			return beanType.cast(result);
		}
	}

	public <T> T getBeanByNameAndType(String beanName, Class<T> beanClass) {
		if (!beans.containsKey(beanName)) {
			throw new NoSuitableBeanFoundException("No bean with the name " + beanName + " was found");
		}

		Object result = retrieveBeanObject(beanName);
		if (!beanClass.isInstance(result)) {
			throw new NoSuitableBeanFoundException("No bean named " + beanName + " of type " + beanClass.getName() + " was found.");
		}
		return beanClass.cast(result);

	}

	private Object retrieveBeanObject(String beanName) {
		Set<Object> beanObjects = beans.get(beanName);
		if (beanObjects.size() == 0) {
			throw new NoSuitableBeanFoundException("No suitable bean was found for " + beanName + ".");
		}
		if (beanObjects.size() > 1) {
			throw new BeanConflictException(beanName);
		}
		return beanObjects.toArray()[0];
	}

}
