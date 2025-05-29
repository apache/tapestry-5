package org.apache.tapestry5.beanmodel

import org.apache.tapestry5.beanmodel.BeanModelSourceBuilder.CoercionTupleConfiguration
import org.apache.tapestry5.beanmodel.internal.services.PropertyAccessImpl
import org.apache.tapestry5.beanmodel.internal.services.PropertyConduitSourceImpl
import org.apache.tapestry5.beanmodel.internal.services.PropertyExpressionException
import org.apache.tapestry5.beanmodel.services.PlasticProxyFactoryImpl
import org.apache.tapestry5.beanmodel.services.PropertyConduitSource
import org.apache.tapestry5.commons.internal.BasicTypeCoercions
import org.apache.tapestry5.commons.internal.services.StringInternerImpl
import org.apache.tapestry5.commons.internal.services.TypeCoercerImpl
import org.apache.tapestry5.commons.services.PlasticProxyFactory
import org.apache.tapestry5.commons.util.IntegerRange
import org.slf4j.LoggerFactory

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll


class PropertyConduitSpec extends Specification {

  static class TestBean {
    String stringProperty
    Integer intProperty
    boolean booleanProperty
    Boolean wrapperBooleanProperty
    TestBean nestedBean
    List<String> stringList
    List<TestBean> beanList
    Map<String, String> stringMap
    Map<String, TestBean> beanMap
    private String privateString = "initialPrivate"

    String getStringMethod() {
      "methodResult"
    }

    String getStringMethodWithArg(String arg) {
      "methodResult:" + arg
    }

    int getIntMethodWithArgs(int a, int b) {
      a + b
    }

    void setStringPropertyFromMethod(String value) {
      this.stringProperty = "setViaMethod:" + value
    }

    boolean isFlag() {
      booleanProperty
    }

    void setFlag(boolean flag) {
      this.booleanProperty = flag
    }

    TestBean() {}
    TestBean(String stringProperty) {
      this.stringProperty = stringProperty
    }

    String getPrivateString() {
      privateString
    }
    void setPrivateString(String privateString) {
      this.privateString = privateString
    }
  }

  static class RootBean {
    TestBean testBean = new TestBean()
    String topLevelString = "top"
    Integer topLevelInt = 100
    boolean topLevelBoolean = true
    List<Integer> topLevelList = [10, 20, 30]
    Map<String, Integer> topLevelMap = [a: 1, b: 2]
    Object nullObject = null
    final String finalProperty = "finalValue"
    static String staticProperty = "staticValue"

    String getCalculated() {
      "calculatedValue"
    }

    void setWriteOnly(String value) {
    }
  }

  RootBean root

  @Shared
  PropertyConduitSource propertyConduitSource

  def setupSpec() {
    def plasticProxyFactory = new PlasticProxyFactoryImpl(getClass().getClassLoader(), LoggerFactory.getLogger(PlasticProxyFactory.class))

    CoercionTupleConfiguration configuration = new CoercionTupleConfiguration()
    BasicTypeCoercions.provideBasicTypeCoercions(configuration)
    BasicTypeCoercions.provideJSR310TypeCoercions(configuration)
    def typeCoercer = new TypeCoercerImpl(configuration.getTuples())

    propertyConduitSource = new PropertyConduitSourceImpl(new PropertyAccessImpl(), plasticProxyFactory, typeCoercer, new StringInternerImpl())
  }

  def setup() {
    root = new RootBean()
    root.testBean = new TestBean()
    root.testBean.stringProperty = "beanString"
    root.testBean.intProperty = 123
    root.testBean.booleanProperty = true
    root.testBean.wrapperBooleanProperty = Boolean.TRUE
    root.testBean.nestedBean = new TestBean("nestedBeanString")
    root.testBean.stringList = ["a", "b", "c"]
    root.testBean.beanList = [
      new TestBean("beanInList1"),
      new TestBean("beanInList2")
    ]
    root.testBean.stringMap = [key1: "value1", key2: "value2"]
    root.testBean.beanMap = [mapKey1: new TestBean("beanInMap1")]
    root.topLevelList = [10, 20, 30]
    root.topLevelMap = [mapA: 100, mapB: 200]
  }

