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
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class LWDIContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LWDIContainer.class);

	private Map<String, Set<Object>> beans;
	private Set<String> packages;

	public LWDIContainer() {
		this.beans = new HashMap<>();
		this.packages = new HashSet<>();
	}

	/**
	 * Scans a given package and adds instances for all classes that are annotated with {@link Bean} to the container.
	 *
	 * @param packageName the package to be scanned.
	 */
	public void scanPackage(String packageName) {
		LOGGER.info("Scanning package {}...", packageName);
		this.packages.add(packageName);
		String beanAnnotation = Bean.class.getName();
		try (ScanResult scanResult =
					 new ClassGraph()
							 // .verbose()
							 .enableAllInfo()
							 .whitelistPackages(packageName)
							 .scan()) {
			for (ClassInfo beanClassInfo : scanResult.getClassesWithAnnotation(beanAnnotation)) {
				AnnotationInfo namedAnnotationInfo = beanClassInfo.getAnnotationInfo(Named.class.getName());
				String beanClassName = beanClassInfo.getName();
				Object instanceOfBean = createInstanceForClass(beanClassName);
				if (namedAnnotationInfo != null) {
					List<AnnotationParameterValue> beanParameterValues = namedAnnotationInfo.getParameterValues();
					if (beanParameterValues.size() == 1) {
						String beanName = (String) beanParameterValues.get(0).getValue();
						LOGGER.info("Registering bean of type {} with name {}.", beanClassName, beanName);
						addBean(beanName, instanceOfBean);
					}
				} else {
					LOGGER.info("Registering bean of type {} with name {}.", beanClassName, beanClassName);
					addBean(beanClassName, instanceOfBean);
				}
				for (ClassInfo interfaceClassInfo : beanClassInfo.getInterfaces()) {
					String interfaceName = interfaceClassInfo.getName();
					LOGGER.info("Registering bean of type {} with name {}.", interfaceName, interfaceName);
					addInstanceToBean(interfaceName, instanceOfBean);
				}
				ClassInfo superclassInfo = beanClassInfo.getSuperclass();
				if (superclassInfo != null) {
					String superClassName = superclassInfo.getName();
					LOGGER.info("Registering bean of type {} with name {}.", superClassName, superClassName);
					addInstanceToBean(superClassName, instanceOfBean);
				}
			}
		}
	}

	private void injectBeans() {

		Set<Object> beanObjects = new HashSet<>();
		beans.values().forEach(beanObjects::addAll);

		for (Object bean : beanObjects) {
			Field[] fields = bean.getClass().getFields();
			for (Field field : fields) {
				Set<Annotation> annotations = Arrays.stream(field.getAnnotations()).collect(Collectors.toSet());
				for (Annotation annotation : annotations) {
					if (annotation instanceof Inject && annotations.size() == 1) {
						injectBean(bean, field);
					}
					if (annotation instanceof Named) {
						injectNamedBean(bean, ((Named) annotation).name(), field);
					}
				}
			}
		}
	}

	private void injectNamedBean(Object targetBean, String name, Field field) {
		Object bean = getBeanByNameAndType(name, field.getType());
		field.setAccessible(true);
		try {
			field.set(targetBean, bean);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void injectBean(Object targetBean, Field field) {
		Object bean = getBeanByType(field.getType());
		field.setAccessible(true);
		try {
			field.set(targetBean, bean);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private Object createInstanceForClass(String className) {
		Class<?> clazz;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		Constructor<?> constructor = null;
		if (clazz != null) {
			try {
				constructor = clazz.getConstructor();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				return null;
			}
		}

		if (constructor != null) {
			try {
				return constructor.newInstance();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
				return null;
			}
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
		Object result = retrieveBeanObjects(beanClassName);
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

		Object result = retrieveBeanObjects(beanName);
		if (!beanClass.isInstance(result)) {
			throw new NoSuitableBeanFoundException("No bean named " + beanName + " of type " + beanClass.getName() + " was found.");
		}
		return beanClass.cast(result);

	}

	private Object retrieveBeanObjects(String beanName) {
		Set<Object> beanObjects = beans.get(beanName);
		if (beanObjects.size() == 0) {
			throw new NoSuitableBeanFoundException("No suitable bean was found for " + beanName + ".");
		}
		if (beanObjects.size() > 1) {
			throw new BeanConflictException("More than one bean was found for class " + beanName + ".");
		}
		return beanObjects.toArray()[0];
	}

}
