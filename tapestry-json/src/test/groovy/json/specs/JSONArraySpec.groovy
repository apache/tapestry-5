package json.specs

import org.apache.tapestry5.json.JSONArray
import spock.lang.Specification

class JSONArraySpec extends Specification {

    def "can create a JSONArray by parsing a JSON string"() {
        when:

        def array = new JSONArray(/[ 'foo', 'bar', "baz" ]/)

        then:

        array.length() == 3
        array.get(0) == "foo"
        array.get(1) == "bar"
        array.get(2) == "baz"
    }

    def "create a JSONArray from varags"() {
        when:

        def array = new JSONArray("fred", "barney", "wilma")

        then:

        array.length() == 3

        array.toCompactString() == /["fred","barney","wilma"]/
    }

    def "array must start with a bracket"() {
        when:

        new JSONArray("1, 2, 3]")

        then:

        RuntimeException e = thrown()

        e.message == "A JSONArray text must start with '[' at character 1 of 1, 2, 3]"
    }

    def "parse an empty array"() {
        when:

        def array = new JSONArray("[]")

        then:

        array.length() == 0
    }

    def "an empty element in the parse is a null"() {
        when:

        def array = new JSONArray("[1,,3]")

        then:

        array.length() == 3
        array.getInt(0) == 1
        array.isNull(1)
        array.getInt(2) == 3
    }

    def "a comma at the end of the list is ignored"() {
        when:

        def array = new JSONArray("[1,2,]")

        then:

        array.length() == 2
        array.get(0) == 1
        array.get(1) == 2
    }

    def "getBoolean() when not a boolean at index is an exception"() {
        def array = new JSONArray("[123]")

        when:

        array.getBoolean(0)

        then:

        RuntimeException e = thrown()

        e.message == "JSONArray[0] is not a Boolean."
    }

    def "handling of boolean values passed into constructor"() {
        when:

        def array = new JSONArray(true, false, "True", "False")

        then:

        array.getBoolean(0) == true
        array.getBoolean(1) == false

        array.getBoolean(2) == true
        array.getBoolean(3) == false
    }

    def "getDouble() when not a double at index is an exception"() {
        def array = new JSONArray(true)

        when:

        array.getDouble(0)

        then:

        RuntimeException e = thrown()

        e.message == "JSONArray[0] is not a number."
    }

    def "getDouble() works with numbers and parseable strings"() {
        def array = new JSONArray(400l, "95.5")

        expect:

        array.getDouble(0) == 400d
        array.getDouble(1) == 95.5d
    }

    def "getLong() works with numbers and parseable strings"() {
        def array = new JSONArray(400l, "95.5")

        expect:

        array.getLong(0) == 400l
        array.getLong(1) == 95l
    }

    def "getJSONArray() when not an array at index is exception"() {
        def array = new JSONArray("fred", "barney")

        when:

        array.getJSONArray(1)

        then:

        RuntimeException e = thrown()

        e.message == "JSONArray[1] is not a JSONArray."
    }

    def "get a nested array"() {
        def nested = new JSONArray()
        def outer = new JSONArray(nested)

        expect:

        outer.getJSONArray(0).is(nested)
    }

    def "getJSONObject() when not a JSONObject at index is an exception"() {
        def array = new JSONArray("fred", "barney", "wilma")

        when:

        array.getJSONObject(1)

        then:

        RuntimeException e = thrown()

        e.message == "JSONArray[1] is not a JSONObject."
    }

    def "may not put at a negative index"() {
        def array = new JSONArray()

        when:

        array.put(-1, "fred")

        then:

        RuntimeException e = thrown()

        e.message == "JSONArray[-1] not found."
    }

    def "put() overrides existing value in array"() {
        def array = new JSONArray("fred", "barney", "wilma")

        when:

        array.put 2, "betty"

        then:

        array.get(2) == "betty"
    }

    def "put() with index past end pads array with nulls"() {
        def array = new JSONArray("fred", "barney", "wilma")

        when:

        array.put 4, "bambam"

        then:

        array.isNull(3)

        array.toCompactString() == /["fred","barney","wilma",null,"bambam"]/
    }

    def "array equality"() {
        when:

        def array1 = new JSONArray(1, 2, 3)
        def array2 = new JSONArray(1, 2, 3)

        then:

        array1 == array1
        !array1.equals(null)
        array1 != this
        array1 == array2

        when:

        array2.put 9, "stuff"

        then:

        array1 != array2
    }

    def "pretty print"() {
        def array = new JSONArray("fred", "barney")

        expect:

        array.toString() == '''[
  "fred",
  "barney"
]'''
    }

    def "JSONArray is Iterable"() {
        def array = new JSONArray(1, 2, false)

        when:

        def i = array.iterator()

        then:

        i.next() == 1
        i.next() == 2
        i.next() == false

        !i.hasNext()
    }

    def "remove an element"() {
        def array = new JSONArray("one", "two", "three")

        when:

        array.remove(1)

        then:

        array.length() == 2
        array.toCompactString() == /["one","three"]/
    }

    def "putAll() adds new objects to existing array"() {
        def array = new JSONArray(100, 200)

        when:

        array.putAll([300, 400, 500])

        then:

        array.toCompactString() == /[100,200,300,400,500]/
    }

    def "putAll() with null does not modify the array"() {
        def array = new JSONArray(100, 200)

        when:

        array.putAll(null)

        then:

        array.toCompactString() == /[100,200]/
    }

    def "list returned by toList() is unmodifiable"() {
        def array = new JSONArray(100, 200)
        def list = array.toList()

        when:

        list.clear()

        then:

        thrown(UnsupportedOperationException)
    }

    def "list from toList() is live"() {
        def array = new JSONArray(100, 200)
        when:

        def list = array.toList()

        then:

        list == [100, 200]

        when:

        array.remove(0)

        then:

        list == [200]
    }

}
