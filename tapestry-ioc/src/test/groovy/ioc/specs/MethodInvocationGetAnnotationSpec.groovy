package ioc.specs

import org.apache.tapestry5.beaneditor.ReorderProperties
import org.apache.tapestry5.ioc.annotations.Advise
import org.apache.tapestry5.ioc.annotations.IntermediateType;
import org.apache.tapestry5.ioc.test.internal.AdviceModule
import org.apache.tapestry5.ioc.test.internal.AnnotatedServiceInterface
import org.apache.tapestry5.ioc.test.internal.DecoratorModule
import org.apache.tapestry5.ioc.test.internal.NonAnnotatedGenericSetServiceInterface
import org.apache.tapestry5.ioc.test.internal.NonAnnotatedServiceInterface
import org.apache.tapestry5.ioc.test.internal.TestAdvice

/**
 * Tests whether MethodAdvice.getAnnotation() is actually returning annotations from
 * the service implementation methods and not from the service interface ones.
 * The tests also verify whether the proxy annotations have the service implementation annotations
 * and whether the service implementation annotations were copied the generated proxy class. 
 * @see TestAdvice
 */
class MethodInvocationGetAnnotationSpec extends AbstractRegistrySpecification {
    
  def "MethodAdvice.getAnnotation() and getMethod() in service decoration"() {
    when:

    buildRegistry DecoratorModule
    
	def nonAnnotatedService = registry.getService NonAnnotatedServiceInterface.class
	def nonAnnotatedResult = nonAnnotatedService.execute(0);
    def nonAnnotatedMethod = nonAnnotatedService.getClass().getMethod("execute", int.class);

    def annotatedService = registry.getService AnnotatedServiceInterface.class
    def annotatedResult = annotatedService.execute(0);
    def annotatedMethod = annotatedService.getClass().getMethod("execute", int.class);

    then:
    nonAnnotatedMethod != null
    nonAnnotatedMethod.getAnnotation(Advise.class) != null
    nonAnnotatedMethod.getAnnotation(Advise.class).id().equals("id")
    nonAnnotatedMethod.getAnnotation(Advise.class).serviceInterface() == NonAnnotatedServiceInterface.class
    nonAnnotatedService.getClass().getAnnotation(ReorderProperties.class) != null
    nonAnnotatedService.getClass().getAnnotation(ReorderProperties.class).value() == "reorder" 
	nonAnnotatedResult == TestAdvice.ANNOTATION_FOUND
    
    annotatedMethod != null
    annotatedMethod.getAnnotation(Advise.class) != null
    annotatedMethod.getAnnotation(Advise.class).id().equals("id")
    annotatedMethod.getAnnotation(Advise.class).serviceInterface() == NonAnnotatedServiceInterface.class
    annotatedService.getClass().getAnnotation(ReorderProperties.class) != null
    annotatedService.getClass().getAnnotation(ReorderProperties.class).value().equals("reorder")
    annotatedResult == TestAdvice.ANNOTATION_FOUND

  }
  
