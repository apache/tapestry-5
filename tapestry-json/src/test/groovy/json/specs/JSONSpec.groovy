package json.specs

import org.apache.tapestry5.json.JSONArray
import org.apache.tapestry5.json.JSONLiteral
import org.apache.tapestry5.json.JSONObject
import org.apache.tapestry5.json.JSONString
import org.apache.tapestry5.json.JSON
import spock.lang.Specification
import spock.lang.Unroll

class JSONSpec extends Specification {

    def "invalid types throw JSONInvalidTypeException"() {
        when:

        JSON.testValidity([:])

        then:

        RuntimeException e = thrown()

        e.message == '''JSONArray values / JSONObject properties may be one of Boolean, Number, String, org.apache.tapestry5.json.JSONArray, org.apache.tapestry5.json.JSONLiteral, org.apache.tapestry5.json.JSONObject, org.apache.tapestry5.json.JSONObject$Null, org.apache.tapestry5.json.JSONString. Type java.util.LinkedHashMap is not allowed.'''
    }

}
