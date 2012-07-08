package json.specs

import org.apache.tapestry5.internal.json.StringToJSONArray
import org.apache.tapestry5.internal.json.StringToJSONObject
import org.apache.tapestry5.json.JSONArray
import org.apache.tapestry5.json.JSONObject
import spock.lang.Specification

class CoercionsSpec extends Specification {

    def "string to JSONObject"() {
        def json = /{foo:"bar"}/
        def expected = new JSONObject(json)

        expect:

        new StringToJSONObject().coerce(json) == expected
    }

    void "string to JSONArray"() {

        def json = /[1, 2, 'three']/
        def expected = new JSONArray(json)

        expect:

        new StringToJSONArray().coerce(json) == expected
    }
}
