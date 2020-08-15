package json.specs

import org.apache.tapestry5.json.JSONArray
import org.apache.tapestry5.json.JSONLiteral
import org.apache.tapestry5.json.JSONObject
import org.apache.tapestry5.json.JSONString
import org.apache.tapestry5.json.exceptions.JSONInvalidTypeException
import org.apache.tapestry5.json.exceptions.JSONSyntaxException
import org.apache.tapestry5.json.exceptions.JSONTypeMismatchException
import org.apache.tapestry5.json.exceptions.JSONValueNotFoundException

import spock.lang.Specification
import spock.lang.Unroll

class JSONObjectSpec extends Specification {

    def "copy an existing JSONObject"() {
        def master = new JSONObject("fred", "flintstone", "barney", "rubble")

        when:

        def emptyCopy = new JSONObject(master)

        then:

        emptyCopy.keys().empty

        when:

        def limitedCopy = new JSONObject(master, "fred")

        then:

        limitedCopy.keys().size() == 1
        limitedCopy.get("fred") == "flintstone"

        when:

        def fullCopy = new JSONObject(master, "fred", "barney")

        then:

        fullCopy.toCompactString() == /{"fred":"flintstone","barney":"rubble"}/
    }

    def "copy all properties of JSONObject"() {
        def master = new JSONObject("fred", "flintstone", "barney", "rubble")

        when:

        def fullCopy = master.copy()

        then:

        master == fullCopy

        // And they are independent:

        when:

        master.put("wilma", "flintstone")

        then:

        master != fullCopy
    }

    def "unknown keys when copying a JSONObject are ignored"() {
        def master = new JSONObject("fred", "flintstone", "barney", "rubble")

        when:

        def copy = new JSONObject(master, "fred", "wilma")

        then:

        copy.keys().size() == 1
        copy.get("fred") == "flintstone"
    }

    def "get with unknown key is an exception"() {
        def master = new JSONObject("fred", "flintstone")

        when:

        master.get "barney"

        then:

        JSONValueNotFoundException e = thrown()

        e.message == /JSONObject["barney"] is not found. Required: ANY/
    }

    def "getOrDefault returns defaultValue if not found"() {
        def master = new JSONObject("fred", "flintstone")

        when:

        def result = master.getOrDefault "barney", "gumble"

        then:

        result == "gumble"
    }

    def "getOrDefault returns value if found"() {
        def master = new JSONObject("fred", "flintstone")

        when:

        def result = master.getOrDefault "fred", "other"

        then:

        result == "flintstone"
    }

    @Unroll
    def "ensure that stored value #typeName #value is #expectedTypeName #expected"() {

        when:

        def object = new JSONObject().put("key", value)

        then:

        object."get${expectedTypeName.capitalize()}"("key") == expected

        where:

        value         | expected | expectedType
        "true"        | true     | boolean
        "TRUE"        | true     | boolean
        "false"       | false    | boolean
        "FALSE"       | false    | boolean
        Boolean.TRUE  | true     | boolean
        Boolean.FALSE | false    | boolean
        3.5d          | 3.5d     | double
        1000l         | 1000d    | double
        "-101.7"      | -101.7d  | double
        "3"           | 3        | int
        97l           | 97       | int
        "-8.76"       | -8       | int
        92d           | 92l      | long
        "-200"        | -200l    | long
        123           | "123"    | String

        typeName = value.class.simpleName
        expectedTypeName = expectedType.simpleName
    }

    @Unroll
    def "conversion of double #input to string should be '#expected'"() {
        expect:

        JSONObject.doubleToString(input) == expected

        where:

        input                    | expected
        3d                       | "3"
        -22.5d                   | "-22.5"
        0d                       | "0"
        Double.NaN               | "null"
        Double.NEGATIVE_INFINITY | "null"
        Double.POSITIVE_INFINITY | "null"
    }

