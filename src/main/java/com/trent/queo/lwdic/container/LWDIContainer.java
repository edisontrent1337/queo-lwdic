package com.trent.queo.lwdic.container;

import com.trent.queo.lwdic.annotations.Bean;
import com.trent.queo.lwdic.annotations.Named;
import com.trent.queo.lwdic.container.exceptions.BeanAlreadyDefinedException;
import io.github.classgraph.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class LWDIContainer {

	private Map<String, Object> beans;
	private Set<String> packages;


	public LWDIContainer() {
		this.beans = new HashMap<>();
		this.packages = new HashSet<>();
	}

	public void addPackage(String packageName) {

	}

	/**
	 * Scans a given package and adds instances for all classes that are annotated with {@link Bean} to the container.
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
				List<AnnotationParameterValue> beanParameterValues = namedAnnotationInfo.getParameterValues();
				String beanClassName = beanClassInfo.getName();
				if (beanParameterValues.size() == 1) {
					String beanName = (String) beanParameterValues.get(0).getValue();
					addBean(beanName, createInstanceForClass(beanClassName));
				} else {
					addBean(beanClassName, createInstanceForClass(beanClassName));
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
			throw new BeanAlreadyDefinedException();
		}
		beans.put(beanName, bean);
	}

	public Map<String, Object> getBeans() {
		return this.beans;
	}
}
