package org.apache.tapestry5.plastic

import org.apache.tapestry5.plastic.PlasticManager;

import spock.lang.Specification;
import testannotations.Maybe;
import testannotations.Outer;
import testannotations.PrimitiveValues;
import testannotations.Truth;

class MethodAnnotationAccess extends Specification {
    def mgr = new PlasticManager()
    def pc = mgr.getPlasticClass("testsubjects.AnnotationSubject")
    
    def "access to method annotation with enum attribute"() {
        
        when:
        
        def methods = pc.getMethodsWithAnnotation(Maybe.class)
        def noMethod = methods[0]
        def yesMethod = methods[1]
        
        then:
        
        methods.size == 2
        
        noMethod.description.methodName == "no"
        yesMethod.description.methodName == "yes"
        
        noMethod.getAnnotation(Maybe.class).value() == Truth.NO
        yesMethod.getAnnotation(Maybe.class).value() == Truth.YES
    }
    
    def "method annotation with primitive attributes"() {
        when:
        def methods = pc.getMethodsWithAnnotation(PrimitiveValues.class)
        
        then:
        methods.size == 1
        
        when:
        def pv = methods[0].getAnnotation(PrimitiveValues.class)
        
        then:
        pv.count()  == 5
        pv.title() == "runnables"  // explicit
        pv.type() == Runnable.class
        pv.message() == "created" // default
    }
    
    def "nested annotation as attribute of outer annotation"() {
        when:
        def ann = pc.getMethodsWithAnnotation(Outer.class)[0].getAnnotation(Outer.class)
        
        then:
        ann.maybe().value() == Truth.YES
    }
}
