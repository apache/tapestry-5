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

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 *
 * A JUnit4ClassRunner to help with Tapestry IOC integration tests. The test
 * runner requires a registry configuration to be defined in a {@link Registry}
 * annotation. A {@link RegistryShutdownType} can be specified to configure the
 * lifecycle of the test registry and it's services
 *
 * 
 *
 * {@link org.apache.tapestry5.ioc.junit.ModuleDef}s can be added to the
 * {@link org.apache.tapestry5.ioc.Registry} by annotating a factory method(s)
 * with {@link ModuleDef}. These {@link ModuleDef} factory methods must be
 * <ul>
 * <li>public</li>
 * <li>static</li>
 * <li>take zero arguments</li>
 * <li>return a subclass of {@link org.apache.tapestry5.ioc.junit.ModuleDef}</li>
 * </ul>
 *
 * 
 *
 * Any services defined in the registry can be {@link Inject}ed into the test
 * class to be used during testing.
 *
 */
public class TapestryIOCJUnit4ClassRunner extends BlockJUnit4ClassRunner {
	private final TestRegistryManager registryManager;

	public TapestryIOCJUnit4ClassRunner(Class<?> type) throws InitializationError {
		super(type);
		this.registryManager = new TestRegistryManager(type);
	}

	@Override
	public void run(RunNotifier notifier) {
		RunNotifier wrapper = new RegistryManagerRunNotifier(registryManager, notifier);
		super.run(wrapper);
	}

	@Override
	protected Statement withAfterClasses(Statement statement) {
		final Statement superStatement = super.withAfterClasses(statement);
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				superStatement.evaluate();
				registryManager.afterTestClass();
			}
		};
	}

	@Override
	protected Object createTest() throws Exception {
		org.apache.tapestry5.ioc.Registry registry = registryManager.getOrCreateRegistry();
		return registry.autobuild(getTestClass().getJavaClass());
	}

	public static class RegistryManagerRunNotifier extends RunNotifier {
		private final RunNotifier delegate;
		private final TestRegistryManager registryManager;

		public RegistryManagerRunNotifier(TestRegistryManager registryManager, RunNotifier delegate) {
			super();
			this.delegate = delegate;
			this.registryManager = registryManager;
		}
		
		@Override
		public void addListener(RunListener listener) {
			delegate.addListener(listener);
		}

		@Override
		public void removeListener(RunListener listener) {
			delegate.removeListener(listener);
		}

		@Override
		public void fireTestRunStarted(Description description) {
			delegate.fireTestRunStarted(description);
		}

		@Override
		public void fireTestRunFinished(Result result) {
			delegate.fireTestRunFinished(result);
		}

		@Override
		public void fireTestStarted(Description description) throws StoppedByUserException {
			delegate.fireTestStarted(description);
		}

		@Override
		public void fireTestFailure(Failure failure) {
			delegate.fireTestFailure(failure);
		}

		@Override
		public void fireTestAssumptionFailed(Failure failure) {
			delegate.fireTestAssumptionFailed(failure);
		}

		@Override
		public void fireTestIgnored(Description description) {
			delegate.fireTestIgnored(description);
		}

		@Override
		public void fireTestFinished(Description description) {
			registryManager.afterTestMethod();
			delegate.fireTestFinished(description);
		}

		@Override
		public void pleaseStop() {
			delegate.pleaseStop();
		}

		@Override
		public void addFirstListener(RunListener listener) {
			delegate.addFirstListener(listener);
		}
	}
}