  def "MethodAdvice.getAnnotation() and getMethod() in service advice"() {
      
    when:
  
    buildRegistry AdviceModule
    
    def nonAnnotatedService = registry.getService NonAnnotatedServiceInterface.class
    def nonAnnotatedResult = nonAnnotatedService.execute(0);
    def nonAnnotatedMethod = nonAnnotatedService.getClass().getMethod("execute", int.class);
    def duplicatedAnnotationMethod = nonAnnotatedService.getClass().getMethod("duplicatedAnnotation", String.class);

    def annotatedService = registry.getService AnnotatedServiceInterface
    def annotatedResult = annotatedService.execute(0);
    def annotatedMethod = annotatedService.getClass().getMethod("execute", int.class);

    def nonAnnotatedGenSetService = registry.getService NonAnnotatedGenericSetServiceInterface.class
    
    def nonAnnotatedGenSetResult1 = nonAnnotatedGenSetService.execute1(0)
    def nonAnnotatedGenSetMethod1 = nonAnnotatedGenSetService.getClass().getMethod("execute1", int.class)
    
    def nonAnnotatedGenSetResult2 = nonAnnotatedGenSetService.execute2("execute2")
    // We need to look for a method that accept Object instead of string (maybe because of generics...)
    def nonAnnotatedGenSetMethod2 = nonAnnotatedGenSetService.getClass().getMethod("execute2", Object.class)
    
    def nonAnnotatedGenSetResult3 = nonAnnotatedGenSetService.execute3(0)
    def nonAnnotatedGenSetMethod3 = nonAnnotatedGenSetService.getClass().getMethod("execute3", int.class)

    def nonAnnotatedGenSetResult4 = nonAnnotatedGenSetService.execute2("execute2")
    // We need to look for a method that accept Object instead of string (maybe because of generics...)
    def nonAnnotatedGenSetMethod4 = nonAnnotatedGenSetService.getClass().getMethod("execute2", Object.class, String.class)

    then:
    nonAnnotatedMethod != null
    nonAnnotatedMethod.getAnnotation(Advise.class) != null
    nonAnnotatedMethod.getAnnotation(Advise.class).id() == "id"
    nonAnnotatedMethod.getAnnotation(Advise.class).serviceInterface() == NonAnnotatedServiceInterface.class
    nonAnnotatedService.getClass().getAnnotation(ReorderProperties.class) != null
    nonAnnotatedService.getClass().getAnnotation(ReorderProperties.class).value().equals("reorder")
    nonAnnotatedResult == TestAdvice.ANNOTATION_FOUND
    
    duplicatedAnnotationMethod != null
    duplicatedAnnotationMethod.getAnnotation(Advise.class) != null
    duplicatedAnnotationMethod.getAnnotation(Advise.class).id() == "right"
    duplicatedAnnotationMethod.getParameterAnnotations()[0].length > 0
    ((IntermediateType) duplicatedAnnotationMethod.getParameterAnnotations()[0][0]).value() == String.class

    annotatedMethod != null
    annotatedMethod.getAnnotation(Advise.class) != null
    annotatedMethod.getAnnotation(Advise.class).id() == "id"
    annotatedMethod.getAnnotation(Advise.class).serviceInterface() == NonAnnotatedServiceInterface.class
    annotatedService.getClass().getAnnotation(ReorderProperties.class) != null
    annotatedService.getClass().getAnnotation(ReorderProperties.class).value() == "reorder"
    annotatedResult == TestAdvice.ANNOTATION_FOUND

    nonAnnotatedGenSetMethod1 != null
    nonAnnotatedGenSetMethod1.getAnnotation(Advise.class) != null
    nonAnnotatedGenSetMethod1.getAnnotation(Advise.class).id() == "id"
    nonAnnotatedGenSetMethod1.getAnnotation(Advise.class).serviceInterface() == NonAnnotatedServiceInterface.class
    nonAnnotatedGenSetResult1 == TestAdvice.ANNOTATION_FOUND

    nonAnnotatedGenSetMethod2 != null
    nonAnnotatedGenSetMethod2.getAnnotation(Advise.class) != null
    nonAnnotatedGenSetMethod2.getAnnotation(Advise.class).id() == "id"
    nonAnnotatedGenSetMethod2.getAnnotation(Advise.class).serviceInterface() == NonAnnotatedServiceInterface.class
    nonAnnotatedGenSetResult2 == TestAdvice.ANNOTATION_FOUND

    nonAnnotatedGenSetMethod3 != null
    nonAnnotatedGenSetMethod3.getAnnotation(Advise.class) != null
    nonAnnotatedGenSetMethod3.getAnnotation(Advise.class).id() == "id"
    nonAnnotatedGenSetMethod3.getAnnotation(Advise.class).serviceInterface() == NonAnnotatedServiceInterface.class
    nonAnnotatedGenSetResult3 == TestAdvice.ANNOTATION_FOUND

    nonAnnotatedGenSetMethod4 != null
    nonAnnotatedGenSetMethod4.getAnnotation(Advise.class) != null
    nonAnnotatedGenSetMethod4.getAnnotation(Advise.class).id() == "id"
    nonAnnotatedGenSetMethod4.getAnnotation(Advise.class).serviceInterface() == NonAnnotatedServiceInterface.class
    nonAnnotatedGenSetResult4 == TestAdvice.ANNOTATION_FOUND
  }

}