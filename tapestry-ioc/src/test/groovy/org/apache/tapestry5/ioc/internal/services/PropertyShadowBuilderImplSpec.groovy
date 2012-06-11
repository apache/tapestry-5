package org.apache.tapestry5.ioc.internal.services

import org.apache.tapestry5.ioc.AbstractSharedRegistrySpecification
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder
import spock.lang.Shared

interface Foo {

  void foo();
}

class FooHolder {

  private Foo foo;

  private int count = 0;

  public Foo getFoo() {
    count++;

    return foo;
  }

  public int getCount() {
    return count;
  }

  public void setFoo(Foo foo) {
    this.foo = foo;
  }

  @Override
  public String toString() {
    return "[FooHolder]";
  }

  public void setWriteOnly(Foo foo) {

  }
}

class PropertyShadowBuilderImplSpec extends AbstractSharedRegistrySpecification {

  @Shared
  PropertyShadowBuilder builder

  Foo foo = Mock()
  FooHolder holder = new FooHolder();

  def setupSpec() {
    builder = getService PropertyShadowBuilder
  }


  def "basic delegation from proxy to property"() {

    Foo shadow = builder.build(holder, "foo", Foo)

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

    Foo shadow = builder.build(holder, "foo", Foo)

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

    builder.build(holder, "writeOnly", Foo)

    then:

    RuntimeException e = thrown()

    e.message == "Class ${FooHolder.name} does not provide an accessor ('getter') method for property 'writeOnly'."
  }

}
