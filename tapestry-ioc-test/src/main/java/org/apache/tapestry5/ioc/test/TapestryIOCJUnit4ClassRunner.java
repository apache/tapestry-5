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

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * <p>
 * A JUnit4ClassRunner to help with Tapestry IOC integration tests. The test runner requires a
 * registry configuration to be defined in a {@link Registry} annotation. A {@link RegistryShutdownType} can
 * be specified to configure the lifecycle of the test registry and it's services
 * </p>
 * 
 * <p>{@link ModuleDef}s can be added to the {@link org.apache.tapestry5.ioc.Registry} by annotating a factory method(s)
 * with {@link org.apache.tapestry5.ioc.test.ModuleDef}. These {@link ModuleDef} factory methods must be
 * <ul>
 *    <li>public</li>
 *    <li>static</li>
 *    <li>take zero arguments</li>
 *    <li>return a subclass of {@link ModuleDef}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Any services defined in the registry can be {@link Inject}ed into the test class to be used during testing.
 * </p>
 */
@SuppressWarnings("deprecation")
public class TapestryIOCJUnit4ClassRunner extends BlockJUnit4ClassRunner {
	private final TestRegistryManager registryManager;

	public TapestryIOCJUnit4ClassRunner(Class<?> type) throws InitializationError {
		super(type);
		this.registryManager = new TestRegistryManager(type);
	}

	@Override
	protected Object createTest() throws Exception {
		return registryManager.createTest();
	}

	@Override
	protected Statement withBeforeClasses(Statement statement) {
		final Statement next = super.withBeforeClasses(statement);
		return new Statement() {
			public void evaluate() throws Throwable {
				registryManager.beforeTestClass();
				next.evaluate();
			}
		};
	}

	@Override
	protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
		final Statement next = super.withBefores(method, target, statement);
		return new Statement() {
			public void evaluate() throws Throwable {
				registryManager.beforeTestMethod();
				next.evaluate();
			}
		};
	}
	
	@Override
	protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
		final Statement first = super.withAfters(method, target, statement);
		return new Statement() {
			public void evaluate() throws Throwable {
				first.evaluate();
				registryManager.afterTestMethod();
			}
		};
	}
	
	@Override
	protected Statement withAfterClasses(Statement statement) {
		final Statement first = super.withAfterClasses(statement);
		return new Statement() {
			public void evaluate() throws Throwable {
				first.evaluate();
				registryManager.afterTestClass();
			}
		};
	}
}