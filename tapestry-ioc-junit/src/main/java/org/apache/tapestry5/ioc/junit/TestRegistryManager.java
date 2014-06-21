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

package org.apache.tapestry5.ioc.junit;

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
	private final List<Method> moduleDefFactories;
	
	private org.apache.tapestry5.ioc.Registry registry;
	
	public TestRegistryManager(Class<?> type) throws InitializationError {
		super();
		
		Registry annotation = type.getAnnotation(Registry.class);
		if (annotation == null) {
			throw new InitializationError(type.getName() + " does not specify a @Registry");
		}
		
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
		if (!org.apache.tapestry5.ioc.def.ModuleDef.class.isAssignableFrom(method.getReturnType())) {
			throw new InitializationError(
					String.format("@ModuleDef method %s return type %s is not valid",
					method.getName(), method.getReturnType().getName()));
		}
	}

	/**
	 * Get the existing registry or create one if required.
	 * @return The test Registry
	 * @throws Exception
	 */
	public org.apache.tapestry5.ioc.Registry getOrCreateRegistry() throws Exception {
		if (registry == null) {
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
					if (e.getTargetException() instanceof Exception) {
						throw (Exception) e.getTargetException();
					}
					throw e;
				}
			}
			registry = builder.build();
			registry.performRegistryStartup();
		}
		return registry;
	}
	
	/**
	 * Notify that the current test method has completed
	 */
	public void afterTestMethod() {
		if (annotation.shutdown() == RegistryShutdownType.AFTER_METHOD) {
			shutdownRegistry();
		}
	}

	/**
	 * Notify that the current test class has completed
	 */
	public void afterTestClass() {
		if (annotation.shutdown() == RegistryShutdownType.AFTER_CLASS) {
			shutdownRegistry();
		}
	}
	
	protected void shutdownRegistry() {
		try {
			registry.shutdown();
		} finally {
			registry = null;
		}
	}
}
