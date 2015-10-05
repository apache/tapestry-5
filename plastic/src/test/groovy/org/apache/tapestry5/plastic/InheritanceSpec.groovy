package org.apache.tapestry5.plastic

import org.apache.tapestry5.internal.plastic.NoopDelegate
import org.spockframework.util.NotThreadSafe

import spock.lang.Ignore
import spock.lang.Issue;
import testannotations.SimpleAnnotation;

import java.lang.reflect.InvocationTargetException

import testsubjects.foo.BaseClass


class InheritanceSpec extends AbstractPlasticSpecification {

  def "default visible method from same package is inherited"() {
    setup:
    def mgr = PlasticManager.withContextClassLoader().packages(["testsubjects.foo",]).create()
    PlasticClass pcBase = mgr.getPlasticClass(BaseClass.name)
    when:
    PlasticClass pcExtending = mgr.pool.createTransformation(pcBase.className, BaseClass.name+"2").plasticClass
    then:
    pcExtending != null
    when:
    MethodDescription methodDescription = findMethod(pcBase, 'method').description

    def method2Extending = pcExtending.introduceMethod(methodDescription)

    then:
    method2Extending != null

    method2Extending.isOverride()
  }

  @Issue("TAP5-2508")
  def "default visible method from different package is not inherited"() {
    setup:
    def mgr = PlasticManager.withContextClassLoader().packages([
      "testsubjects.foo",
      "testsubjects.bar"
    ]).create()
    PlasticClass pcBase = mgr.getPlasticClass(BaseClass.name)
    when:
    PlasticClass pcExtending = mgr.pool.createTransformation(pcBase.className, "testsubjects.bar.ExtendingClass").plasticClass
    then:
    pcExtending != null
    when:
    MethodDescription methodDescription = findMethod(pcBase, 'method').description

    def method2Extending = pcExtending.introduceMethod(methodDescription)

    then:
    method2Extending != null

    !method2Extending.isOverride()
  }
}