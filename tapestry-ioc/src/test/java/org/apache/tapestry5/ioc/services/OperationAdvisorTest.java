package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.internal.DefaultModuleDefImpl;
import org.apache.tapestry5.ioc.internal.LoggerSourceImpl;
import org.apache.tapestry5.ioc.internal.RegistryImpl;
import org.apache.tapestry5.ioc.internal.services.PlasticProxyFactoryImpl;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests for the {@link OperationAdvisor} service.
 *
 * @since 5.4
 */
public class OperationAdvisorTest extends IOCTestCase
{
    private List<String> operations = CollectionFactory.newList();

    private Registry registry;

    @BeforeClass
    public void setup()
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        LoggerSource loggerSource = new LoggerSourceImpl();

        Logger logger = loggerSource.getLogger(OperationAdvisorTest.class);
        Logger proxyFactoryLogger = loggerSource.getLogger(TapestryIOCModule.class.getName() + ".PlasticProxyFactory");

        PlasticProxyFactory plasticProxyFactory = new PlasticProxyFactoryImpl(classLoader, proxyFactoryLogger);

        List<ModuleDef> modules = CollectionFactory.newList();

        modules.add(new DefaultModuleDefImpl(TapestryIOCModule.class, logger, plasticProxyFactory));
        modules.add(new DefaultModuleDefImpl(OperationTrackedModule.class, logger, plasticProxyFactory));

        OperationTracker simpleOperationTracker = new OperationTracker()
        {
            @Override
            public void run(String description, Runnable operation)
            {
                operations.add(description);

                operation.run();
            }

            @Override
            public <T> T invoke(String description, Invokable<T> operation)
            {
                operations.add(description);

                return operation.invoke();
            }
        };

        registry = new RegistryImpl(modules, plasticProxyFactory, loggerSource, simpleOperationTracker);
    }

    @AfterClass
    public void cleanup()
    {
        registry.shutdown();

        registry = null;

        operations = null;
    }

    @Test
    public void simple_operation_tracking()
    {
        OperationTrackedService service = registry.getService(OperationTrackedService.class);

        service.nonOperation();

        operations.clear();

        service.first();

        assertListsEquals(operations, "First operation");
    }

    @Test
    public void complex_operation_tracking()
    {
        OperationTrackedService service = registry.getService(OperationTrackedService.class);

        service.nonOperation();

        operations.clear();

        service.second("foo");

        service.second("bar");

        assertListsEquals(operations, "Second operation: foo", "Second operation: bar");
    }


}
