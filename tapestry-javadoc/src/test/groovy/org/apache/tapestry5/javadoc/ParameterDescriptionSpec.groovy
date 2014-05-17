package org.apache.tapestry5.javadoc

import spock.lang.Specification

import com.sun.javadoc.FieldDoc
import com.sun.tools.doclets.internal.toolkit.util.TextTag
import com.sun.tools.javadoc.TagImpl


class ParameterDescriptionSpec extends Specification {

	def "Parameter description without embedded tags is passed through"(){
		setup:
		FieldDoc fieldDoc = Mock()
		def inlineTags = [
			new TextTag(null, "Plain text")
		]
		ParameterDescription parameterDescription = new ParameterDescription(fieldDoc, "parameter", "String", "value", "literal", false, true, false, "1", false)
		when:
		def extracted = parameterDescription.extractDescription()
		then:
		1 * fieldDoc.inlineTags() >> inlineTags
		extracted == "Plain text"
	}

	def "Embedded code tags are turned into HTML <code> elements"(){
		setup:
		FieldDoc fieldDoc = Mock()
		def inlineTags = [
			new TagImpl(null, "@code", "blah")
		]
		ParameterDescription parameterDescription = new ParameterDescription(fieldDoc, "parameter", "String", "value", "literal", false, true, false, "1", false)
		when:
		def extracted = parameterDescription.extractDescription()
		then:
		1 * fieldDoc.inlineTags() >> inlineTags
		extracted == "<code>blah</code>"
	}

	// TAP5-2266
	def "HTML in embedded code tags is escaped"(){
		setup:
		FieldDoc fieldDoc = Mock()
		def inlineTags = [
			new TextTag(null, "This renders the component as a "),
			new TagImpl(null, "@code", "<li>"),
			new TextTag(null, " (instead of a "),
			new TagImpl(null, "@code", "<div>"),
			new TextTag(null, ")")
		]
		ParameterDescription parameterDescription = new ParameterDescription(fieldDoc, "parameter", "String", "value", "literal", false, true, false, "1", false)
		when:
		def extracted = parameterDescription.extractDescription()
		then:
		1 * fieldDoc.inlineTags() >> inlineTags
		extracted == "This renders the component as a <code>&lt;li&gt;</code> (instead of a <code>&lt;div&gt;</code>)"
	}


	def "Characters with special meaning are escaped"(){
		setup:
		FieldDoc fieldDoc = Mock()
		def inlineTags = [
			new TextTag(null, "Javadoc with < character")
		]
		ParameterDescription parameterDescription = new ParameterDescription(fieldDoc, "parameter", "String", "value", "literal", false, true, false, "1", false)
		when:
		def extracted = parameterDescription.extractDescription()
		then:
		1 * fieldDoc.inlineTags() >> inlineTags
		extracted == "Javadoc with &lt; character"
	}

	def "Entities in Javadoc are left alone"(){
		setup:
		FieldDoc fieldDoc = Mock()
		def inlineTags = [
			new TextTag(null, "Text &amp; entity")
		]
		ParameterDescription parameterDescription = new ParameterDescription(fieldDoc, "parameter", "String", "value", "literal", false, true, false, "1", false)
		when:
		def extracted = parameterDescription.extractDescription()
		then:
		1 * fieldDoc.inlineTags() >> inlineTags
		extracted == "Text &amp; entity"
	}

	def "Un-safe tags in Javadoc are removed"(){
		setup:
		FieldDoc fieldDoc = Mock()
		def inlineTags = [
			new TextTag(null, "We don't <br>want new lines or</td> table stuff")
		]
		ParameterDescription parameterDescription = new ParameterDescription(fieldDoc, "parameter", "String", "value", "literal", false, true, false, "1", false)
		when:
		def extracted = parameterDescription.extractDescription()
		then:
		1 * fieldDoc.inlineTags() >> inlineTags
		extracted == "We don't want new lines or table stuff"
	}

	def "#src in Javadoc becomes #target in HTML"(){
		setup:
		FieldDoc fieldDoc = Mock()
		def inlineTags = [
			new TextTag(null, src)
		]
		ParameterDescription parameterDescription = new ParameterDescription(fieldDoc, "parameter", "String", "value", "literal", false, true, false, "1", false)
		when:
		def extracted = parameterDescription.extractDescription()
		then:
		1 * fieldDoc.inlineTags() >> inlineTags
		extracted == target
		where:
		src     | target
		"&"     | "&amp;"
		"&amp;" | "&amp;"
		"<"     | "&lt;"
		"<b>"   | "<b>"

	}



}
