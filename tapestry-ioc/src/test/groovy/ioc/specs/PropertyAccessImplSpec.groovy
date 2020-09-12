package ioc.specs

import java.awt.Image
import java.beans.*
import java.lang.reflect.Method

import org.apache.tapestry5.beaneditor.DataType
import org.apache.tapestry5.beaneditor.Validate
import org.apache.tapestry5.beanmodel.internal.services.PropertyAccessImpl
import org.apache.tapestry5.commons.services.ClassPropertyAdapter
import org.apache.tapestry5.commons.services.PropertyAccess
import org.apache.tapestry5.ioc.annotations.Scope
import org.apache.tapestry5.ioc.test.internal.services.AnnotatedBean
import org.apache.tapestry5.ioc.test.internal.services.AnnotatedBeanSubclass
import org.apache.tapestry5.ioc.test.internal.services.Bean
import org.apache.tapestry5.ioc.test.internal.services.BeanWithIndexedProperty
import org.apache.tapestry5.ioc.test.internal.services.PublicFieldBean
import org.apache.tapestry5.ioc.test.internal.services.ShadowedPublicFieldBean
import org.apache.tapestry5.ioc.test.internal.services.SubInterface
import org.apache.tapestry5.ioc.test.internal.util.Pair
import org.apache.tapestry5.ioc.test.internal.util.StringLongPair

import com.example.TestInterface

import spock.lang.*

class ExceptionBean {

  boolean getFailure() {
    throw new RuntimeException("getFailure");
  }

  void setFailure(boolean b) {
    throw new RuntimeException("setFailure");
  }

  @Override
  String toString() {
    return "PropertyAccessImplSpecBean";
  }
}

class UglyBean {
}

class UglyBeanBeanInfo implements BeanInfo {

  BeanInfo[] getAdditionalBeanInfo() {
    return new BeanInfo[0];
  }

  BeanDescriptor getBeanDescriptor() {
    return null;
  }

  int getDefaultEventIndex() {
    return 0;
  }

  int getDefaultPropertyIndex() {
    return 0;
  }

  EventSetDescriptor[] getEventSetDescriptors() {
    return new EventSetDescriptor[0];
  }

  Image getIcon(int iconKind) {
    return null;
  }

  MethodDescriptor[] getMethodDescriptors() {
    return new MethodDescriptor[0];
  }

  PropertyDescriptor[] getPropertyDescriptors() {
    throw new RuntimeException("This is the UglyBean.");
  }

}

class ScalaBean {

  private String value;

  String getValue() {
    return value;
  }

  void setValue(String value) {
    this.value = value;
  }

  String value() {
    return value;
  }

  void value_$eq(String value) {
    this.value = value;
  }
}

class ScalaClass {

  private String value;

  String value() {
    return value;
  }

  void value_$eq(String value) {
    this.value = value;
  }
}

interface BeanInterface {

  String getValue();

  void setValue(String v);

  String getOtherValue();

  void setOtherValue(String v);

  int getIntValue(); // read-only
}

abstract class AbstractBean implements BeanInterface {
  // abstract class implements method from interface
  private String other;

  String getOtherValue() {
    return other;
  }

  void setOtherValue(String v) {
    other = v;
  }
}

class ConcreteBean extends AbstractBean {

  private String value;
  private int intValue;

  ConcreteBean(int intValue) {
    this.intValue = intValue;
  }

  String getValue() {
    return value;
  }

  void setValue(String v) {
    value = v;
  }

  int getIntValue() {
    return intValue;
  }
}

abstract class GenericBean<T> {

  public T value;
}

class GenericStringBean extends GenericBean<String> {
}

class PropertyAccessImplSpec extends Specification {

  @Shared
  PropertyAccess access = new PropertyAccessImpl()

  @Shared
  Random random = new Random()

  def "simple read access to a standard bean"() {
    Bean b = new Bean()
    int value = random.nextInt()

    when:

    b.value = value

    then:

    access.get(b, "value") == value
  }

  def "property name access is case insensitive"() {
    Bean b = new Bean()
    int value = random.nextInt()

    when:

    b.value = value

    then:

    access.get(b, "VaLUe") == value
  }

  def "simple write access to a standard bean"() {
    Bean b = new Bean()
    int value = random.nextInt()

    when:

    access.set(b, "value", value)

    then:

    b.value == value
  }

