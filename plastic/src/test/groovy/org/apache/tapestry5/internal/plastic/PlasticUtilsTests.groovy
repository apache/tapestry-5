// Copyright 2011, 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.plastic

import org.apache.tapestry5.plastic.PlasticUtils
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
        "java.lang.String"    | String.class
        "java.lang.Integer[]" | Integer[].class
        "java.lang.Long[][]"  | Long[][].class
        "void"                | void.class
        "int"                 | int.class
        "int[]"               | int[].class
        "float[][]"           | float[][].class
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

        String.class  | String.class
        boolean.class | Boolean.class
        double.class  | Double.class
        int[].class   | int[].class
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