    def "exception when getBoolean() on a value that is not a boolean"() {

        def object = new JSONObject().put("akey", 37)

        when:

        object.getBoolean "akey"

        then:

        JSONTypeMismatchException e = thrown()

        e.message == /JSONObject["akey"] is not a BOOLEAN. Actual: java.lang.Integer/
    }

    def "accumulate simple values"() {
        def object = new JSONObject()

        def key = "acckey"

        when:

        object.accumulate key, "alpha"

        then: "first value added is just stored"

        object.toCompactString() == /{"acckey":"alpha"}/

        when: "subsequent values added are accumulated in a JSONArray"

        object.accumulate key, "beta"
        object.accumulate key, "gamma"

        then:

        object.toCompactString() == /{"acckey":["alpha","beta","gamma"]}/

        object.getJSONArray(key).length() == 3
    }

    def "accumulate when the stored value is already an array just appends the value"() {
        def object = new JSONObject()

        when:

        object.accumulate "key", new JSONArray("fred", "barney")

        then:

        object.toCompactString() == /{"key":["fred","barney"]}/

        when:

        object.accumulate "key", "wilma"

        then:

        object.toCompactString() == /{"key":["fred","barney","wilma"]}/
    }

    def "can create a JSONObject from a JSON string"() {
        when:

        def object = new JSONObject(/ { fred: "flintstone", caveman: true, friends: ["barney"] }/)

        then:

        object.get("fred") == "flintstone"
        object.getBoolean("caveman") == true

        def friends = object.getJSONArray "friends"

        friends.length() == 1
        friends.get(0) == "barney"
    }

    def "append to an unknown key creates a JSONArray for the value"() {
        def object = new JSONObject()

        when:

        object.append "friends", "barney"

        then:

        object.toCompactString() == /{"friends":["barney"]}/
    }

    def "append to an existing key containing a JSONArray appends to the array"() {
        def object = new JSONObject(/{friends: ["barney"] }/)

        when:

        object.append "friends", "zaphod"

        then:

        object.toCompactString() == /{"friends":["barney","zaphod"]}/
    }

    def "appending to a key whose value is not aArray is an exception"() {
        def object = new JSONObject(/{friends: 0 }/)

        when:

        object.append "friends", "zaphod"

        then:

        JSONTypeMismatchException e = thrown()

        e.message == /JSONObject["friends"] is not a ARRAY. Actual: java.lang.Integer/
    }

    def "getDouble() with a non-numeric value is an exception"() {
        def object = new JSONObject(/{notdouble: true}/)

        when:

        object.getDouble "notdouble"

        then:

        JSONTypeMismatchException e = thrown()

        e.message == /JSONObject["notdouble"] is not a NUMBER. Actual: java.lang.Boolean/
    }

    def "getDouble() with a string that can not be parsed as a number is an exception"() {
        def object = new JSONObject(/{notdouble: "this is a fact"}/)

        when:

        object.getDouble "notdouble"

        then:

        JSONTypeMismatchException e = thrown()

        e.message == /JSONObject["notdouble"] is not a NUMBER. Actual: java.lang.String/
    }

    def "has() will identify which keys have values and which do not"() {
        def object = new JSONObject(/{fred: 'flintstone'}/)

        expect:

        object.has("fred")
        !object.has("barney")
    }

    def "getJSONArray() for a value that is not aArray is an exception"() {
        def object = new JSONObject(/{notarray: 22.7}/)

        when:

        object.getJSONArray "notarray"

        then:

        RuntimeException e = thrown()

        e.message == /JSONObject["notarray"] is not a ARRAY. Actual: java.lang.Double/
    }

    def "length() of a JSONObject is the number of keys"() {
        when:

        def object = new JSONObject()

        then:

        object.length() == 0

        when:

        object.put "fred", "flintstone"

        then:

        object.length() == 1

        when:

        object.accumulate "fred", "murray"

        then:

        object.length() == 1

        when:

        object.put "barney", "rubble"

        then:

        object.length() == 2
    }