  def "missing properties are an exception"() {
    Bean b = new Bean()

    when:

    access.get(b, "zaphod")

    then:

    IllegalArgumentException e = thrown()

    e.message == "Class ${b.class.name} does not contain a property named 'zaphod'."
  }

  def "it is not possible to update a read-only property"() {
    Bean b = new Bean()

    when:

    access.set(b, "class", null)

    then:

    UnsupportedOperationException e = thrown()

    e.message == "Class ${b.class.name} does not provide a mutator ('setter') method for property 'class'."
  }

  def "it is not possible to read a write-only property"() {
    Bean b = new Bean()

    when:

    access.get(b, "writeOnly")

    then:

    UnsupportedOperationException e = thrown()

    e.message == "Class ${b.class.name} does not provide an accessor ('getter') method for property 'writeOnly'."
  }

  def "when a getter method throws an exception, the exception is wrapped and rethrown"() {

    ExceptionBean b = new ExceptionBean()

    when:

    access.get(b, "failure")

    then:

    RuntimeException e = thrown()

    e.message == "Error reading property 'failure' of ${b}: getFailure"
  }

  def "when a setter method throws an exception, the exception is wrapped and rethrown"() {
    ExceptionBean b = new ExceptionBean()

    when:

    access.set(b, "failure", false)

    then:

    RuntimeException e = thrown()

    e.message == "Error updating property 'failure' of ${b.class.name}: setFailure"
  }

  @Ignore
  def "exception throw when introspecting the class is wrapped and rethrown"() {

    // Due to Groovy, the exception gets thrown here, not inside
    // the access.get() method, thus @Ingore (for now)

    UglyBean b = new UglyBean()

    when:

    access.get(b, "google")

    then:

    RuntimeException e = thrown()

    e.message == "java.lang.RuntimeException: This is the UglyBean."
  }

  def "clearCache() wipes internal cache"() {
    when:

    ClassPropertyAdapter cpa1 = access.getAdapter Bean

    then:

    cpa1.is(access.getAdapter(Bean))


    when:

    access.clearCache()

    then:

    !cpa1.is(access.getAdapter(Bean))
  }

  def "ClassPropertyAdapter has a useful toString()"() {

    when:

    def cpa = access.getAdapter Bean

    then:

    cpa.toString() == "<ClassPropertyAdaptor ${Bean.class.name}: PI, class, readOnly, value, writeOnly>"
  }

  @Unroll
  def "expected properties for #beanClass.name property '#propertyName' are read=#read, update=#update, castRequired=#castRequired"() {

    when:

    def pa = getPropertyAdapter beanClass, propertyName

    then:

    pa.read == read
    pa.update == update
    pa.castRequired == castRequired
    pa.writeMethod == writeMethod
    pa.readMethod == readMethod

    where:

    beanClass | propertyName | read  | update | castRequired | writeMethodName | readMethodName
    Bean      | "readOnly"   | true  | false  | false        | null            | "getReadOnly"
    Bean      | "writeOnly"  | false | true   | false        | "setWriteOnly"  | null
    Bean      | "pi"         | true  | false  | false        | null            | null

    writeMethod = findMethod beanClass, writeMethodName
    readMethod = findMethod beanClass, readMethodName
  }

  def "PropertyAdapter for unknown property name is null"() {
    when:

    ClassPropertyAdapter cpa = access.getAdapter(Bean)

    then:

    cpa.getPropertyAdapter("google") == null
  }

  @Unroll
  def "PropertyAdapter.type for #beanClass.name property '#propertyName' is #type.name"() {

    ClassPropertyAdapter cpa = access.getAdapter(beanClass)

    when:

    def adapter = cpa.getPropertyAdapter(propertyName)

    then:

    adapter.type.is(type)

    where:

    beanClass | propertyName | type
    Bean      | "value"      | int
    Bean      | "readOnly"   | String
    Bean      | "writeOnly"  | boolean
  }

  def "ClassPropertyAdapter gives access to property names (in sorted order)"() {
    ClassPropertyAdapter cpa = access.getAdapter(Bean)

    expect:

    cpa.propertyNames == ["PI", "class", "readOnly", "value", "writeOnly"]
  }

  def "public static fields are treated as properties"() {
    when:

    def adapter = getPropertyAdapter Bean, "pi"

    then:

    adapter.get(null).is(Bean.PI)
  }

