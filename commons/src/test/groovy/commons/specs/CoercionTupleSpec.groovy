package commons.specs

import java.time.LocalDate
import java.time.LocalTime

import org.apache.tapestry5.commons.services.Coercion
import org.apache.tapestry5.commons.services.CoercionTuple

import spock.lang.Specification

class CoercionTupleSpec extends Specification {

    def "corcion tuples key equality"() {

        when:

        def tuple1 = CoercionTuple.create(String.class, LocalDate.class, { input ->
            return LocalDate.parse(input)
        })

        def tuple2 = CoercionTuple.create(String.class, LocalDate.class, { input ->
            return null
        })

        def key1 = tuple1.getKey()
        def key2 = tuple2.getKey()

        then:

        tuple1.equals(tuple2) == false
        key1.equals(key1) == true
        key1.equals(new String()) == false
        key1.equals(key2)
    }
}
