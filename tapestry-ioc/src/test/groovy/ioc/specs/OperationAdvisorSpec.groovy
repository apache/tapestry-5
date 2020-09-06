package ioc.specs

import org.apache.tapestry5.beanmodel.services.PlasticProxyFactoryImpl
import org.apache.tapestry5.commons.services.PlasticProxyFactory
import org.apache.tapestry5.ioc.OperationTracker
import org.apache.tapestry5.ioc.internal.DefaultModuleDefImpl
import org.apache.tapestry5.ioc.internal.LoggerSourceImpl
import org.apache.tapestry5.ioc.internal.RegistryImpl
import org.apache.tapestry5.ioc.modules.TapestryIOCModule
import org.apache.tapestry5.ioc.services.OperationTrackedModule
import org.apache.tapestry5.ioc.services.OperationTrackedService

import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class OperationAdvisorSpec extends Specification {

  @Shared @AutoCleanup("shutdown")
  def registry

  @Shared
  def operations = []

  def setupSpec() {

    def classLoader = Thread.currentThread().contextClassLoader
    def loggerSource = new LoggerSourceImpl()

    def logger = loggerSource.getLogger(OperationAdvisorSpec)
    def proxyFactoryLogger = loggerSource.getLogger(PlasticProxyFactory)

    def plasticProxyFactory = new PlasticProxyFactoryImpl(classLoader, proxyFactoryLogger)

    def simpleOperationTracker = [

        run: { description, operation ->
          operations << description
          operation.run()
        },

        invoke: {description, operation ->
          operations << description
          operation.invoke()
        }
    ] as OperationTracker

    registry = new RegistryImpl([
        new DefaultModuleDefImpl(TapestryIOCModule, logger, plasticProxyFactory),
        new DefaultModuleDefImpl(OperationTrackedModule, logger, plasticProxyFactory)],
        plasticProxyFactory,
        loggerSource,
        simpleOperationTracker)
  }

  def "simple operation tracking"() {
    def service = registry.getService OperationTrackedService

    service.nonOperation()

    when:

    operations.clear()

    service.first()

    then:

    operations == ["First operation"]
  }

  def "complex operation tracking"() {
    def service = registry.getService OperationTrackedService

    service.nonOperation()

    operations.clear()

    when:

    service.second "foo"
    service.second "bar"

    then:

    operations == ["Second operation: foo", "Second operation: bar"]
  }
}
