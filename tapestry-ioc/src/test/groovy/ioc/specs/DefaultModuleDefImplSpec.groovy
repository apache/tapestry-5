package ioc.specs

import static org.apache.tapestry5.internal.plastic.asm.Opcodes.*

import org.apache.tapestry5.beanmodel.services.PlasticProxyFactoryImpl
import org.apache.tapestry5.commons.services.PlasticProxyFactory
import org.apache.tapestry5.internal.plastic.PlasticClassLoader
import org.apache.tapestry5.internal.plastic.PlasticInternalUtils
import org.apache.tapestry5.internal.plastic.asm.ClassWriter
import org.apache.tapestry5.ioc.*
import org.apache.tapestry5.ioc.def.ServiceDef3
import org.apache.tapestry5.ioc.internal.*
import org.apache.tapestry5.ioc.test.AutobuildModule
import org.apache.tapestry5.ioc.test.BlueMarker
import org.apache.tapestry5.ioc.test.MarkerModule
import org.apache.tapestry5.ioc.test.RedMarker
import org.apache.tapestry5.ioc.test.StringHolder
import org.apache.tapestry5.ioc.test.internal.AbstractAutobuildServiceModule
import org.apache.tapestry5.ioc.test.internal.ArrayDecoratorMethodModule
import org.apache.tapestry5.ioc.test.internal.BuilderMethodModule
import org.apache.tapestry5.ioc.test.internal.ComplexAutobuildModule
import org.apache.tapestry5.ioc.test.internal.DefaultServiceIdModule
import org.apache.tapestry5.ioc.test.internal.EagerLoadViaAnnotationModule
import org.apache.tapestry5.ioc.test.internal.ExceptionInBindMethod
import org.apache.tapestry5.ioc.test.internal.FieService
import org.apache.tapestry5.ioc.test.internal.MappedConfigurationModule
import org.apache.tapestry5.ioc.test.internal.ModuleWithOverriddenObjectMethods
import org.apache.tapestry5.ioc.test.internal.MutlipleAutobuildServiceConstructorsModule
import org.apache.tapestry5.ioc.test.internal.NamedServiceModule
import org.apache.tapestry5.ioc.test.internal.NoUsableContributionParameterModule
import org.apache.tapestry5.ioc.test.internal.NonStaticBindMethodModule
import org.apache.tapestry5.ioc.test.internal.NoopClassLoaderDelegate
import org.apache.tapestry5.ioc.test.internal.OrderedConfigurationModule
import org.apache.tapestry5.ioc.test.internal.PrimitiveDecoratorMethodModule
import org.apache.tapestry5.ioc.test.internal.ServiceIdConflictMethodModule
import org.apache.tapestry5.ioc.test.internal.ServiceIdViaAnnotationModule
import org.apache.tapestry5.ioc.test.internal.SimpleModule
import org.apache.tapestry5.ioc.test.internal.SyntheticMethodModule
import org.apache.tapestry5.ioc.test.internal.ToUpperCaseStringHolder
import org.apache.tapestry5.ioc.test.internal.TooManyContributionParametersModule
import org.apache.tapestry5.ioc.test.internal.UninstantiableAutobuildServiceModule
import org.apache.tapestry5.ioc.test.internal.VoidBuilderMethodModule
import org.slf4j.Logger