  def "public final static fields may not be updated"() {
    def adapter = getPropertyAdapter Bean, "pi"

    when:

    adapter.set(null, 3.0d)

    then:

    RuntimeException e = thrown()

    e.message.contains "final"
    e.message.contains "PI"
  }

  def "super interface methods are inherited by sub-interface"() {
    when:

    ClassPropertyAdapter cpa = access.getAdapter SubInterface

    then:

    cpa.propertyNames == ["grandParentProperty", "parentProperty", "subProperty"]
  }

  def "indexed properties are ignored"() {
    when:

    ClassPropertyAdapter cpa = access.getAdapter BeanWithIndexedProperty

    then:

    cpa.propertyNames == ["class", "primitiveProperty"]
  }

  def "getAnnotation from a bean property"() {
    AnnotatedBean b = new AnnotatedBean()

    when:

    def annotation = access.getAnnotation(b, "annotationOnRead", Scope)

    then:

    annotation.value() == "onread"
  }


  def "getAnnotation() when annotation is not present is null"() {

    when:

    def pa = getPropertyAdapter AnnotatedBean, "readWrite"

    then:

    pa.getAnnotation(Scope) == null
  }

  def "getAnnotation() with annotation on setter method"() {

    when:

    def pa = getPropertyAdapter AnnotatedBean, "annotationOnWrite"

    then:

    pa.getAnnotation(Scope).value() == "onwrite"
  }

  def "annotation on getter method overrides annotation on setter method"() {
    def pa = getPropertyAdapter AnnotatedBean, "annotationOnRead"

    when:

    Scope annotation = pa.getAnnotation(Scope)

    then:

    annotation.value() == "onread"
  }

  def "getAnnotation() works on read-only properties, skipping the missing setter method"() {

    when:

    def pa = getPropertyAdapter AnnotatedBean, "readOnly"

    then:

    pa.getAnnotation(Scope) == null
  }

  def "annotations directly on fields are located"() {
    when:

    def pa = access.getAdapter(Bean).getPropertyAdapter("value")

    then:

    pa.getAnnotation(DataType).value() == "fred"
  }

  @Issue("TAPESTY-2448")
  def "getAnnotation() will find annotations from an inherited field in a super-class"() {
    when:

    def pa = getPropertyAdapter AnnotatedBeanSubclass, "value"

    then:

    pa.getAnnotation(DataType).value() == "fred"
  }

  def "annotations on a getter or setter method override annotations on the field"() {
    when:

    def pa = getPropertyAdapter Bean, "value"

    then:

    pa.getAnnotation(Validate).value() == "getter-value-overrides"
  }

  def "PropertyAdapter.type understands (simple) generic signatures"() {
    def cpa1 = access.getAdapter(StringLongPair)

    when:

    def key = cpa1.getPropertyAdapter("key")

    then:

    key.type == String
    key.castRequired
    key.declaringClass == Pair

    when:

    def value = cpa1.getPropertyAdapter("value")

    then:

    value.type == Long
    value.castRequired

    when:

    def cpa2 = access.getAdapter(Pair)
    def pkey = cpa2.getPropertyAdapter("key")

    then:

    pkey.type == Object
    !pkey.castRequired

    when:

    def pvalue = cpa2.getPropertyAdapter("value")

    then:

    pvalue.type == Object
    !pvalue.castRequired
  }

  def "PropertyAdapter prefers JavaBeans property method names to Scala method names"() {
    when:

    def pa = getPropertyAdapter ScalaBean, "value"

    then:

    pa.readMethod.name == "getValue"
    pa.writeMethod.name == "setValue"
  }

  def "PropertyAdapter understands Scala accessor method naming"() {
    when:

    def pa = getPropertyAdapter ScalaClass, "value"

    then:

    pa.readMethod.name == "value"
    pa.writeMethod.name == 'value_$eq'
  }

  def "PropertyAccess exposes public fields as if they were properties"() {
    when:

    def pa = getPropertyAdapter PublicFieldBean, "value"

    then:

    pa.field
    pa.read
    pa.update

    when:

    PublicFieldBean bean = new PublicFieldBean()

    pa.set(bean, "fred")

    then:

    bean.value == "fred"

    when:

    bean.value = "barney"

    then:

    pa.get(bean) == "barney"
  }

