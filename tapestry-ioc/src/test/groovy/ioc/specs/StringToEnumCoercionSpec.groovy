package ioc.specs

import org.apache.tapestry5.commons.util.StringToEnumCoercion
import org.apache.tapestry5.ioc.util.Stooge

import spock.lang.Specification

class StringToEnumCoercionSpec extends Specification {

  def "searches are case-insensitive"() {
    def coercion = new StringToEnumCoercion(Stooge)

    expect:

    coercion.coerce("moe").is(Stooge.MOE)
    coercion.coerce("MOE").is(Stooge.MOE)
    coercion.coerce("CURLY_Joe").is(Stooge.CURLY_JOE)
  }

  def "blank input returns null"() {
    def coercion = new StringToEnumCoercion(Stooge)

    expect:

    coercion.coerce("") == null
    coercion.coerce("\t\n") == null
  }

  def "enum value can be found by an added alias"() {
    def coercion = new StringToEnumCoercion(Stooge)

    coercion.addAlias("shemp", Stooge.CURLY_JOE)

    expect:

    coercion.coerce("curly_joe").is(Stooge.CURLY_JOE)
    coercion.coerce("shemp").is(Stooge.CURLY_JOE)
    coercion.coerce("Shemp").is(Stooge.CURLY_JOE)
  }

  def "a failed search by name throws an exception"() {
    def coercion = new StringToEnumCoercion(Stooge)

    when:

    coercion.coerce("shemp")

    then:

    RuntimeException e = thrown()

    e.message == /Input 'shemp' does not identify a value from enumerated type ${Stooge.name}./
  }
}
