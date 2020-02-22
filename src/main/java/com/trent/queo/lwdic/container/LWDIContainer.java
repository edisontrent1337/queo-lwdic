package com.trent.queo.lwdic.container;

import com.trent.queo.lwdic.annotations.Bean;
import com.trent.queo.lwdic.annotations.Named;
import com.trent.queo.lwdic.container.exceptions.BeanAlreadyDefinedException;
import com.trent.queo.lwdic.container.exceptions.NoSuitableBeanFoundException;
import io.github.classgraph.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class LWDIContainer {

	private Map<String, Object> beans;


	public LWDIContainer() {
		this.beans = new HashMap<>();
	}

	/**
	 * Scans a given package and adds instances for all classes that are annotated with {@link Bean} to the container.
	 *
	 * @param packageName the package to be scanned.
	 */
	public void scanPackage(String packageName) {
		String beanAnnotation = Bean.class.getName();
		try (ScanResult scanResult =
					 new ClassGraph()
							 .verbose()
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
						addBean(beanName, instanceOfBean);
					}
				} else {
					addBean(beanClassName, instanceOfBean);
				}
				for (ClassInfo interfaceClassInfo : beanClassInfo.getInterfaces()) {
					String interfaceName = interfaceClassInfo.getName();
					addBean(interfaceName, instanceOfBean);
				}
				ClassInfo superclassInfo = beanClassInfo.getSuperclass();
				if (superclassInfo != null) {
					String superClassName = superclassInfo.getName();
					addBean(superClassName, instanceOfBean);
				}
			}
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
	 * Starts the container and initializes all beans.
	 */
	public void start() {

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
		beans.put(beanName, bean);
	}

	public Map<String, Object> getBeans() {
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
		Object result = beans.get(beanClassName);
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
		Object result = beans.get(beanName);
		if (!beanClass.isInstance(result)) {
			throw new NoSuitableBeanFoundException("No bean named " + beanName + " of type " + beanClass.getName() + "was found.");
		}
		return beanClass.cast(result);

	}

}
