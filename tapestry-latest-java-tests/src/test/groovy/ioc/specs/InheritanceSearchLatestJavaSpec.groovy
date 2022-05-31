package ioc.specs

import org.apache.tapestry5.commons.internal.util.InheritanceSearch
import org.apache.tapestry5.plastic.PlasticUtils

import java.lang.constant.Constable
import java.lang.constant.ConstantDesc

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Complements InheritanceSearchSpec from tapestry-ioc.
 */
class InheritanceSearchLatestJavaSpec extends Specification {

  @Unroll
  def "inheritance of #className is #expectedNames (latest Java)"() {
	def search = new InheritanceSearch(clazz)
	def result = []
	while (search.hasNext()) {
	  result << search.next()
	}

	expect:

	result == expected

	where:

	clazz        | expected
	String       | [String, Serializable, Comparable, CharSequence, Constable, ConstantDesc, Object]
	long         | [long, Long, Number, Comparable, Constable, ConstantDesc, Serializable, Object]
	

	className = PlasticUtils.toTypeName(clazz)
	expectedNames = expected.collect { PlasticUtils.toTypeName(it) }.join(", ")

  }

}
