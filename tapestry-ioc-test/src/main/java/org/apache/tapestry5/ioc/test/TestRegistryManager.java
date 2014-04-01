// Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.ioc.RegistryBuilder;
import org.junit.runners.model.InitializationError;

/**
 * Helper class used by the {@link TapestryIOCJUnit4ClassRunner} to manage the test registry
 */
public class TestRegistryManager {
	private final Registry annotation;
	private final Class<?> type;
	private final List<Method> moduleDefFactories;
	
	private org.apache.tapestry5.ioc.Registry registry;
	
	public TestRegistryManager(Class<?> type) throws InitializationError {
		super();
		
		Registry annotation = type.getAnnotation(Registry.class);
		if (annotation == null) {
			throw new InitializationError(type.getName() + " does not specify a @Registry");
		}
		
		this.type = type;
		this.annotation = annotation;
		this.moduleDefFactories = findModuleDefFactories(type);
	}
	
	protected List<Method> findModuleDefFactories(Class<?> type) throws InitializationError {
		List<Method> factoryMethods = new ArrayList<Method>();
		for (Method method : type.getMethods()) {
			if (method.getAnnotation(ModuleDef.class) != null) {
				validateModuleDefMethod(method);
				factoryMethods.add(method);
			}
		}
		return factoryMethods.isEmpty() ? Collections.<Method> emptyList() : factoryMethods;
	}

	protected void validateModuleDefMethod(Method method) throws InitializationError {
		int modifiers = method.getModifiers();
		if (method.getParameterTypes().length != 0
				|| !Modifier.isStatic(modifiers)
				|| !Modifier.isPublic(modifiers)) {

			throw new InitializationError(
					String.format("@ModuleDef method %s must be public static and accept no arguments",
					method.getName()));
		}
		if (method.getReturnType().isAssignableFrom(org.apache.tapestry5.ioc.def.ModuleDef.class)) {
			throw new InitializationError(
					String.format("@ModuleDef method %s return type %s is not valid",
					method.getName(), method.getReturnType()));
		}
	}

	public void beforeTestClass() {
	}
	
	public void beforeTestMethod() {
	}
	
	public Object createTest() throws Exception {
		if (!isRegistryStarted()) {
			try {
				createRegistry();
			} catch (Throwable e) {
				throw new Exception(e);
			}
		}
		return registry.autobuild(type);
	}
	
	public void afterTestMethod() {
		if (annotation.shutdown() == RegistryShutdownType.AFTER_METHOD) {
			shutdownRegistry();
		}
	}

	public void afterTestClass() {
		if (annotation.shutdown() == RegistryShutdownType.AFTER_CLASS) {
			shutdownRegistry();
		}
	}
	
	protected void createRegistry() throws Throwable {
		RegistryBuilder builder = new RegistryBuilder();
		if (annotation.modules() != null) {
			builder.add(annotation.modules());
		}
		for (Method moduleDefFactory : moduleDefFactories) {
			try {
				org.apache.tapestry5.ioc.def.ModuleDef moduleDef =
						(org.apache.tapestry5.ioc.def.ModuleDef) moduleDefFactory.invoke(null);
				
				builder.add(moduleDef);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}
		registry = builder.build();
		registry.performRegistryStartup();
	}
	
	protected boolean isRegistryStarted() {
		return registry != null;
	}
	
	protected void shutdownRegistry() {
		try {
			registry.shutdown();
		} finally {
			registry = null;
		}
	}
}
