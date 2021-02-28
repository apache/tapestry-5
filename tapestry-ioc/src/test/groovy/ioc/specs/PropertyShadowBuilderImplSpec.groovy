package ioc.specs

import org.apache.tapestry5.ioc.services.PropertyShadowBuilder
import spock.lang.Shared

interface AnotherCloseable {
  void close();    
}

interface FooService extends java.io.Closeable, AnotherCloseable {

  void foo();
}

class FooHolder {

  private FooService foo;

  private int count = 0;

  public FooService getFoo() {
    count++;

    return foo;
  }

  public int getCount() {
    return count;
  }

  public void setFoo(FooService foo) {
    this.foo = foo;
  }

  @Override
  public String toString() {
    return "[FooHolder]";
  }

  public void setWriteOnly(FooService foo) {

  }
}

class PropertyShadowBuilderImplSpec extends AbstractSharedRegistrySpecification {

  @Shared
  PropertyShadowBuilder builder

  FooService foo = Mock()
  FooHolder holder = new FooHolder();

  def setupSpec() {
    builder = getService PropertyShadowBuilder
  }


  def "basic delegation from proxy to property"() {

    FooService shadow = builder.build(holder, "foo", FooService)

    holder.foo = foo


    when:

    shadow.foo()

    then:

    foo.foo()
    holder.count == 1

    shadow.toString() == "<Shadow: property foo of [FooHolder]>"

    when:

    shadow.foo()

    then:

    foo.foo()
    holder.count == 2
  }

  def "verify exception when accessing the value when null"() {

    FooService shadow = builder.build(holder, "foo", FooService)

    when:

    shadow.foo()

    then:

    NullPointerException e = thrown()

    e.message == "Unable to delegate method invocation to property 'foo' of [FooHolder], because the property is null."
  }

  def "property type mismatch"() {
    when:

    builder.build(holder, "count", Map)

    then:

    RuntimeException e = thrown()

    e.message == "Property 'count' of class ${FooHolder.name} is of type int, which is not assignable to type java.util.Map."
  }

  def "attempting to build for a write-only property is an exception"() {
    when:

    builder.build(holder, "writeOnly", FooService)

    then:

    RuntimeException e = thrown()

    e.message == "Class ${FooHolder.name} does not provide an accessor ('getter') method for property 'writeOnly'."
  }

}