    def "names() returns the keys as a JSONArray"() {
        def object = new JSONObject(/ {fred: 'flintstone', barney: 'rubble' }/)

        when:

        def names = object.names()

        then:

        names instanceof JSONArray
        names.length() == 2

        new ArrayList(names.toList()).sort() == ["barney", "fred"]
    }

    def "names() with no properties returns null"() {

        when:

        def object = new JSONObject()

        then:

        object.names() == null
    }

    def "the null literal is the NULL object"() {
        when:

        def object = new JSONObject(/{nullkey: null}/)

        then:

        object.get("nullkey") is JSONObject.NULL
        object.isNull("nullkey")
    }

    def "the NULL object is output as null literal"() {
        when:

        def object = new JSONObject().put("nullkey", JSONObject.NULL)

        then:

        object.isNull("nullkey")
        object.toCompactString() == /{"nullkey":null}/
    }

    def "the NULL object matches Java's null"() {
        expect:

        JSONObject.NULL.equals(null)
    }

    @Unroll
    def "#desc invalid input is an exception"() {
        when:

        new JSONObject(input)

        then:

        JSONSyntaxException e = thrown()

        e.message.trim().startsWith expected

        where:

        input                     | expected                                                                 | desc
        "{  "                     | "A JSONObject text must end with '}' at character 3"                     | "unmatched open brace"
        "fred"                    | /A JSONObject text must start with '{' (actual: 'f') at character 1 of/  | "missing open brace"
        /{ "akey" }/              | /Expected a ':' after a key at character 10 of/                          | "missing value after key"
        /{ "fred" : 1 "barney" }/ | /Expected a ',' or '}' at character 14 of/                               | "missing property separator"
        /{ "list" : [1, 2/        | /Expected a ',' or ']' at character 16 of/                               | "missing seperator or closing bracket"
        '''/* unclosed'''         | /Unclosed comment at character 11 of/                                    | "unclosed C-style comment"
        /{ "fred \n}/             | /Unterminated string at character 11 of/                                 | "unterminated string at line end"
        /{ fred: ,}/              | /Missing value at character 8 of /                                       | "missing value after key"
    }

    def "can use ':' or '=>' as key seperator, and ';' as property separator"() {
        def object = new JSONObject(/{ "fred" = 1; "barney" => 2}/)

        expect:

        object.getInt("fred") == 1
        object.getInt("barney") == 2
    }

    @Unroll
    def "conversion of #typeName #value to string is #expected"() {
        expect:

        JSONObject.numberToString(value) == expected

        where:

        value                         | expected
        new BigDecimal("100.0000000") | "100"

        typeName = value.class.simpleName
    }

    @Unroll
    def "quote(): #desc"() {
        expect:

        JSONObject.quote(value) == expected

        where:

        value                        | expected                                 | desc
        null                         | /""/                                     | "null is empty string"
        ""                           | /""/                                     | "empty string is empty string"
        '''"/\b\t\n\f\r\u2001/a</''' | '''"\\"/\\b\\t\\n\\f\\r\\u2001/a<\\/"''' | "special characters"
    }

    def "non-finite numbers are not allowed"() {
        def object = new JSONObject()

        when:

        object.put "nonfinite", value

        then:

        RuntimeException e = thrown()

        e.message == /JSON does not allow non-finite numbers./

        where:

        value << [
            Double.NaN,
            Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Float.NaN,
            Float.NEGATIVE_INFINITY,
            Float.POSITIVE_INFINITY
        ]
    }

    def "parse an empty object is empty"() {
        when:

        def object = new JSONObject(/{}/)

        then:

        object.length() == 0
    }

    def "put a Java null is a remove"() {
        def object = new JSONObject(/{fred:'flintstone', barney:'rubble'}/)

        when:

        object.put("fred", null)

        then:

        object.toCompactString() == /{"barney":"rubble"}/
    }

    def "only specific object types may be added - no exception"() {
        def object = new JSONObject(/{}/)

        when:

        object.put("key", value)

        then:

        noExceptionThrown()

        where:
        value << [
            null,
            true,
            3,
            3.5,
            "*VALUE*",
            new JSONLiteral("*LITERAL*"),
            new JSONObject(),
            new JSONArray()
        ]
    }

    def "only specific object types may be added - exception"() {
        def object = new JSONObject(/{}/)

        when:

        object.put("key", value)

        then:

        JSONInvalidTypeException e = thrown()

        where:
        value << [
            new java.util.Date(),
            [],
            [:]
        ]
    }

    def "JSONString can output anything it wants"() {
        def string = { "*VALUE*" } as JSONString

        def object = new JSONObject()

        when:

        object.put("key", string)

        then:

        object.toCompactString() == /{"key":*VALUE*}/
    }

    def "equals() implementation"() {
        def json = /{ "key" : 99 }/

        when:

        def obj1 = new JSONObject(json)
        def obj2 = new JSONObject(json)

        then:

        obj1 == obj2
        obj2 == obj1

        obj1.equals(null) == false
        obj1 != json

        when:

        obj2.put("screw", "the pooch")

        then:

        obj1 != obj2
    }

    def "escaped characters in the JSON are parsed correctly"() {
        when:

        def object = new JSONObject("{ \"key\" : \"\\\"/\\b\\t\\n\\f\\r\\u2001/a<\\/\\x20\" }")

        then:

        object.get("key") == '''"/\b\t\n\f\r\u2001/a</ '''
    }

    def "parse a nested object"() {
        def object = new JSONObject(/ { key: { name: "inner" }}/)

        expect:

        object.getJSONObject("key").getString("name") == "inner"
    }

    def "parse boolean literals"() {
        def object = new JSONObject(/{ t: true, f: false }/)

        expect:

        object.getBoolean("t") == true
        object.getBoolean("f") == false
    }

    def "parse numeric forms"() {
        def object = new JSONObject(/{ hex: 0x50, oct: 030, posInt: +50, negInt: -50,
    long: 4294968530, float: -32.7 }/)

        expect:

        object.getInt("hex") == 80
        object.getInt("oct") == 24
        object.getInt("posInt") == 50
        object.getInt("negInt") == -50
        object.getLong("long") == 4294968530l
        object.getDouble("float") == -32.7d
    }

    def "NULL toString() is null keyword"() {
        expect:

        JSONObject.NULL.toString() == "null"
    }

    def "JSONLiteral can output any content"() {

        def text = '''function(x) { $('bar').show(); }'''

        def literal = new JSONLiteral(text)

        when:

        def object = new JSONObject().put("callback", literal)

        then:

        object.toCompactString() == /{"callback":${text}}/
        literal.toString() == text
    }

    def "pretty print"() {
        def object = new JSONObject("fred", "flintstone", "barney", "rubble")

        expect:

        object.toString() == '''{
  "fred" : "flintstone",
  "barney" : "rubble"
}'''
    }

    def "C style comments are ignored"() {
        when:

        def object = new JSONObject('''/* C comment */ { "foo" : "bar" /* *ignored* */ }''')

        then:

        object.get("foo") == "bar"
    }

    def "double slash comments extend to end of line"() {
        when:

        def object = new JSONObject('''{ foo: // This is a double-slash comment
"bar" }''')

        then:

        object.get("foo") == "bar"
    }

    def "hash comments extend to end of line"() {
        when:

        def object = new JSONObject('''{ foo: # This is a hash comment
"bar" }''')

        then:

        object.get("foo") == "bar"
    }

    def "pretty-print an empty JSONObject"() {
        def object = new JSONObject()

        expect:

        object.toString() == /{}/
    }

    def "complex pretty-print"() {
        when:

        def object = new JSONObject('''
{ outer: { inner: "here" },
  number: 22.7,
  array: [1, 2, 3],
  boolean: true,
  otherwise: 'a quoted string'
}
''')

        then:

        object.toString() == '''{
  "outer" : {
    "inner" : "here"
  },
  "number" : 22.7,
  "array" : [
    1,
    2,
    3
  ],
  "boolean" : true,
  "otherwise" : "a quoted string"
}'''
    }

    def "toCompactString()"() {
        def object = new JSONObject("kermit", "frog")

        expect:

        object.toString(true) == /{"kermit":"frog"}/
        object.toString(false) == /{
  "kermit" : "frog"
}/
    }

    def "print to PrintWriter)"() {
        def object = new JSONObject("kermit", "frog")

        expect:

        print(object, true) == /{"kermit":"frog"}/
        print(object, false) == /{
  "kermit" : "frog"
}/
    }

    def withPrintWriter(closure) {
        def caw = new CharArrayWriter()
        def pw = new PrintWriter(caw)

        closure(pw)

        pw.close()

        return caw.toString()
    }

    def print(object, compact) {

        withPrintWriter { pw -> object.print(pw, compact) }
    }

    def "prettyPrint() to PrintWriter"() {
        def object = new JSONObject("kermit", "frog")

        when:

        def json = withPrintWriter { pw -> object.prettyPrint(pw) }

        then:

        json == /{
  "kermit" : "frog"
}/
    }

    def "getString() at index"() {
        def array = new JSONArray("one", 2, false)

        expect:

        array.getString(0) == "one"
        array.getString(1) == "2"
        array.getString(2) == "false"
    }

    def "toMap() is not modifiable"() {
        def object = new JSONObject()

        def map = object.toMap()

        when:

        map.clear()

        then:

        thrown(UnsupportedOperationException)
    }

    def "can access contents of object as a map"() {
        def object = new JSONObject("foo", "bar")
                .put("null", JSONObject.NULL)
                .put("number", 6)

        when:

        def map = object.toMap()

        then:

        map.foo == "bar"
        map.number == 6
        map["null"].is(JSONObject.NULL)
    }

    def "the map returned by toMap() is live"() {
        def object = new JSONObject("foo", "bar")

        def map = object.toMap()

        when:

        object.put "foo", null

        then:

        object.length() == 0
        map.isEmpty()
    }

    def "can add new properties via putAll()"() {
        def object = new JSONObject("fred", "flintstone")

        when:

        def result = object.putAll([barney: "rubble", wilma: "flintstone"])

        then:

        object.toCompactString() == /{"fred":"flintstone","barney":"rubble","wilma":"flintstone"}/
    }

    def "in() where the value is not JSONObject is an exception"() {
        def object = new JSONObject("time", "to rock")

        when:

        object.in("time")

        then:

        IllegalStateException e = thrown()

        e.message == /JSONObject["time"] is not a JSONObject./
    }

    def "in() creates a JSONObject if necessary"() {
        def object = new JSONObject()

        when:

        object.in("nested").put("created", true)

        then:

        object.toCompactString() == /{"nested":{"created":true}}/
    }

    def "in() returns the JSONObject"() {
        def object = new JSONObject(/{nested: {}}/)

        when:

        object.in("nested").put("time", "to rock")

        then:

        object.toCompactString() == /{"nested":{"time":"to rock"}}/
    }

    def "constructor supports non-string keys and values"() {
        when:

        def object = new JSONObject(true, "truthy", false, false)

        then:

        object.has "true"
        object.getString("true") == "truthy"
        object.has "false"
        object.get("false").is false
    }

    private static copyViaSerialization(source) {

        def bos = new ByteArrayOutputStream()

        bos.withObjectOutputStream { it << source }

        def bis = new ByteArrayInputStream(bos.toByteArray())

        bis.withObjectInputStream { it.readObject() }
    }

    def "serialize and de-serialize"() {
        when:

        def source = new JSONObject("string", "a string", "null", JSONObject.NULL)

        def copy = copyViaSerialization source

        then:

        source == copy
    }
}
