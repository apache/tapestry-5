package ioc.specs

import org.apache.tapestry5.commons.internal.util.MessageFormatterImpl

import spock.lang.Specification
import spock.lang.Unroll

class MessageFormatterImplSpec extends Specification {

    @Unroll
    def "standard formatting: #desc"() {

        def mf = new MessageFormatterImpl(format, null)

        expect:

        mf.format(* args) == expected

        where:

        format                    | args                                            | expected                                         | desc

        "Tapestry is %s."         | ["cool"]                                        | "Tapestry is cool."                              | "simple substition"
        "Tapestry release #%d."   | [5]                                             | "Tapestry release #5."                           | "numeric conversion"
        "%s is %s at version %d." | ["Tapestry", "cool", 5]                         | "Tapestry is cool at version 5."                 | "multiple conversions"
        "%s failed: %s"           | ["Something", new RuntimeException("bad wolf")] | "Something failed: bad wolf"                     | "expansion of exception message"
        "%s failed: %s"           | ["Another", new NullPointerException()]         | "Another failed: java.lang.NullPointerException" | "expansion of exception without message is exception class name"
    }

    def "toString() of a MessageFormatter is the format"() {

        when:
        def mf = new MessageFormatterImpl("this is the %s", null)

        then:

        mf.toString() == "this is the %s"
    }

}
