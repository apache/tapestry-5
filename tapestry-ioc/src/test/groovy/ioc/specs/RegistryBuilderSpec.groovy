package ioc.specs

import org.apache.tapestry5.ioc.*
import org.apache.tapestry5.ioc.def.ModuleDef
import org.apache.tapestry5.ioc.internal.DefaultModuleDefImpl
import org.apache.tapestry5.ioc.test.BarneyModule
import org.apache.tapestry5.ioc.test.FredModule
import org.apache.tapestry5.ioc.test.Greeter
import org.apache.tapestry5.ioc.test.MasterModule
import org.apache.tapestry5.ioc.test.NameListHolder
import org.apache.tapestry5.ioc.test.RegistryBuilderTestModule
import org.apache.tapestry5.ioc.test.ServiceBuilderModule
import org.apache.tapestry5.ioc.test.Square
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

class RegistryBuilderSpec extends Specification {

  def "@SubModule annotation is honored"() {
    when:

    Registry r = new RegistryBuilder().add(MasterModule).build()

    def service = r.getService("UnorderedNames", NameListHolder)

    then:

    service.names == ["Beta", "Gamma", "UnorderedNames"]

    cleanup:

    r.shutdown()
  }

  def "adding modules by name, in comma seperated list, as from a manifest"() {
    when:

    RegistryBuilder builder = new RegistryBuilder()

    IOCUtilities.addModulesInList builder,
        "${FredModule.class.name}, ${BarneyModule.class.name}, ${RegistryBuilderTestModule.class.name}"

    Registry registry = builder.build()

    Square service = registry.getService(Square)

    then:

    service.square(4) == 16

    service.toString() == "<Proxy for Square(${Square.class.name})>"

    cleanup:

    registry.shutdown()
  }

  def "exercise RegistryBuilder.buildAndStartupRegistry()"() {
    when:

    Registry r = RegistryBuilder.buildAndStartupRegistry(MasterModule);

    NameListHolder service = r.getService("UnorderedNames", NameListHolder);

    then:

    service.names == ["Beta", "Gamma", "UnorderedNames"]

    cleanup:

    r.shutdown();
  }

  def "use explicit ModuleDef with buildAndStartupRegistry()"() {
    when:

    Logger logger = LoggerFactory.getLogger(getClass());

    ModuleDef module = new DefaultModuleDefImpl(ServiceBuilderModule,
        logger, null);

    Registry r = RegistryBuilder.buildAndStartupRegistry(module, MasterModule);

    NameListHolder nameListHolder = r.getService("UnorderedNames", NameListHolder);

    then:

    nameListHolder.names == ["Beta", "Gamma", "UnorderedNames"]

    when:

    Greeter greeter = r.getService("Greeter", Greeter)

    then:

    greeter.greeting == "Greetings from service Greeter."

    cleanup:

    r.shutdown();

  }
}
