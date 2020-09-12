package ioc.specs

import org.apache.tapestry5.ioc.test.NameListHolder
import org.apache.tapestry5.ioc.test.StaticModule

class ModuleInstantiationSpec extends AbstractRegistrySpecification {

  def setup() {
    StaticModule.reset()
  }

  def "module class is not instantiated when invoking static builder method"() {
    buildRegistry StaticModule

    def fred = getService "Fred", Runnable

    when:

    fred.run()

    then:

    !StaticModule.instantiated
    StaticModule.fredRan
  }

  def "module class is not instantiated when invoking static decorator method"() {
    buildRegistry StaticModule

    def barney = getService "Barney", Runnable

    when:

    barney.run()

    then:

    !StaticModule.instantiated
    StaticModule.decoratorRan
  }

  def "module class is not instantiated when invoking a static contributor method"() {
    buildRegistry StaticModule

    def holder = getService "Names", NameListHolder

    when:

    assert holder.names == ["Fred"]

    then:

    !StaticModule.instantiated
  }
}
