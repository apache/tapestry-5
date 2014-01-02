package ioc.specs

import org.apache.tapestry5.beaneditor.ReorderProperties
import org.apache.tapestry5.ioc.annotations.Advise
import org.apache.tapestry5.ioc.internal.AdviceModule
import org.apache.tapestry5.ioc.internal.AnnotatedServiceInterface
import org.apache.tapestry5.ioc.internal.DecoratorModule
import org.apache.tapestry5.ioc.internal.NonAnnotatedServiceInterface
import org.apache.tapestry5.ioc.internal.TestAdvice

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
    nonAnnotatedService.getClass().getAnnotation(ReorderProperties.class).value().equals("reorder") 
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

    def annotatedService = registry.getService AnnotatedServiceInterface
    def annotatedResult = annotatedService.execute(0);
    def annotatedMethod = annotatedService.getClass().getMethod("execute", int.class);
    
    then:
    nonAnnotatedMethod != null
    nonAnnotatedMethod.getAnnotation(Advise.class) != null
    nonAnnotatedMethod.getAnnotation(Advise.class).id().equals("id")
    nonAnnotatedMethod.getAnnotation(Advise.class).serviceInterface() == NonAnnotatedServiceInterface.class
    nonAnnotatedService.getClass().getAnnotation(ReorderProperties.class) != null
    nonAnnotatedService.getClass().getAnnotation(ReorderProperties.class).value().equals("reorder")
    nonAnnotatedResult == TestAdvice.ANNOTATION_FOUND
    
    annotatedMethod != null
    annotatedMethod.getAnnotation(Advise.class) != null
    annotatedMethod.getAnnotation(Advise.class).id().equals("id")
    annotatedMethod.getAnnotation(Advise.class).serviceInterface() == NonAnnotatedServiceInterface.class
    annotatedService.getClass().getAnnotation(ReorderProperties.class) != null
    annotatedService.getClass().getAnnotation(ReorderProperties.class).value().equals("reorder")
    annotatedResult == TestAdvice.ANNOTATION_FOUND

  }

}