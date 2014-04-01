package org.apache.tapestry5.ioc.test;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

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