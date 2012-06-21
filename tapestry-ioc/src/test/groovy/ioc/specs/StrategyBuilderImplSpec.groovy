package ioc.specs

import org.apache.tapestry5.ioc.services.StrategyBuilder
import spock.lang.Shared


interface KindOf {

  String kindOf(Object value);
}

class KindOfImpl implements KindOf {

  private final String value;

  KindOfImpl(String value) { this.value = value; }

  @Override
  String kindOf(Object value) {
    return this.value;
  }

}

class StrategyBuilderImplSpec extends AbstractSharedRegistrySpecification {

  @Shared
  KindOf service


  def setup() {

    StrategyBuilder builder = getService StrategyBuilder

    service = builder.build KindOf, [
        (Map): new KindOfImpl("MAP"),
        (List): new KindOfImpl("LIST")
    ]
  }

  def "generated class implements a useful toString()"() {

    expect:

    service.toString() == "<Strategy for ioc.specs.KindOf>"
  }

  def "ensure implementation inputs map to interface definitions"() {

    expect:

    service.kindOf(Collections.EMPTY_MAP) == "MAP"
    service.kindOf(Collections.EMPTY_LIST) == "LIST"
  }

  def "mapping null with no void mapping is a failure"() {

    when:

    service.kindOf null

    then:

    RuntimeException e = thrown()

    e.message == "No adapter from type void to type ioc.specs.KindOf is available."
  }

}
