package ioc.specs

import org.apache.tapestry5.ioc.services.ChainBuilder
import org.apache.tapestry5.ioc.internal.InterfaceWithStaticMethod

interface ChainCommand {

  void run();

  int workInt(int input);

  boolean workBoolean(boolean input);

  double workDouble(double input);

  String workString(String input);
}

class ChainBuilderImplSpec extends AbstractSharedRegistrySpecification {

  ChainCommand c1 = Mock()
  ChainCommand c2 = Mock()

  ChainCommand chain = getService(ChainBuilder).build(ChainCommand, [c1, c2])

  def "chaining of simple void method with no parameters"() {
    when:

    chain.run()

    then:

    1 * c1.run()

    then:

    1 * c2.run()
    0 * _
  }

  def "chaining of method with int parameter and return type"() {

    when:

    assert chain.workInt(7) == 99

    then:

    1 * c1.workInt(7) >> 0

    then:

    1 * c2.workInt(7) >> 99
    0 * _
  }

  def "verify that an int method that returns a non-zero value short-circuits the chain"() {
    when:

    assert chain.workInt(7) == 88

    then:

    1 * c1.workInt(7) >> 88
    0 * _
  }

  def "verify boolean parameters, return type, and short circuiting"() {

    when:

    assert chain.workBoolean(true) == true

    then:

    1 * c1.workBoolean(true) >> false

    then:

    1 * c2.workBoolean(true) >> true
    0 * _
  }

  def "verify string method parameter, return type, and short circuiting"() {
    when:

    assert chain.workString("fred") == "flintstone"

    then:

    1 * c1.workString("fred") >> null

    then:

    1 * c2.workString("fred") >> "flintstone"
    0 * _
  }

  def "verify double method parameter, return type, and short circuiting"() {

    when:

    assert chain.workDouble(1.2d) == 3.14d

    then:

    1 * c1.workDouble(1.2d) >> 0d

    then:

    1 * c2.workDouble(1.2d) >> 3.14d
    0 * _
  }

  def "chain instance has reasonable toString()"() {
    expect:

    chain.toString() == "<Command chain of ioc.specs.ChainCommand>"
  }
  
  final private static class InterfaceWithStaticMethodImpl extends InterfaceWithStaticMethod 
  {
    public int something() { return 2; }
  }

  /* Blows up without fix. */
  def "chain interface has static method"() {
    InterfaceWithStaticMethod c1 = Mock()
    InterfaceWithStaticMethod c2 = new InterfaceWithStaticMethodImpl()

    when:
      InterfaceWithStaticMethod chain = getService(ChainBuilder).build(InterfaceWithStaticMethod, [c1, c2])
    then: 
      chain.something() == 2
      chain.defaultSomething() == 1
  
  }

}
