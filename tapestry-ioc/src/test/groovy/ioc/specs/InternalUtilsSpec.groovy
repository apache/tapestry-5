package ioc.specs

import java.lang.reflect.Method

import org.apache.tapestry5.commons.Locatable
import org.apache.tapestry5.commons.Location
import org.apache.tapestry5.commons.ObjectCreator
import org.apache.tapestry5.commons.ObjectLocator
import org.apache.tapestry5.commons.services.Coercion
import org.apache.tapestry5.func.F
import org.apache.tapestry5.func.Predicate
import org.apache.tapestry5.ioc.*
import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.ioc.def.ServiceDef
import org.apache.tapestry5.ioc.def.ServiceDef2
import org.apache.tapestry5.ioc.internal.QuietOperationTracker
import org.apache.tapestry5.ioc.internal.util.*
import org.apache.tapestry5.ioc.services.Builtin
import org.apache.tapestry5.ioc.services.SymbolSource
import org.apache.tapestry5.ioc.test.internal.util.FieldInjectionViaInject
import org.apache.tapestry5.ioc.test.internal.util.FieldInjectionViaInjectService
import org.apache.tapestry5.ioc.test.internal.util.FieldInjectionViaJavaxInject
import org.apache.tapestry5.ioc.test.internal.util.FieldInjectionViaJavaxNamed
import org.apache.tapestry5.ioc.test.internal.util.InjectoBean
import org.apache.tapestry5.ioc.test.internal.util.JavaxInjectBean
import org.apache.tapestry5.ioc.test.internal.util.NotRetainedRuntime
import org.apache.tapestry5.ioc.test.internal.util.TooManyAutobuildConstructorsBean

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class InternalUtilsSpec extends Specification {

  private static class PrivateInnerClass {

    public PrivateInnerClass() {
    }
  }

  static class PublicInnerClass {

    protected PublicInnerClass() {
    }
  }

  @Shared
  def tracker = new QuietOperationTracker();


  @Unroll
  def "asString(): #desc"() {

    when:

    Method m = clazz.getMethod(methodName, * paramTypes)

    then:

    InternalUtils.asString(m) == expected

    where:

    clazz       | methodName | paramTypes         | expected                                       | desc
    Object      | "toString" | []                 | "java.lang.Object.toString()"                  | "method with no arguments"
    Collections | "sort"     | [List, Comparator] | "java.util.Collections.sort(List, Comparator)" | "method with multiple argments"
    Object      | "wait"     | [long]             | "java.lang.Object.wait(long)"                  | "method with primitive argument"
    Arrays      | "sort"     | [int[]]            | "java.util.Arrays.sort(int[])"                 | "method with primitive array argument"
    Arrays      | "sort"     | [Object[]]         | "java.util.Arrays.sort(Object[])"              | "method with object array argument"
  }

  @Unroll
  def "size(): #desc"() {
    expect:

    InternalUtils.size(array as Object[]) == expected

    where:

    array     | expected | desc
    []        | 0        | "empty array"
    null      | 0        | "null is size 0"
    [1, 2, 3] | 3        | "non-empty array"
  }

  @Unroll
  def "stripMemberName('#input') should be '#expected'"() {
    expect:

    InternalUtils.stripMemberName(input) == expected

    where:

    input                         | expected
    "simple"                      | "simple"
    "_name"                       | "name"
    '$name'                       | "name"
    '__$ruby_style_'              | "ruby_style"
    '$_$__$__$_$____$_$_$_$$name' | "name"
    "foo_"                        | "foo"
    "_foo_"                       | "foo"
  }

  def "invalid input to stripMemberName() is an exception"() {
    when:

    InternalUtils.stripMemberName("!foo")

    then:

    IllegalArgumentException e = thrown()

    e.message == "Input '!foo' is not a valid Java identifier."
  }

  def "toList(Enumeration) is a sorted list"() {
    when:

    def e = Collections.enumeration(["wilma", "fred", "barney"])

    then:

    InternalUtils.toList(e) == ["barney", "fred", "wilma"]
  }

  @Unroll
  def "join(): #desc"() {
    expect:

    InternalUtils.join(list) == expected

    where:

    list                            | expected                       | desc
    ["barney"]                      | "barney"                       | "single value"
    ["fred", "barney", "wilma"]     | "fred, barney, wilma"          | "multiple values"
    ["fred", "barney", "", "wilma"] | "fred, barney, (blank), wilma" | "empty string converted to '(blank)'"
    ["fred", null ]                 | "fred, null"                   | "list with null values"
    [ null ]                        | "null"                         | "list with a single null value" //TAP5-2529
  }

  @Unroll
  def "joinSorted(): #desc"() {
    InternalUtils.joinSorted(list) == expected

    where:

    list                            | expected                       | desc
    null                            | "(none)"                       | "null list is '(none)'"
    []                              | "(none)"                       | "empty list is '(none)'"
    ["barney"]                      | "barney"                       | "single value"
    ["fred", "barney", "wilma"]     | "barney, fred, wilma"          | "multiple values"
    ["fred", "barney", "", "wilma"] | "(blank), barney, fred, wilma" | "empty string converted to '(blank)'"
  }

  @Unroll
  def "capitalize('#input') is '#expected'"() {
    expect:

    InternalUtils.capitalize(input) == expected

    where:

    input     | expected
    "hello"   | "Hello"
    "Goodbye" | "Goodbye"
    ""        | ""
    "a"       | "A"
    "A"       | "A"
  }

  def "locationOf(Object)"() {
    Locatable locatable = Mock()
    Location l = Mock()

    expect:

    InternalUtils.locationOf(null) == null
    InternalUtils.locationOf("La! La!") == null

    InternalUtils.locationOf(l).is(l)

    when:

    def actual = InternalUtils.locationOf(locatable)

    then:

    _ * locatable.location >> l

    actual.is(l)
  }

  @Unroll
  def "sortedKeys(): #desc"() {
    expect:

    InternalUtils.sortedKeys(map) == expected

    where:

    map                                        | expected           | desc
    null                                       | []                 | "null map"
    [:]                                        | []                 | "empty map"
    ["fred": "flintstone", "barney": "rubble"] | ["barney", "fred"] | "standard map"
  }

  @Unroll
  def "get(Map,Object): #desc"() {
    expect:

    InternalUtils.get(map, key) == expected

    where:

    map                    | key      | expected     | desc
    null                   | null     | null         | "null key and map"
    null                   | "foo"    | null         | "null map"
    ["fred": "flintstone"] | "fred"   | "flintstone" | "real map and key"
    ["fred": "flintstone"] | "barney" | null         | "real map with missing key"
  }

  def "reverseIterator(List)"() {
    when:

    def i = InternalUtils.reverseIterator(["a", "b", "c"])

    then:

    i.hasNext()
    i.next() == "c"

    i.hasNext()
    i.next() == "b"

    i.hasNext()
    i.next() == "a"

    !i.hasNext()
  }

  def "remove() not supported by reverse Iterator"() {
    def i = InternalUtils.reverseIterator(["a", "b", "c"])

    when:

    i.remove()

    then:

    thrown(UnsupportedOperationException)
  }

  @Unroll
  def "lastTerm(): #desc"() {
    expect:

    InternalUtils.lastTerm(input) == expected

    where:

    input             | expected | desc
    "simple"          | "simple" | "single term"
    "fee.fie.foe.fum" | "fum"    | "dotted name sequence"
  }

  def "simple value passed to lastTerm() returns the exact input value"() {
    def input = "simple"

    expect:

    InternalUtils.lastTerm(input).is(input)
  }

  def "addToMapList()"() {
    def map = [:]

    when:

    InternalUtils.addToMapList(map, "fred", 1)

    then:

    map == ["fred": [1]]

    when:

    InternalUtils.addToMapList(map, "fred", 2)

    then:

    map == ["fred": [1, 2]]
  }

  def "validateMarkerAnnotation()"() {

    when:

    InternalUtils.validateMarkerAnnotation(Inject)

    then:

    noExceptionThrown()

    when:

    InternalUtils.validateMarkerAnnotations([Inject, NotRetainedRuntime] as Class[])

    then:

    IllegalArgumentException e = thrown()

    e.message == "Marker annotation class org.apache.tapestry5.ioc.test.internal.util.NotRetainedRuntime is not valid because it is not visible at runtime. Add a @Retention(RetentionPolicy.RUNTIME) to the class."
  }

  def "close(Closable) for null does nothing"() {
    when:
    InternalUtils.close(null)

    then:
    noExceptionThrown()
  }

  def "close(Closable) for success case"() {
    Closeable c = Mock()

    when:

    InternalUtils.close(c)

    then:

    1 * c.close()
  }

  def "close(Closable) ignores exceptions"() {
    Closeable c = Mock()

    when:

    InternalUtils.close(c)

    then:

    1 * c.close() >> {
      throw new IOException("ignored")
    }
  }

  def "constructor with Tapestry @Inject annotation"() {
    when:

    def c = InternalUtils.findAutobuildConstructor(InjectoBean)

    then:

    c.parameterTypes == [String]
  }

  def "constructor with javax @Inject annotation"() {
    when:

    def c = InternalUtils.findAutobuildConstructor(JavaxInjectBean)

    then:

    c.parameterTypes == [String]
  }

  def "too many autobuild constructors"() {
    when:

    InternalUtils.findAutobuildConstructor(TooManyAutobuildConstructorsBean)

    then:

    IllegalArgumentException e = thrown()

    e.message == "Too many autobuild constructors found: use either @org.apache.tapestry5.ioc.annotations.Inject or @javax.inject.Inject annotation to mark a single constructor for autobuilding."
  }

  def "validateConstructorForAutobuild(): ensure check that the class itself is public"() {
    def cons = PrivateInnerClass.constructors[0]

    when:

    InternalUtils.validateConstructorForAutobuild(cons)

    then:

    IllegalArgumentException e = thrown()

    e.message == "Class ${PrivateInnerClass.name} is not a public class and may not be autobuilt."
  }

  def "validateConstructorForAutobuild(): ensure check that constructor is public"() {
    def cons = PublicInnerClass.declaredConstructors[0]

    when:

    InternalUtils.validateConstructorForAutobuild(cons)

    then:

    IllegalArgumentException e = thrown()

    e.message == "Constructor protected ${PublicInnerClass.name}() is not public and may not be used for autobuilding an instance of the class. " +
        "You should make the constructor public, or mark an alternate public constructor with the @Inject annotation."
  }

  def "@Inject service annotation on a field"() {
    ObjectLocator ol = Mock()
    def target = new FieldInjectionViaInjectService()
    Runnable fred = Mock()

    when:

    InternalUtils.injectIntoFields(target, ol, null, tracker)

    then:

    target.fred.is(fred)

    1 * ol.getService("FredService", Runnable) >> fred
  }

  def "@javax.annotations.Inject / @Named annotation on field"() {
    ObjectLocator ol = Mock()
    def target = new FieldInjectionViaJavaxNamed()
    Runnable fred = Mock()

    when:

    InternalUtils.injectIntoFields(target, ol, null, tracker)

    then:

    target.fred.is(fred)

    1 * ol.getService("BarneyService", Runnable) >> fred
  }

  def "@Inject annotation on field"() {
    ObjectLocator ol = Mock()
    def target = new FieldInjectionViaInject()
    SymbolSource source = Mock()
    InjectionResources resources = Mock()

    when:

    InternalUtils.injectIntoFields(target, ol, resources, tracker)

    then:

    target.symbolSource.is(source)

    1 * resources.findResource(SymbolSource, SymbolSource) >> null
    1 * ol.getObject(SymbolSource, _) >> { type, ap ->
      assert ap.getAnnotation(Builtin) != null

      return source
    }
  }

  def "@javax.annotation.Inject annotation on field"() {
    ObjectLocator ol = Mock()
    def target = new FieldInjectionViaJavaxInject()
    SymbolSource source = Mock()
    InjectionResources resources = Mock()

    when:

    InternalUtils.injectIntoFields(target, ol, resources, tracker)

    then:

    target.symbolSource.is(source)

    1 * resources.findResource(SymbolSource, SymbolSource) >> null
    1 * ol.getObject(SymbolSource, _) >> { type, ap ->
      assert ap.getAnnotation(Builtin) != null

      return source
    }
  }

  def "check handling of exception while injecting into a field"() {
    ObjectLocator ol = Mock()
    def target = new FieldInjectionViaInjectService()

    when:

    InternalUtils.injectIntoFields(target, ol, null, tracker)

    then:

    Exception e = thrown()

    1 * ol.getService("FredService", Runnable) >> "NotTheRightType"

    e.message.contains "Unable to set field 'fred' of <FieldInjectionViaInjectService> to NotTheRightType"
  }

  @Unroll
  def "keys(Map): #desc"() {
    expect:

    InternalUtils.keys(map) == (expected as Set)

    where:

    map                                        | expected           | desc
    null                                       | []                 | "null map"
    [:]                                        | []                 | "empty map"
    ["fred": "flintstone", "barney": "rubble"] | ["fred", "barney"] | "non-empty map"
  }

  @Unroll
  def "size(Collection): #desc"() {
    expect:

    InternalUtils.size(coll) == expected

    where:

    coll      | expected | desc
    null      | 0        | "null collection"
    []        | 0        | "empty collection"
    [1, 2, 3] | 3        | "non-empty collection"
  }

  def "toServiceDef2() delegates most methods to ServiceDef instance"() {
    ServiceDef delegate = Mock()
    ServiceBuilderResources resources = Mock()
    ObjectCreator creator = Mock()
    def serviceId = "fred"
    def markers = [] as Set

    ServiceDef2 sd = InternalUtils.toServiceDef2(delegate)

    when:

    def actual = sd.createServiceCreator(resources)

    then:

    actual.is creator

    1 * delegate.createServiceCreator(resources) >> creator


    when:

    actual = sd.getServiceId()

    then:
    actual.is serviceId

    1 * delegate.serviceId >> serviceId

    when:

    actual = sd.markers

    then:

    actual.is markers
    1 * delegate.markers >> markers


    when:

    actual = sd.serviceInterface

    then:

    actual == Runnable
    1 * delegate.serviceInterface >> Runnable

    when:

    actual = sd.serviceScope

    then:

    actual == ScopeConstants.PERTHREAD
    1 * delegate.serviceScope >> ScopeConstants.PERTHREAD

    when:

    actual = sd.eagerLoad

    then:

    actual == true
    1 * delegate.eagerLoad >> true

    expect:

    !sd.preventDecoration
  }

  def "matchAndSort()"() {
    def pred = { !it.startsWith(".") } as Predicate

    expect:

    InternalUtils.matchAndSort(["Fred", "Barney", "..", ".hidden", "Wilma"], pred) == ["Barney", "Fred", "Wilma"]
  }

  def "toMapper(Coercion)"() {
    def coercion = { it.toUpperCase() } as Coercion

    def flow = F.flow("Mary", "had", "a", "little", "lamb")

    expect:

    flow.map(InternalUtils.toMapper(coercion)).toList() == ["MARY", "HAD", "A", "LITTLE", "LAMB"]
  }
}

