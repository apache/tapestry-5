package org.apache.tapestry5.internal.plastic

import org.apache.tapestry5.plastic.PlasticUtils

import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class PlasticUtilsTests extends Specification
{
    def "Class name #className should convert to descriptor #desc"()
    {
        expect:
        PlasticInternalUtils.toDescriptor(className) == desc

        where:
        className            | desc
        "void"               | "V"
        "boolean"            | "Z"
        "boolean[]"          | "[Z"
        "java.lang.Integer"  | "Ljava/lang/Integer;"
        "java.lang.String[]" | "[Ljava/lang/String;"
        "java.lang.Long[][]" | "[[Ljava/lang/Long;"
    }

    @Issue(['TAP5-1995', 'TAP5-2365'])
    def "Do not urlencode file paths"()
    {
        expect:
        PlasticInternalUtils.getStreamForPath(getClass().classLoader, 'webapp##01.test') != null
    }

    def "Descriptor #descriptor as class name should be #className"()
    {
        expect:
        PlasticInternalUtils.objectDescriptorToClassName(descriptor) == className
        where:
        descriptor           | className
        "Ljava/lang/String;" | "java.lang.String"
        'Lfoo/bar/Baz$Biff;' | 'foo.bar.Baz$Biff'
    }

    def "toClass('#javaName') should be #expectedClass"()
    {
        expect:

        PlasticInternalUtils.toClass(getClass().classLoader, javaName) == expectedClass

        where:

        javaName              | expectedClass
        "java.lang.String"    | String
        "java.lang.Integer[]" | Integer[]
        "java.lang.Long[][]"  | Long[][]
        "void"                | void
        "int"                 | int
        "int[]"               | int[]
        "float[][]"           | float[][]
    }

    def "not object descriptor is an exception"()
    {
        when:
        PlasticInternalUtils.objectDescriptorToClassName("I")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Input 'I' is not an object descriptor."
    }

    def "Field '#fieldName' should convert to property '#propertyName'"()
    {
        expect:

        PlasticInternalUtils.toPropertyName(fieldName) == propertyName

        where:

        fieldName                 | propertyName

        "fred"                    | "fred"

        "m_fred"                  | "fred"

        "M_fred"                  | "fred"

        "barney_"                 | "barney"

        "m_wilma_"                | "wilma"

        "foo_bar"                 | "foo_bar"

        "_______overkill________" | "overkill"

        "m____overkill____"       | "overkill"

        "m_"                      | "m"
    }

    def "bad input for field name to property name conversion"()
    {
        when:

        PlasticInternalUtils.toPropertyName ""

        then:

        thrown(IllegalArgumentException)
    }

    def "capitalize('#input') should be '#output'"()
    {
        expect:

        PlasticInternalUtils.capitalize(input) == output

        where:

        input     | output

        "Hello"   | "Hello"

        "g"       | "G"

        "goodbye" | "Goodbye"
    }

    def "toWrapperType #primitiveType should be #wrapperType"()
    {
        expect:

        PlasticUtils.toWrapperType(primitiveType) == wrapperType
        where:

        primitiveType | wrapperType

        String  | String
        boolean | Boolean
        double  | Double
        int[]   | int[]
    }

    def "isPrimitive #name should be #expected"()
    {
        expect:

        PlasticUtils.isPrimitive(name) == expected

        where:

        name                | expected

        "boolean"           | true
        "int"               | true
        "java.lang.Integer" | false
    }
}
