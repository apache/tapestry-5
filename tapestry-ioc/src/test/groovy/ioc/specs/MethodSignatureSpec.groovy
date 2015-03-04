package ioc.specs

import org.apache.tapestry5.ioc.internal.services.MethodSignature
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Method
import java.sql.SQLException

class MethodSignatureSpec extends Specification {

  def MethodSignature find(Class sourceClass, String methodName) {
    Method match = sourceClass.methods.find { it.name == methodName }

    if (match == null) {
      throw new IllegalStateException("Call $sourceClass.name has no method named '$methodName'.")
    }

    return new MethodSignature(match)
  }

  @Unroll
  def "#firstClass.name and #secondClass.name have identical MethodSignatures for method #methodName"() {

    when:

    def m1 = find firstClass, methodName
    def m2 = find secondClass, methodName

    then:

    m1.hashCode() == m2.hashCode()
    m1 == m2

    where:

    firstClass  | secondClass       | methodName
    Object      | Boolean           | "notify"
    String      | StringBuilder     | "charAt"
    ObjectInput | ObjectInputStream | "close"
  }

  def "a null parameter or exception list is equivalent to an empty one"() {
    def m1 = new MethodSignature(void, "foo", null, null)
    def m2 = new MethodSignature(void, "foo", [] as Class[], [] as Class[])

    expect:

    m1 == m2
    m2 == m1

    m1.hashCode() == m2.hashCode()
  }

  def "a mismatch of method name causes inequality"() {
    def m1 = new MethodSignature(void, "foo", null, null)
    def m2 = new MethodSignature(void, "bar", null, null)

    expect:

    m1 != m2
  }

  def "a mismatch of parameters causes inequality"() {
    def m1 = new MethodSignature(void, "foo", [String] as Class[], null)
    def m2 = new MethodSignature(void, "foo", [Boolean] as Class[], null)

    expect:

    m1 != m2
  }

  def "a MethodSignature never equals null"() {

    expect:

    new MethodSignature(void, "foo", null, null) != null
  }

  def "a MethodSignature may only equal another MethodSignature"() {

    expect:

    new MethodSignature(void, "foo", null, null) != "Any Old Thing"
  }

  @Unroll
  def "MethodSignature.toString() for #clazz.name #methodName is '#toString'"() {

    def sig = find(clazz, methodName)

    expect:

    sig.toString() == toString

    where:

    clazz  | methodName    | toString
    String | "getChars"    | "void getChars(int, int, char[], int)"
    Class  | "newInstance" | "java.lang.Object newInstance() throws java.lang.IllegalAccessException, java.lang.InstantiationException"
  }

  @Unroll
  def "MethodSignature.uniqueId for #clazz.name #methodName is '#uniqueId'"() {
    def sig = find(clazz, methodName)

    expect:

    sig.uniqueId == uniqueId

    where:

    clazz  | methodName    | uniqueId
    String | "getChars"    | "getChars(int,int,char[],int)"
    Class  | "newInstance" | "newInstance()"
  }

  def "different return types will prevent override"() {

    def m1 = new MethodSignature(void, "foo", null, null)
    def m2 = new MethodSignature(int, "foo", null, null)

    expect:

    !m1.isOverridingSignatureOf(m2)
  }

  def "different method names will prevent override"() {
    def m1 = new MethodSignature(int, "foo", null, null)
    def m2 = new MethodSignature(int, "bar", null, null)

    expect:

    !m1.isOverridingSignatureOf(m2)
  }

  def "different parameter types will prevent override"() {
    def m1 = new MethodSignature(int, "foo", null, null)
    def m2 = new MethodSignature(int, "foo", [String] as Class[], null)

    expect:

    !m1.isOverridingSignatureOf(m2)
  }

  def "a difference of exceptions thrown allows for override"() {
    def m1 = new MethodSignature(int, "foo", null, [Exception] as Class[])
    def m2 = new MethodSignature(int, "foo", null, [RuntimeException] as Class[])

    expect:

    // All of m2's exceptions are assignable to at least one of m1's exceptions
    m1.isOverridingSignatureOf(m2)
    !m2.isOverridingSignatureOf(m1)
  }

  def "signature with no exceptions will not override"() {
    def m1 = new MethodSignature(int, "foo", null, null)
    def m2 = new MethodSignature(int, "foo", null, [RuntimeException] as Class[])

    expect:

    !m1.isOverridingSignatureOf(m2)
    m2.isOverridingSignatureOf(m1)
  }

  def "complex matching of signature exceptions when determining override"() {
    def m1 = new MethodSignature(void, "close", null,
        [SQLException, NumberFormatException] as Class[])
    def m2 = new MethodSignature(void.class, "close", null,
        [SQLException, IOException] as Class[])

    expect:

    // NumberFormatException and IOException don't fit in either direction
    !m1.isOverridingSignatureOf(m2)
    !m2.isOverridingSignatureOf(m1)
  }
}
