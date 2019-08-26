package org.apache.tapestry5.javadoc

import com.sun.source.doctree.DocCommentTree
import com.sun.source.doctree.DocTree
import com.sun.source.doctree.LiteralTree
import com.sun.source.doctree.TextTree
import spock.lang.Specification

import javax.lang.model.element.VariableElement

class ParameterDescriptionSpec extends Specification {

    def "Parameter description without embedded tags is passed through"() {
        setup:
        DocCommentTreeProvider docCommentTreeProvider = Mock()
        DocCommentTree docCommentTree = Mock()
        VariableElement fieldDoc = Mock()

        def fullBody = [mockTextTree("Plain text")]
        ParameterDescription parameterDescription =
                new ParameterDescription(
                        fieldDoc,
                        "parameter",
                        "String",
                        "value",
                        "literal",
                        false,
                        true,
                        false,
                        "1",
                        false,
                        docCommentTreeProvider)
        when:
        def extracted = parameterDescription.extractDescription()
        then:
        1 * docCommentTreeProvider.getDocCommentTree(fieldDoc) >> docCommentTree
        1 * docCommentTree.getFullBody() >> fullBody
        extracted == "Plain text"
    }

    private static TextTree mockTextTree(String body) {
        ['getKind': { DocTree.Kind.TEXT },
         'getBody': { body }
        ] as TextTree
    }

    private static LiteralTree mockCodeTree(String body) {
        ['getKind': { DocTree.Kind.CODE },
         'getBody': { mockTextTree(body) }
        ] as LiteralTree
    }

    def "Embedded code tags are turned into HTML <code> elements"() {
        setup:
        DocCommentTreeProvider docCommentTreeProvider = Mock()
        DocCommentTree docCommentTree = Mock()
        VariableElement fieldDoc = Mock()

        def fullBody = [
                mockCodeTree("blah")
        ]
        ParameterDescription parameterDescription =
                new ParameterDescription(
                        fieldDoc,
                        "parameter",
                        "String",
                        "value",
                        "literal",
                        false,
                        true,
                        false,
                        "1",
                        false,
                        docCommentTreeProvider)
        when:
        def extracted = parameterDescription.extractDescription()
        then:
        1 * docCommentTreeProvider.getDocCommentTree(fieldDoc) >> docCommentTree
        1 * docCommentTree.getFullBody() >> fullBody
        extracted == "<code>blah</code>"
    }

    // TAP5-2266
    def "HTML in embedded code tags is escaped"() {
        setup:
        DocCommentTreeProvider docCommentTreeProvider = Mock()
        DocCommentTree docCommentTree = Mock()
        VariableElement fieldDoc = Mock()

        def fullBody = [
                mockTextTree("This renders the component as a "),
                mockCodeTree("<li>"),
                mockTextTree(" (instead of a "),
                mockCodeTree("<div>"),
                mockTextTree(")")
        ]
        ParameterDescription parameterDescription =
                new ParameterDescription(
                        fieldDoc,
                        "parameter",
                        "String",
                        "value",
                        "literal",
                        false,
                        true,
                        false,
                        "1",
                        false,
                        docCommentTreeProvider)
        when:
        def extracted = parameterDescription.extractDescription()
        then:
        1 * docCommentTreeProvider.getDocCommentTree(fieldDoc) >> docCommentTree
        1 * docCommentTree.getFullBody() >> fullBody
        extracted == "This renders the component as a <code>&lt;li&gt;</code> (instead of a <code>&lt;div&gt;</code>)"
    }

    def "Characters with special meaning are escaped"() {
        setup:
        DocCommentTreeProvider docCommentTreeProvider = Mock()
        DocCommentTree docCommentTree = Mock()
        VariableElement fieldDoc = Mock()

        def fullBody = [
                mockTextTree("Javadoc with < character")
        ]
        ParameterDescription parameterDescription =
                new ParameterDescription(
                        fieldDoc,
                        "parameter",
                        "String",
                        "value",
                        "literal",
                        false,
                        true,
                        false,
                        "1",
                        false,
                        docCommentTreeProvider)
        when:
        def extracted = parameterDescription.extractDescription()
        then:
        1 * docCommentTreeProvider.getDocCommentTree(fieldDoc) >> docCommentTree
        1 * docCommentTree.getFullBody() >> fullBody
        extracted == "Javadoc with &lt; character"
    }

    def "Entities in Javadoc are left alone"() {
        setup:
        DocCommentTreeProvider docCommentTreeProvider = Mock()
        DocCommentTree docCommentTree = Mock()
        VariableElement fieldDoc = Mock()

        def fullBody = [
                mockTextTree("Text &amp; entity")
        ]
        ParameterDescription parameterDescription =
                new ParameterDescription(
                        fieldDoc,
                        "parameter",
                        "String",
                        "value",
                        "literal",
                        false,
                        true,
                        false,
                        "1",
                        false,
                        docCommentTreeProvider)
        when:
        def extracted = parameterDescription.extractDescription()
        then:
        1 * docCommentTreeProvider.getDocCommentTree(fieldDoc) >> docCommentTree
        1 * docCommentTree.getFullBody() >> fullBody
        extracted == "Text &amp; entity"
    }

    def "Un-safe tags in Javadoc are removed"() {
        setup:
        DocCommentTreeProvider docCommentTreeProvider = Mock()
        DocCommentTree docCommentTree = Mock()
        VariableElement fieldDoc = Mock()

        def fullBody = [
                mockTextTree("We don't <br>want new lines or</td> table stuff")
        ]
        ParameterDescription parameterDescription =
                new ParameterDescription(
                        fieldDoc,
                        "parameter",
                        "String",
                        "value",
                        "literal",
                        false,
                        true,
                        false,
                        "1",
                        false,
                        docCommentTreeProvider)
        when:
        def extracted = parameterDescription.extractDescription()
        then:
        1 * docCommentTreeProvider.getDocCommentTree(fieldDoc) >> docCommentTree
        1 * docCommentTree.getFullBody() >> fullBody
        extracted == "We don't want new lines or table stuff"
    }

    def "#src in Javadoc becomes #target in HTML"() {
        setup:
        DocCommentTreeProvider docCommentTreeProvider = Mock()
        DocCommentTree docCommentTree = Mock()
        VariableElement fieldDoc = Mock()

        def fullBody = [
                mockTextTree(src)
        ]
        ParameterDescription parameterDescription =
                new ParameterDescription(
                        fieldDoc,
                        "parameter",
                        "String",
                        "value",
                        "literal",
                        false,
                        true,
                        false,
                        "1",
                        false,
                        docCommentTreeProvider)
        when:
        def extracted = parameterDescription.extractDescription()
        then:
        1 * docCommentTreeProvider.getDocCommentTree(fieldDoc) >> docCommentTree
        1 * docCommentTree.getFullBody() >> fullBody
        extracted == target
        where:
        src     | target
        "&"     | "&amp;"
        "&amp;" | "&amp;"
        "<"     | "&lt;"
        "<b>"   | "<b>"
    }
}