  def "access to property is favored over public field when the names are the same"() {
    def bean = new ShadowedPublicFieldBean()

    when:

    def pa = getPropertyAdapter ShadowedPublicFieldBean, "value"

    then:

    !pa.field

    when:

    pa.set(bean, "fred")

    then:

    bean.@value == null

    when:

    bean.@value = "barney"
    bean.value = "wilma"

    then:

    pa.get(bean) == "wilma"
  }

  def "a property defined by an unimplemented inteface method of an abstract class is accessible"() {
    AbstractBean bean = new ConcreteBean(33)
    def ca = access.getAdapter(AbstractBean)

    when:

    def va = ca.getPropertyAdapter("value")

    then:

    !va.field

    when:

    va.set(bean, "hello")

    then:

    va.get(bean) == "hello"
    bean.value == "hello"

    when:

    def ova = ca.getPropertyAdapter("otherValue")

    then:

    !ova.field

    when:

    ova.set(bean, "other value")

    then:

    ova.get(bean) == "other value"
    bean.otherValue == "other value"

    when:

    def iva = ca.getPropertyAdapter("intvalue")

    then:

    iva.get(bean) == 33
    iva.read
    !iva.update
    !iva.field
  }

  def "generic field is recognized"() {
    when:
    def pa = getPropertyAdapter GenericStringBean, "value"

    then:

    pa.castRequired
    pa.type == String
    pa.declaringClass == GenericBean
  }
  
  interface GetterInterface {
    int getValue();
  }
        
  interface SetterGetterInterface extends GetterInterface {
    void setValue(int value);
  }
  
  interface SetterInterface {
    void setValue(int value);
  }
          
  interface GetterSetterInterface extends SetterInterface {
    int getValue();
  }
  
  final class GetterSetterClass implements GetterSetterInterface {
    public void setValue(int value) {}
    public int getValue() {}
  }
    
  // TAP5-1885
  def "split properties (getter in one supertype, setter in another)"() {
        
    when:
    def pa1 = getPropertyAdapter SetterGetterInterface, "value";
    def pa2 = getPropertyAdapter GetterSetterInterface, "value";
    def pa3 = getPropertyAdapter GetterSetterClass, "value";
      
    then:
    pa1.isRead();
    pa1.isUpdate();
    pa2.isRead();
    pa2.isUpdate();
    pa3.isRead();
    pa3.isUpdate();
  }
  
  public interface Baz { String getBar(); }
 
  public class AbstractFoo implements Baz {
    private String bar;
    public String getBar() { return bar; }
    public void setBar(String bar){ this.bar =bar; }
  }
  
  public class Foo extends AbstractFoo {}
  
  // TAP5-1548
  def "property expressions fails when using a supertype that implements an interface with a matching method"() {
        
    when:
    def pa = getPropertyAdapter AbstractFoo, "bar";
      
    then:
    pa.isRead();
    pa.isUpdate();
  }
  
  public static interface Entity<T extends Serializable>
  { 
    T getId(); 
  }
  
  public static interface NamedEntity extends Entity<Long> { 
    String getName(); 
  }

  // TAP5-1480
  def "exception when creating property conduits for generic interfaces"() {
    when:
    def paId = getPropertyAdapter Entity, "id";
    def paName = getPropertyAdapter NamedEntity, "name";
    
    then:
    paId != null
    paName != null
  }
  
  def getPropertyAdapter(clazz, name) {
    access.getAdapter(clazz).getPropertyAdapter(name)
  }

  private Method findMethod(Class beanClass, String methodName) {
    return beanClass.methods.find { it.name == methodName }
  }
  
 
  public static class TestData implements TestInterface {
  }
  
  // TAP5-2449
  def "default method is recognized"(){
    when:
    def pa = getPropertyAdapter(TestData, 'testString')
    then:
    pa != null
    
  }
  
  public interface IdentifiableEnum<E extends Enum<E>, ID extends Number> {
    ID getId();
  }
  
  public enum ById implements IdentifiableEnum<ById, Byte> {
    public Byte getId() {
      return null
    }
  }
  
  @Issue("TAP5-2032")
  def "create adapter for enum class with overridden methods"(){
    given:
    def adapter = access.getAdapter(ById)
    when:
    def propertyNames = adapter.propertyNames
    then:
    !propertyNames.empty
  }
  

}