import spock.lang.Issue;
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DefaultModuleDefImplSpec extends Specification {

  @Shared
  PlasticProxyFactory proxyFactory = new PlasticProxyFactoryImpl(Thread.currentThread().contextClassLoader, null)

  @Shared
  OperationTracker tracker = new QuietOperationTracker()

  Logger logger = Mock()

  def "toString() of module lists services in the module"() {
    when:

    def md = module SimpleModule

    then:

    md.toString() == "ModuleDef[$SimpleModule.name Barney, Fred, Wilma]"
  }

  def "serviceIds contains all service ids"() {
    def md = module SimpleModule

    expect:

    md.serviceIds == ["Fred", "Barney", "Wilma"] as Set
  }

  def "ServiceDef obtainable by service id"() {
    def md = module SimpleModule

    when:

    def sd = md.getServiceDef "fred"

    then:

    sd.serviceId == "Fred"
    sd.serviceInterface == FieService
    sd.toString().contains "${SimpleModule.name}.buildFred()"
    sd.serviceScope == ScopeConstants.DEFAULT
    !sd.eagerLoad
    sd.markers.empty

    when:

    sd = md.getServiceDef("Wilma")

    then:

    sd.eagerLoad
  }

  def "ModuleDef exposes decorator methods as DecoratorDefs"() {
    def md = module SimpleModule

    when:

    def decos = md.decoratorDefs

    then:

    decos.size() == 1

    def deco = decos.find()

    deco.decoratorId == "Logging"
    deco.toString().contains "${SimpleModule.name}.decorateLogging(Class, Object)"
  }

  def "@ServiceId annotation on service builder method overrides naming convention"() {
    when:

    def md = module ServiceIdViaAnnotationModule

    then:

    md.getServiceDef("FooService") != null
  }

  def "@ServiceId on implementation class overrides default id from ServiceBinder.bind() default"() {
    when:

    def md = module ServiceIdViaAnnotationModule

    then:

    md.getServiceDef("BarneyService") != null
  }

  def "@Named annotation on service builder method overrides naming convention"() {
    when:

    def md = module NamedServiceModule

    then:

    md.getServiceDef("BazService") != null
  }

  def "@Named annotation on service implementation class overrides ServiceBinder.bind() default"() {
    when:

    def md = module NamedServiceModule

    then:

    md.getServiceDef("QuuxService") != null
  }

  def "naming convention for a service builder method named build() is derived from the return type"() {
    when:

    def md = module DefaultServiceIdModule

    then:

    md.getServiceDef("FieService") != null
  }

  def "conflicting service ids result in an exception"() {
    when:

    module ServiceIdConflictMethodModule

    then:

    RuntimeException ex = thrown()

    ex.message.contains "Service Fred (defined by ${ServiceIdConflictMethodModule.name}.buildFred()"
    ex.message.contains "conflicts with previously defined service defined by ${ServiceIdConflictMethodModule.name}.buildFred(Object)"
  }

  def "a service builder method may not return void"() {
    when:

    module VoidBuilderMethodModule

    then:

    RuntimeException ex = thrown()

    ex.message.contains "${VoidBuilderMethodModule.name}.buildNull()"
    ex.message.contains "but the return type (void) is not acceptable"
  }

  def "a service builder method may not return an array"() {
    when:

    module BuilderMethodModule

    then:

    RuntimeException ex = thrown()

    ex.message.contains "${BuilderMethodModule.name}.buildStringArray()"
    ex.message.contains "but the return type (java.lang.String[])"
  }

  @Unroll
  def "A decorator method #desc"() {
    when:

    module moduleClass

    then:

    RuntimeException e = thrown()

    e.message.contains expectedText

    where:

    moduleClass                    | expectedText        | desc
    PrimitiveDecoratorMethodModule | "decoratePrimitive" | "may not return a primitive type"
    ArrayDecoratorMethodModule     | "decorateArray"     | "may not return an array"
  }

  @Unroll
  def "#desc"() {
    when:

    def md = module moduleClass

    then:

    def defs = md.contributionDefs

    defs.size() == 1

    def cd = defs.find()

    cd.serviceId == serviceId

    cd.toString().contains "${moduleClass.name}.$methodSignature"

    where:

    moduleClass                | serviceId | methodSignature                           | desc
    SimpleModule               | "Barney"  | "contributeBarney(Configuration)"         | "contribution without annotation to configuration"
    OrderedConfigurationModule | "Ordered" | "contributeOrdered(OrderedConfiguration)" | "contribution to ordered configuration"
    MappedConfigurationModule  | "Mapped"  | "contributeMapped(MappedConfiguration)"   | "contribution to mapped configuration"
  }

  @Unroll
  def "service contribution method that #desc throws an exception"() {

    when:

    module moduleClass

    then:

    RuntimeException e = thrown()

    e.message.contains message

    where:

    moduleClass                         | message                                                                                                | desc

    NoUsableContributionParameterModule | "does not contain a parameter of type Configuration, OrderedConfiguration or MappedConfiguration"      | "does not include configuration parameter"
    TooManyContributionParametersModule | "contains more than one parameter of type Configuration, OrderedConfiguration, or MappedConfiguration" | "includes more than one configuration parameter"
  }

  def "using defaults for ServiceBinder.bind()"() {

    when:

    def md = module AutobuildModule
    ServiceDef3 sd = md.getServiceDef "stringholder"

    then:

    sd.serviceInterface == StringHolder
    sd.serviceId == "StringHolder"
    sd.serviceScope == ScopeConstants.DEFAULT
    !sd.isEagerLoad()
    sd.markers.empty
    !sd.preventDecoration
  }

  def "overriding defaults for ServiceBinder.bind()"() {

    when:

    def md = module ComplexAutobuildModule
    ServiceDef3 sd = md.getServiceDef "sh"

    then:

    sd.serviceInterface == StringHolder
    sd.serviceId == "SH"
    sd.serviceScope == "magic"
    sd.eagerLoad
    sd.preventDecoration
  }

  def "implementation class for ServiceBinder.bind() must have a public constructor"() {
    when:

    module UninstantiableAutobuildServiceModule

    then:

    RuntimeException e = thrown()

    e.message.contains "Class org.apache.tapestry5.ioc.test.internal.RunnableServiceImpl (implementation of service 'Runnable') does not contain any public constructors."
  }

  def "the bind() method of a module class must be a static method"() {
    when:

    module NonStaticBindMethodModule

    then:

    RuntimeException e = thrown()

    e.message.contains "Method org.apache.tapestry5.ioc.test.internal.NonStaticBindMethodModule.bind(ServiceBinder)"
    e.message.contains "appears to be a service binder method, but is an instance method, not a static method"
  }

  def "when autobuilding a service implementation, the constructor with the most parameters is chosen"() {
    ServiceBuilderResources resources = Mock()

    when:

    def md = module MutlipleAutobuildServiceConstructorsModule

    def sd = md.getServiceDef "stringholder"

    then:

    sd != null

    0 * _

    when:

    def oc = sd.createServiceCreator(resources)
    def holder = oc.createObject()

    holder.value = "foo"

    then:

    holder instanceof StringHolder
    holder.value == "FOO"

    _ * resources.serviceId >> "StringHolder"
    _ * resources.logger >> logger
    _ * resources.serviceInterface >> StringHolder
    1 * resources.getService("ToUpperCaseStringHolder", StringHolder) >> new ToUpperCaseStringHolder()
    _ * resources.tracker >> tracker

    1 * logger.debug(_) >> { args ->
      assert args[0].contains(
          "Invoking constructor org.apache.tapestry5.ioc.test.internal.MultipleConstructorsAutobuildService(StringHolder)")
    }

    0 * _
  }

  def "an exception inside a bind() method bubbles up"() {
    when:

    module ExceptionInBindMethod

    then:

    RuntimeException e = thrown()

    e.message.contains "Error invoking service binder method org.apache.tapestry5.ioc.test.internal.ExceptionInBindMethod.bind(ServiceBinder)"
    e.message.contains "at ExceptionInBindMethod.java"
    e.message.contains "Really, how often is this going to happen?"
  }

  def "@EagerLoad annotation on service implementation class is reflected in the ServiceDef"() {
    when:

    def md = module EagerLoadViaAnnotationModule
    def sd = md.getServiceDef "runnable"

    then:

    sd.eagerLoad
  }

  private DefaultModuleDefImpl module(moduleClass) {
    new DefaultModuleDefImpl(moduleClass, logger, proxyFactory)
  }

  def "marker annotations on the service builder method are available in the ServiceDef"() {

    when:

    def md = module MarkerModule
    def sd = md.getServiceDef "greeter"

    then:

    sd.markers == [BlueMarker] as Set
  }

  def "marker annotations specified via ServiceBinder is available in the ServiceDef"() {
    when:

    def md = module MarkerModule
    def sd = md.getServiceDef "redgreeter"

    then:

    sd.markers == [RedMarker] as Set
  }

  def "marker annotation on the implementation class is available in the ServiceDef"() {
    when:

    def md = module MarkerModule
    def sd = md.getServiceDef "SecondRedGreeter"

    then:

    sd.markers == [RedMarker] as Set
  }

  def "marker annotation from ServiceBinder and implementation class are merged"() {
    when:

    def md = module MarkerModule
    def sd = md.getServiceDef "SurprisinglyBlueGreeter"

    then:

    sd.markers == [RedMarker, BlueMarker] as Set
  }

  def "Multiple marker annotations can be added to service via ServiceBindingOptions"() {
	  when:

	  def md = module MarkerModule
	  def sd = md.getServiceDef "ColorfulGreeter"

	  then:

	  sd.markers == [RedMarker, BlueMarker] as Set
	}

  def "public synthetic methods on module class are ignored"() {
    def moduleClass = createSyntheticModuleClass()

    when:

    def md = module moduleClass

    then:

    md.serviceIds.size() == 1
  }

  def "Methods overridden from Object are ignored"() {

    when:

    def md = module ModuleWithOverriddenObjectMethods

    then:

    md.serviceIds.size() == 1
  }

  @Issue('https://issues.apache.org/jira/browse/TAP5-2425')
  def "a service implementation must not be abstract"() {

    when:

    module AbstractAutobuildServiceModule

    then:

    RuntimeException e = thrown()

    e.message.contains "Class org.apache.tapestry5.ioc.test.internal.AbstractRunnableService (implementation of service 'Runnable') is abstract."

  }


  private createSyntheticModuleClass() {

    def cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES)

    cw.visit(V1_5, ACC_PUBLIC, "EnhancedSyntheticMethodModule", null,
        PlasticInternalUtils.toInternalName(SyntheticMethodModule.name), null);

    def mv = cw.visitMethod ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC, "synth", "()V", null, null
    mv.visitCode()
    mv.visitInsn RETURN
    mv.visitEnd()

    cw.visitEnd()

    def bytecode = cw.toByteArray()

    ClassLoader loader = Thread.currentThread().contextClassLoader

    PlasticClassLoader plasticLoader = new PlasticClassLoader(loader, new NoopClassLoaderDelegate())

    return plasticLoader.defineClassWithBytecode("EnhancedSyntheticMethodModule", bytecode)
  }
}