  @Unroll
  def "property access for '#expression'"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, expression)

    when:
    def result = conduit.get(root)
    def conduitType = conduit.getPropertyType()

    then:
    result == expectedValue
    conduitType == expectedType

    where:
    expression                          | expectedValue        | expectedType
    "topLevelString"                    | "top"                | String.class
    "topLevelInt"                       | 100                  | Integer.class
    "topLevelBoolean"                   | true                 | boolean.class
    "testBean.stringProperty"           | "beanString"         | String.class
    "testBean.intProperty"              | 123                  | Integer.class
    "testBean.booleanProperty"          | true                 | boolean.class
    "testBean.wrapperBooleanProperty"   | Boolean.TRUE         | Boolean.class
    "testBean.nestedBean.stringProperty"| "nestedBeanString"   | String.class
    "testBean.flag"                     | true                 | boolean.class
    "testBean.privateString"            | "initialPrivate"     | String.class
  }

  @Unroll
  def "safe dereference ('?.') for '#expression'"() {

    given:
    if (makePathNull) {
      if (expression.startsWith("nullObject")) {
          assert root.nullObject == null
      } else if (expression == "testBean.nestedBean?.stringProperty") {
          assert root.testBean != null
          root.testBean.nestedBean = null
      } else if (expression == "testBean?.stringProperty") {
          root.testBean = null
      }
    }
    def conduit = propertyConduitSource.create(RootBean, expression)

    when:
    def result = conduit.get(root)
    def conduitType = conduit.getPropertyType()

    then:
    result == expectedValue
    conduitType == expectedTypeIfNotNull // Type is determined even if current value is null

    where:
    expression                             | makePathNull | expectedValue      | expectedTypeIfNotNull
    "testBean?.stringProperty"             | false        | "beanString"       | String.class
    "testBean.nestedBean?.stringProperty"  | false        | "nestedBeanString" | String.class
    "testBean.nestedBean?.stringProperty"  | true         | null               | String.class // nestedBean is null
    "testBean?.stringProperty"             | true         | null               | String.class // testBean itself is null
  }

  @Unroll
  def "safe dereference ('?.') failing for non-existent properties for '#expression'"() {

    given:
    if (makePathNull) {
      if (expression.startsWith("nullObject")) {
          assert root.nullObject == null
      } else if (expression == "testBean?.stringProperty") {
          root.testBean = null
      }
    }

    when:
    def conduit = propertyConduitSource.create(RootBean, expression)

    then:
    thrown(PropertyExpressionException)

    where:
    expression                             | makePathNull
    "nullObject?.someProperty"             | true
    "testBean?.nonExistentProperty"        | false
  }

  @Unroll
  def "method invocation for '#expression'"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, expression)

    when:
    def result = conduit.get(root)
    def conduitType = conduit.getPropertyType()

    then:
    result == expectedValue
    conduitType == expectedType

    where:
    expression                                                 | expectedValue             | expectedType
    "testBean.getStringMethod()"                               | "methodResult"            | String.class
    "testBean.getStringMethodWithArg('hello')"                 | "methodResult:hello"      | String.class
    "testBean.getStringMethodWithArg(testBean.stringProperty)" | "methodResult:beanString" | String.class
    "testBean.getIntMethodWithArgs(5, 3)"                      | 8                         | int.class
    "testBean.getIntMethodWithArgs(testBean.intProperty, 2)"   | 125                       | int.class
    "getCalculated()"                                          | "calculatedValue"         | String.class
  }

  def "keyword 'this' evaluates to the root object"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, "this")

    when:
    def result = conduit.get(root)
    def conduitType = conduit.getPropertyType()

    then:
    result === root
    conduitType == RootBean.class
  }

  @Unroll
  def "keyword evaluation for '#expression'"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, expression)

    when:
    def result = conduit.get(root)
    def conduitType = conduit.getPropertyType()

    then:
    result == expectedValue
    conduitType == expectedType

    where:
    expression | expectedValue | expectedType
    "null"     | null          | Void.class
    "true"     | true          | Boolean.class
    "false"    | false         | Boolean.class
  }

  @Unroll
  def "literal evaluation for '#expression'"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, expression)

    when:
    def result = conduit.get(root)
    def conduitType = conduit.getPropertyType()

    then:
    result == expectedValue
    // ANTLR is more open for numeric literals, but PropertyConduit might
    // normalize these, so checking is a little more involved.
    if (expectedType == double.class && result instanceof Float) {
      (result as Float).toDouble() == (expectedValue as Double)
    } else {
      result.getClass() == expectedValue.getClass()
    }
    conduitType == expectedType

    where:
    expression | expectedValue | expectedType
    "123"      | 123           | Long.class
    "-45"      | -45           | Long.class
    "1.23"     | 1.23d         | Double.class
    "-.5"      | -0.5d         | Double.class
    "'hello'"  | "hello"       | String.class
    "''"       | ""            | String.class
  }

  @Unroll
  def "not operator evaluation for '#expression'"() {

    given:
    setupAction(root)
    def conduit = propertyConduitSource.create(RootBean, expression)

    when:
    def result = conduit.get(root)
    def conduitType = conduit.getPropertyType()

    then:
    result == expectedValue
    conduitType == boolean.class

    where:
    expression         | setupAction                    | expectedValue
    "!true"            | { }                            | false
    "!false"           | { }                            | true
    "!topLevelBoolean" | { }                            | false
    "!topLevelBoolean" | { it.topLevelBoolean = false } | true
    "!nullObject"      | { }                            | true
    "!testBean"        | { }                            | false
  }

  @Unroll
  def "range operator for '#expression'"() {

    given:
    setupAction(root)
    def conduit = propertyConduitSource.create(RootBean, expression)

    when:
    def result = conduit.get(root)
    def conduitType = conduit.getPropertyType()

    then:
    result instanceof IntegerRange
    result.iterator().collect { it } == expectedList
    conduitType == IntegerRange.class

    where:
    expression                  | setupAction            | expectedList
    "1..3"                      | { }                    | [1, 2, 3]
    "topLevelInt .. 102"        | { }                    | [100, 101, 102]
    "topLevelInt .. 7"          | { it.topLevelInt = 5 } | [5, 6, 7]
    "2..topLevelInt"            | { it.topLevelInt = 4 } | [2, 3, 4]
    "120..testBean.intProperty" | { }                    | [120, 121, 122, 123]
    "-2..0"                     | { }                    | [-2, -1, 0]
  }

  def "list literal evaluation"() {

    given:
    // TAP5-2808: RANGEOP isn't working as a sub-expression, even though the grammar defines it;
    // NOT WORKING: "[1, 'two', true, testBean.intProperty, [1..2]]"
    def conduit = propertyConduitSource.create(RootBean, "[1, 'two', true, testBean.intProperty]")

    when:
    def result = conduit.get(root)
    def conduitType = conduit.getPropertyType()

    then:
    result instanceof List
    result.size() == 4
    result[0] == 1
    result[1] == "two"
    result[2] == true
    result[3] == 123 // testBean.intProperty
    conduitType == List.class
  }

  def "map literal evaluation"() {

    given:
    // TAP5-2808: RANGEOP isn't working as a sub-expression, even though the grammar defines it;
    // NOT WORKING: "{'a': 1, 'b': testBean.stringProperty, 'c': true, 123: 'numKey', d: [1..2] }"
    def conduit = propertyConduitSource.create(RootBean, "{'a': 1, 'b': testBean.stringProperty, 'c': true }")

    when:
    def result = conduit.get(root)
    def conduitType = conduit.getPropertyType()

    then:
    result instanceof Map
    result.size() == 3
    result.a == 1
    result.b == "beanString"
    result.c == true
  }

  def "empty list/map literal for '#expression'"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, expression)

    when:
    def result = conduit.get(root)
    def conduitType = conduit.getPropertyType()

    then:
    result == expectedResult
    expectedType.isInstance(result)
    conduitType == expectedType

    where:
    expression | expectedResult | expectedType
    '[]'       | []             | List
    '{}'       | [:]            | Map
  }

  @Unroll
  def "set property for '#expression'"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, expression)

    def originalValue = conduit.get(root)

    when:
    conduit.set(root, newValue)
    def updatedValue = conduit.get(root)

    then:
    originalValue != newValue
    updatedValue == newValue

    where:
    expression                          | newValue
    "topLevelString"                    | "newTop"
    "topLevelInt"                       | 999
    "topLevelBoolean"                   | false
    "testBean.stringProperty"           | "newBeanString"
    "testBean.intProperty"              | 789
    "testBean.booleanProperty"          | false
    "testBean.wrapperBooleanProperty"   | Boolean.FALSE
    "testBean.nestedBean.stringProperty"| "newNestedString"
    "testBean.flag"                     | false
    "testBean.privateString"            | "newPrivate"
  }

  def "method used as property setter (stringPropertyFromMethod)"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, "testBean.stringPropertyFromMethod")

    when:
    conduit.set(root, "directValue")
    then:
    root.testBean.stringProperty == "setViaMethod:directValue"
  }

  def "final property should be read-only"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, "finalProperty")

    when:
    conduit.set(root, "newValue")

    then:
    thrown(RuntimeException)
    root.finalProperty == "finalValue"
  }

  def "write-only property (no getter)"() {

    given:
    def conduit = propertyConduitSource.create(RootBean.class, "writeOnly")

    expect: "property type for write-only property is bassed on setter"
    conduit.getPropertyType() == String.class

    when: "getting a write-only property"
    conduit.get(root)

    then: "should throw exception as there's no getter"
    thrown(RuntimeException)

    when: "setting a write-only property"
    conduit.set(root, "testSet")

    then: "should succeed"
    noExceptionThrown()
  }

  def "set property through safe dereference (last segment is settable)"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, "testBean?.stringProperty")

    when: "set on valid path"
    conduit.set(root, "safeSet")

    then:
    root.testBean.stringProperty == "safeSet"

    when: "path before ?. is null"
    root.testBean = null
    conduit.set(root, "ignoredSet")

    then:
    noExceptionThrown()
  }

  @Unroll
  def "literals are read-only for '#expression'"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, expression)

    when:
    conduit.set(root,  newValue)

    then:
    thrown(RuntimeException)

    where:
    expression | newValue
    "'aString'"| "new"
    "123"      | 456
    "1..3"     | [1, 2]
    "[1,2]"    | [3, 4]
    "{'a':1}"  | ['b':2]
    "true"     | false
    "null"     | "not null"
  }

  @Unroll
  def "non-existent property path '#expression' should fail"() {

    when:
    // Depending on when the failure occurs (conduit creation vs. get),
    // we test both.
    // Usually conduit creation fails for totally unknown property.
    // If it's a nested path where an intermediate part is valid, get() might fail.
    try {
      def conduit = propertyConduitSource.create(RootBean.class, expression)
      conduit.get(root)
    } catch (RuntimeException e) {
      assert e.getMessage().toLowerCase().contains("property") ||
      e.getMessage().toLowerCase().contains("could not find") ||
      e.getMessage().toLowerCase().contains("unable to parse") ||
      e.getMessage().toLowerCase().contains("no read property")
      throw e
    }

    then:
    thrown(RuntimeException)

    where:
    expression << [
      "nonExistentProperty",
      "testBean.nonExistent",
      "topLevelString.nonExistentPart"
    ]
  }

  def "method call with wrong number of arguments"() {

    when:
    propertyConduitSource.create(RootBean, "testBean.getIntMethodWithArgs(5)")

    then:
    thrown(PropertyExpressionException)
  }

  def "method call with wrong type of arguments (success due to TypeCoercer)"() {

    given:
    def conduit = propertyConduitSource.create(RootBean, "testBean.getStringMethodWithArg(testBean.intProperty)")

    when:
    def result = conduit.get(root)

    then:
    result == "methodResult:123"
  }

  @Unroll
  def "invalid expression syntax '#expression' (#description)"() {

    when:
    propertyConduitSource.create(RootBean, expression)

    then:
    thrown(RuntimeException)

    where:
    expression | description
    "a.b..c"   | "invalid '..' syntax"
    "foo."     | "trailing dot"
    ".foo"     | "leading dot not part of number"
    "a b c"    | "unparseable sequence"
    "foo("     | "unmatched parenthesis"
    "{'a':1,}" | "trailing comma in map"
    "[1,2,]"   |  "trailing comma in list"
  }
}
