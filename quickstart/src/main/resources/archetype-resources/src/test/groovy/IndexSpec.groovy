package ${package}

import spock.lang.Shared
import spock.lang.Specification

class CalculateSpec extends Specification {

    def "calculate sum"() {
        when:
        def total = 1 + 2

        then:
        3 == total
    }
}
