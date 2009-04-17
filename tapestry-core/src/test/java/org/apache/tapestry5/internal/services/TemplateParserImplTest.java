// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.parser.*;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Locatable;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.test.TapestryTestConstants;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.String.format;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This is used to test the template parser ... and in some cases, the underlying behavior of the SAX APIs.
 */
public class TemplateParserImplTest extends InternalBaseTestCase
{

    private TemplateParser getParser()
    {
        return this.getService(TemplateParser.class);
    }

    private synchronized ComponentTemplate parse(String file)
    {
        Resource resource = getResource(file);

        return getParser().parseTemplate(resource);
    }

    private synchronized List<TemplateToken> tokens(String file)
    {
        return parse(file).getTokens();
    }

    private Resource getResource(String file)
    {
        String packageName = getClass().getPackage().getName();

        String path = packageName.replace('.', '/') + "/" + file;

        ClassLoader loader = getClass().getClassLoader();

        return new ClasspathResource(loader, path);
    }

    @SuppressWarnings("unchecked")
    private <T extends TemplateToken> T get(List l, int index)
    {
        Object raw = l.get(index);

        return (T) raw;
    }

    private void checkType(List<TemplateToken> l, int index, TokenType expected)
    {
        assertEquals(l.get(index).getTokenType(), expected);
    }

    private void checkLine(Locatable l, int expectedLineNumber)
    {
        assertEquals(l.getLocation().getLine(), expectedLineNumber);
    }

    @Test
    public void just_HTML()
    {
        Resource resource = getResource("justHTML.tml");

        ComponentTemplate template = getParser().parseTemplate(resource);

        assertSame(template.getResource(), resource);

        List<TemplateToken> tokens = template.getTokens();

        // They add up quick ...

        assertEquals(tokens.size(), 20);

        StartElementToken t0 = get(tokens, 0);

        // Spot check a few things ...

        assertEquals(t0.getName(), "html");
        assertEquals(t0.getNamespaceURI(), "");
        checkLine(t0, 1);

        TextToken t1 = get(tokens, 1);
        // Concerned this may not work cross platform.
        assertEquals(t1.getText(), "\n    ");

        StartElementToken t2 = get(tokens, 2);
        assertEquals(t2.getName(), "head");
        checkLine(t2, 2);

        TextToken t5 = get(tokens, 5);
        assertEquals(t5.getText(), "title");
        checkLine(t5, 3);

        get(tokens, 6);

        StartElementToken t12 = get(tokens, 12);
        assertEquals(t12.getName(), "p");

        AttributeToken t13 = get(tokens, 13);
        assertEquals(t13.getName(), "class");
        assertEquals(t13.getValue(), "important");
        assertEquals(t13.getNamespaceURI(), "");

        TextToken t14 = get(tokens, 14);
        // Simplify the text, converting consecutive whitespace to just a single space.
        assertEquals(t14.getText().replaceAll("\\s+", " ").trim(), "Tapestry rocks! Line 2");

        // Line number is the *start* line of the whole text block.
        checkLine(t14, 6);
    }

    @Test
    public void namespaced_element()
    {
        Resource resource = getResource("namespaced_element.tml");

        ComponentTemplate template = getParser().parseTemplate(resource);

        assertSame(template.getResource(), resource);

        List<TemplateToken> tokens = template.getTokens();

        // They add up quick ...

        assertEquals(tokens.size(), 8);

        StartElementToken t0 = get(tokens, 0);

        String expectedURI = "http://foo.com";

        assertEquals(t0.getNamespaceURI(), expectedURI);
        assertEquals(t0.getName(), "bar");

        DefineNamespacePrefixToken t1 = get(tokens, 1);

        assertEquals(t1.getNamespacePrefix(), "foo");
        assertEquals(t1.getNamespaceURI(), expectedURI);

        AttributeToken t2 = get(tokens, 2);

        assertEquals(t2.getName(), "biff");
        assertEquals(t2.getValue(), "baz");
        assertEquals(t2.getNamespaceURI(), expectedURI);

        StartElementToken t4 = get(tokens, 4);

        assertEquals(t4.getNamespaceURI(), "");
        assertEquals(t4.getName(), "gnip");

        // The rest are close tokens
    }

    @Test
    public void container_element()
    {
        List<TemplateToken> tokens = tokens("container_element.tml");

        assertEquals(tokens.size(), 4);

        TextToken t0 = get(tokens, 0);

        assertEquals(t0.getText().trim(), "A bit of text.");

        StartElementToken t1 = get(tokens, 1);

        assertEquals(t1.getName(), "foo");

        EndElementToken t2 = get(tokens, 2);

        assertNotNull(t2); // Keep compiler happy

        TextToken t3 = get(tokens, 3);

        assertEquals(t3.getText().trim(), "Some more text.");
    }

    @Test
    public void xml_entity()
    {
        List<TemplateToken> tokens = tokens("xmlEntity.tml");

        assertEquals(tokens.size(), 3);

        TextToken t = get(tokens, 1);

        // This is OK because the org.apache.tapestry5.dom.Text will convert the characters back into
        // XML entities.

        assertEquals(t.getText().trim(), "lt:< gt:> amp:&");
    }

    @Test
    public void html_entity()
    {
        String expectedURI = "http://www.w3.org/1999/xhtml";

        List<TemplateToken> tokens = tokens("html_entity.tml");

        assertEquals(tokens.size(), 5);

        DTDToken t0 = get(tokens, 0);

        assertEquals(t0.getName(), "html");
        assertEquals(t0.getPublicId(), "-//W3C//DTD XHTML 1.0 Transitional//EN");
        assertEquals(t0.getSystemId(), "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");

        StartElementToken t1 = get(tokens, 1);

        assertEquals(t1.getNamespaceURI(), expectedURI);
        assertEquals(t1.getName(), "html");

        DefineNamespacePrefixToken t2 = get(tokens, 2);

        assertEquals(t2.getNamespaceURI(), expectedURI);
        assertEquals(t2.getNamespacePrefix(), "");


        TextToken t = get(tokens, 3);

        // HTML entities are parsed into values that will ultimately
        // be output as numeric entities. This is less than ideal; would like
        // to find a way to keep the entities in their original form (possibly
        // involving a new type of token), but SAX seems to be fighting me on this.
        // You have to have a DOCTYPE just to parse a template that uses
        // an HTML entity.

        assertEquals(t.getText().trim(), "nbsp:[\u00a0]");
    }

    @Test
    public void cdata()
    {
        List<TemplateToken> tokens = tokens("cdata.tml");

        // Whitespace text tokens around the CDATA

        assertEquals(tokens.size(), 5);

        CDATAToken t = get(tokens, 2);

        assertEquals(t.getContent(), "CDATA: &lt;foo&gt; &amp; &lt;bar&gt; and <baz>");
        checkLine(t, 2);
    }

    @Test
    public void comment()
    {
        List<TemplateToken> tokens = tokens("comment.tml");

        // Again, whitespace before and after the comment adds some tokens

        assertEquals(tokens.size(), 3);

        CommentToken token1 = get(tokens, 1);

        // Comments are now trimmed of leading and trailing whitespace. This may mean
        // that the output isn't precisely what's in the template, but a) its a comment
        // and b) that's pretty much true of everything in the templates.

        assertEquals(token1.getComment(), "Single line comment");
    }

    @Test
    public void multiline_comment()
    {
        List<TemplateToken> tokens = tokens("multilineComment.tml");

        // Again, whitespace before and after the comment adds some tokens

        assertEquals(tokens.size(), 5);

        CommentToken t = get(tokens, 2);

        String comment = t.getComment().trim().replaceAll("\\s+", " ");

        assertEquals(comment, "Line one Line two Line three");
    }

    @Test
    public void component()
    {
        List<TemplateToken> tokens = tokens("component.tml");

        assertEquals(tokens.size(), 6);

        StartComponentToken t = get(tokens, 2);
        assertEquals(t.getId(), "fred");
        assertEquals(t.getComponentType(), "somecomponent");
        assertNull(t.getMixins());
        checkLine(t, 2);

        get(tokens, 3);
    }

    @Test
    public void component_with_body()
    {
        List<TemplateToken> tokens = tokens("componentWithBody.tml");

        assertEquals(tokens.size(), 7);

        get(tokens, 2);

        TextToken t = get(tokens, 3);

        assertEquals(t.getText().trim(), "fred's body");

        get(tokens, 4);
    }

    /**
     * @since 5.1.0.1
     */
    @Test
    public void comment_element_ignored()
    {
        List<TemplateToken> tokens = tokens("comment_element_ignored.tml");

        assertEquals(tokens.size(), 8);

        get(tokens, 2);

        TextToken t = get(tokens, 3);

        assertEquals(t.getText().trim(), "fred's body");

        EndElementToken end5 = get(tokens, 5);
        EndElementToken end7 = get(tokens, 7);
    }

    @Test
    public void root_element_is_component()
    {
        List<TemplateToken> tokens = tokens("root_element_is_component.tml");

        assertEquals(tokens.size(), 3);

        StartComponentToken start = get(tokens, 0);

        assertEquals(start.getId(), "fred");
        assertEquals(start.getComponentType(), "Fred");
        assertNull(start.getElementName());

        AttributeToken attr = get(tokens, 1);

        assertEquals(attr.getName(), "param");
        assertEquals(attr.getValue(), "value");

        assertTrue(EndElementToken.class.isInstance(tokens.get(2)));
    }

    @Test
    public void instrumented_element()
    {
        ComponentTemplate template = parse("instrumented_element.tml");
        List<TemplateToken> tokens = template.getTokens();

        assertEquals(tokens.size(), 3);

        StartComponentToken start = get(tokens, 0);

        assertEquals(start.getId(), "fred");
        assertEquals(start.getComponentType(), "Fred");
        assertEquals(start.getElementName(), "html");

        AttributeToken attr = get(tokens, 1);

        assertEquals(attr.getName(), "param");
        assertEquals(attr.getValue(), "value");

        assertTrue(EndElementToken.class.isInstance(tokens.get(2)));

        assertEquals(template.getComponentIds().keySet(), Arrays.asList("fred"));
    }

    @Test
    public void body_element()
    {
        List<TemplateToken> tokens = tokens("body_element.tml");

        // start(html), text, body, text, end(html)
        assertEquals(tokens.size(), 5);

        // javac bug requires use of isInstance() instead of instanceof
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=113218
        assertTrue(BodyToken.class.isInstance(get(tokens, 2)));
    }

    @Test
    public void component_with_parameters()
    {
        List<TemplateToken> tokens = tokens("componentWithParameters.tml");

        assertEquals(tokens.size(), 9);

        TemplateToken templateToken = get(tokens, 2);
        Location l = templateToken.getLocation();

        AttributeToken t1 = get(tokens, 3);

        // TODO: Not sure what order the attributes appear in. Order in the XML? Sorted
        // alphabetically? Random 'cause they're hashed?

        assertEquals(t1.getName(), "cherry");
        assertEquals(t1.getValue(), "bomb");
        assertSame(t1.getLocation(), l);

        AttributeToken t2 = get(tokens, 4);
        assertEquals(t2.getName(), "align");
        assertEquals(t2.getValue(), "right");
        assertSame(t2.getLocation(), l);

        TextToken t3 = get(tokens, 5);

        assertEquals(t3.getText().trim(), "fred's body");

        get(tokens, 6);
    }

    @Test
    public void component_with_mixins()
    {
        List<TemplateToken> tokens = tokens("component_with_mixins.tml");

        assertEquals(tokens.size(), 4);

        StartComponentToken token1 = get(tokens, 1);

        assertEquals(token1.getId(), "fred");
        assertEquals(token1.getComponentType(), "comp");
        assertEquals(token1.getMixins(), "Barney");
    }

    @Test
    public void empty_string_mixins_is_null()
    {
        List<TemplateToken> tokens = tokens("empty_string_mixins_is_null.tml");

        assertEquals(tokens.size(), 6);

        StartComponentToken t = get(tokens, 2);

        assertEquals(t.getId(), "fred");
        // We also check that empty string type is null ..
        assertNull(t.getComponentType());
        assertNull(t.getMixins());
    }

    @Test
    public void component_ids()
    {
        ComponentTemplate template = parse("component_ids.tml");

        Map<String, Location> map = template.getComponentIds();

        assertEquals(map.keySet(), CollectionFactory.newSet(Arrays.asList("bomb", "border", "zebra")));
    }

    @Test
    public void expansions_in_normal_text()
    {
        List<TemplateToken> tokens = tokens("expansions_in_normal_text.tml");

        assertEquals(tokens.size(), 7);

        TextToken t1 = get(tokens, 1);

        assertEquals(t1.getText().trim(), "Expansion #1[");

        ExpansionToken t2 = get(tokens, 2);
        assertEquals(t2.getExpression(), "expansion1");

        TextToken t3 = get(tokens, 3);
        assertEquals(t3.getText().replaceAll("\\s+", " "), "] Expansion #2[");

        ExpansionToken t4 = get(tokens, 4);
        assertEquals(t4.getExpression(), "expansion2");

        TextToken t5 = get(tokens, 5);
        assertEquals(t5.getText().trim(), "]");
    }

    @Test
    public void expansions_must_be_on_one_line()
    {
        List<TemplateToken> tokens = tokens("expansions_must_be_on_one_line.tml");

        assertEquals(tokens.size(), 3);

        TextToken t1 = get(tokens, 1);

        assertEquals(t1.getText().replaceAll("\\s+", " "), " ${expansions must be on a single line} ");
    }

    @Test
    public void multiple_expansions_on_one_line()
    {
        List<TemplateToken> tokens = tokens("multiple_expansions_on_one_line.tml");

        assertEquals(tokens.size(), 10);

        ExpansionToken token3 = get(tokens, 3);

        assertEquals(token3.getExpression(), "classLoader");

        TextToken token4 = get(tokens, 4);

        assertEquals(token4.getText(), " [");

        ExpansionToken token5 = get(tokens, 5);

        assertEquals(token5.getExpression(), "classLoader.class.name");

        TextToken token6 = get(tokens, 6);

        assertEquals(token6.getText(), "]");
    }

    @Test
    public void expansions_not_allowed_in_cdata()
    {
        List<TemplateToken> tokens = tokens("expansions_not_allowed_in_cdata.tml");

        assertEquals(tokens.size(), 5);

        CDATAToken t2 = get(tokens, 2);

        assertEquals(t2.getContent(), "${not-an-expansion}");
    }

    @Test
    public void expansions_not_allowed_in_attributes()
    {
        List<TemplateToken> tokens = tokens("expansions_not_allowed_in_attributes.tml");

        assertEquals(tokens.size(), 3);

        AttributeToken token1 = get(tokens, 1);

        assertEquals(token1.getName(), "exp");
        assertEquals(token1.getValue(), "${not-an-expansion}");
    }

    @Test
    public void parameter_element()
    {
        List<TemplateToken> tokens = tokens("parameter_element.tml");

        ParameterToken token4 = get(tokens, 4);
        assertEquals(token4.getName(), "fred");

        CommentToken token6 = get(tokens, 6);
        assertEquals(token6.getComment(), "fred content");

        TemplateToken token8 = get(tokens, 8);

        assertEquals(token8.getTokenType(), TokenType.END_ELEMENT);
    }

    /**
     * TAP5-112
     */
    @Test
    public void parameter_namespace_element()
    {
        List<TemplateToken> tokens = tokens("parameter_namespace_element.tml");

        ParameterToken token4 = get(tokens, 4);
        assertEquals(token4.getName(), "fred");

        CommentToken token6 = get(tokens, 6);
        assertEquals(token6.getComment(), "fred content");

        TemplateToken token8 = get(tokens, 8);

        assertEquals(token8.getTokenType(), TokenType.END_ELEMENT);
    }

    @Test
    public void complex_component_type()
    {
        List<TemplateToken> tokens = tokens("complex_component_type.tml");

        assertEquals(tokens.size(), 4);

        StartComponentToken token1 = get(tokens, 1);

        assertEquals(token1.getComponentType(), "subfolder/nifty");
    }

    /**
     * TAP5-66
     */
    @Test
    public void component_inside_library_namespace()
    {
        List<TemplateToken> tokens = tokens("component_inside_library_namespace.tml");

        assertEquals(tokens.size(), 4);

        StartComponentToken token1 = get(tokens, 1);

        assertEquals(token1.getComponentType(), "subfolder/nifty");
    }

    @Test
    public void block_element()
    {
        List<TemplateToken> tokens = tokens("block_element.tml");

        BlockToken token1 = get(tokens, 1);
        assertEquals(token1.getId(), "block0");

        CommentToken token2 = get(tokens, 2);
        assertEquals(token2.getComment(), "block0 content");

        BlockToken token4 = get(tokens, 4);
        assertNull(token4.getId());

        CommentToken token5 = get(tokens, 5);
        assertEquals(token5.getComment(), "anon block content");
    }

    @DataProvider
    public Object[][] parse_failure_data()
    {
        return new Object[][] {

                { "mixin_requires_id_or_type.tml",
                        "You may not specify mixins for element <span> because it does not represent a component (which requires either an id attribute or a type attribute).",
                        2 },

                { "unexpected_attribute_in_parameter_element.tml",
                        "Element <parameter> does not support an attribute named 'grok'. The only allowed attribute name is 'name'.",
                        4 },

                { "name_attribute_of_parameter_element_omitted.tml",
                        "The name attribute of the <parameter> element must be specified.", 4 },

                { "name_attribute_of_parameter_element_blank.tml",
                        "The name attribute of the <parameter> element must be specified.", 4 },

                { "unexpected_attribute_in_block_element.tml",
                        "Element <block> does not support an attribute named 'name'. The only allowed attribute name is 'id'.",
                        3 },

                { "parameter_namespace_with_attributes.tml",
                        ServicesMessages.parameterElementDoesNotAllowAttributes(), 4 },

                { "invalid_library_namespace_path.tml",
                        "The path portion of library namespace URI 'tapestry-library:subfolder/' is not valid", 2 },

                { "content_within_body_element.tml", "Content inside a Tapestry body element is not allowed", 2 },

                { "nested_content_element.tml",
                        "The <content> element may not be nested within another <content> element.", 3 },

                { "container_must_be_root.tml", "Element <container> is only valid as the root element of a template.",
                        3 },

                { "extend_must_be_root.tml", "Element <extend> is only valid as the root element of a template.", 3 },

                { "replace_must_be_under_extend.tml",
                        "The <replace> element may only appear directly within an extend element.", 3 },

                { "only_replace_within_extend.tml", "Child element of <extend> must be <replace>.", 2 },

                { "missing_id_in_replace_element.tml", "The <replace> element must have an id attribute.", 3 },

                { "extension_point_must_have_id.tml", "The <extension-point> element must have an id attribute.", 3 },

                { "misplaced_parameter.tml", "Block parameters are only allowed directly within component elements.",
                        5 },

        };
    }

    @Test(dataProvider = "parse_failure_data")
    public void parse_failure(String fileName, String errorMessageSubstring, int expectedLine)
    {
        try
        {
            parse(fileName);
            unreachable();
        }
        catch (TapestryException ex)
        {
            if (!ex.getMessage().contains(errorMessageSubstring))
            {
                throw new AssertionError(format("Message [%s] does not contain substring [%s].", ex.getMessage(),
                                                errorMessageSubstring));
            }

            assertEquals(ex.getLocation().getLine(), expectedLine);
        }
    }

    @DataProvider
    public Object[][] doctype_parsed_correctly_data()
    {
        return new Object[][] { { "xhtml1_strict_doctype.tml" }, { "xhtml1_transitional_doctype.tml" },
                { "xhtml1_frameset_doctype.tml" } };
    }

    @Test(dataProvider = "doctype_parsed_correctly_data")
    public void doctype_parsed_correctly(String fileName) throws Exception
    {
        List<TemplateToken> tokens = tokens(fileName);
        assertEquals(tokens.size(), 12);
        TextToken t = get(tokens, 9);
        assertEquals(t.getText().trim(), "<Test>");
    }

    @DataProvider
    public Object[][] doctype_token_added_correctly_data()
    {
        return new Object[][] {

                { "xhtml1_strict_doctype.tml", "html", "-//W3C//DTD XHTML 1.0 Strict//EN",
                        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" },

                { "xhtml1_transitional_doctype.tml", "html", "-//W3C//DTD XHTML 1.0 Transitional//EN",
                        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" },

                { "xhtml1_frameset_doctype.tml", "html", "-//W3C//DTD XHTML 1.0 Frameset//EN",
                        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd" },

                { "html4_strict_doctype.tml", "HTML", "-//W3C//DTD HTML 4.01//EN",
                        "http://www.w3.org/TR/html4/strict.dtd" },

                { "html4_transitional_doctype.tml", "HTML", "-//W3C//DTD HTML 4.01 Transitional//EN",
                        "http://www.w3.org/TR/html4/loose.dtd" },

                { "html4_frameset_doctype.tml", "HTML", "-//W3C//DTD HTML 4.01 Frameset//EN",
                        "http://www.w3.org/TR/html4/frameset.dtd" },

                { "system_doctype.xml", "foo", null,
                        "src/test/resources/org/apache/tapestry5/internal/services/simple.dtd" } };
    }

    @Test(dataProvider = "doctype_token_added_correctly_data")
    public void doctype_added_correctly(String fileName, String name, String publicId, String systemId) throws Exception
    {
        System.setProperty("user.dir", TapestryTestConstants.MODULE_BASE_DIR_PATH);

        List<TemplateToken> tokens = tokens(fileName);
        DTDToken t0 = get(tokens, 0);
        assertEquals(t0.getName(), name);
        assertEquals(t0.getPublicId(), publicId);
        assertEquals(t0.getSystemId(), systemId);
    }

    @Test
    public void invalid_component_id() throws Exception
    {
        try
        {
            parse("invalid_component_id.tml");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Component id 'not-valid' is not valid");
        }
    }

    @Test
    public void invalid_block_id() throws Exception
    {
        try
        {
            parse("invalid_block_id.tml");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Block id 'not-valid' is not valid");
        }
    }

    /**
     * Because of common code, this covers t:block and t:parameter.
     */
    @Test
    public void space_preserved_in_block() throws Exception
    {
        List<TemplateToken> tokens = tokens("space_preserved_in_block.tml");

        TextToken token1 = get(tokens, 1);

        assertEquals(token1.getText(), "\n" + "        line in the middle\n" + "    ");
    }

    /**
     * t:container is a bit of a different code path than t:block/t:parameter
     */
    @Test
    public void space_preserved_in_container() throws Exception
    {
        List<TemplateToken> tokens = tokens("space_preserved_in_container.tml");

        TextToken token0 = get(tokens, 0);
        assertEquals(token0.getText(), "\n" + "    ");

        TextToken token2 = get(tokens, 2);
        assertEquals(token2.getText(), "\n" + "        some text\n" + "    ");
    }

    @Test
    public void minimal_whitespace_maintained_inside_tags() throws Exception
    {
        List<TemplateToken> tokens = tokens("minimal_whitespace_maintained_inside_tags.tml");

        // A line feed or carriage return surrounded by other whitespace is reduced to
        // just a line feed.

        TextToken token1 = get(tokens, 1);
        assertEquals(token1.getText(), "\nWhitespace\n");


        TextToken token5 = get(tokens, 5);
        assertEquals(token5.getText(), "\nis maintained.\n");
    }

    /**
     * TAP5-563
     */
    @Test
    public void content_element() throws Exception
    {
        List<TemplateToken> tokens = tokens("content_element.tml");

        assertEquals(tokens.size(), 5);

        StartComponentToken token0 = get(tokens, 0);
        assertEquals(token0.getElementName(), "body");
        assertEquals(token0.getComponentType(), "layout");

        StartElementToken token1 = get(tokens, 1);
        assertEquals(token1.getName(), "p");

        TextToken token2 = get(tokens, 2);

        assertEquals(token2.getText(), "Page content");

        checkType(tokens, 3, TokenType.END_ELEMENT);
        checkType(tokens, 4, TokenType.END_ELEMENT);
    }

    @Test
    public void overrides() throws Exception
    {
        ComponentTemplate template = parse("overrides.tml");

        assertTrue(template.isExtension());

        assertEquals(template.getTokens().size(), 0);

        List<TemplateToken> alpha = template.getExtensionPointTokens("alpha");

        assertEquals(alpha.size(), 1);

        TextToken alpha0 = get(alpha, 0);
        assertEquals(alpha0.getText(), "beta");

        List<TemplateToken> gamma = template.getExtensionPointTokens("gamma");
        assertEquals(gamma.size(), 3);

        StartElementToken gamma0 = get(gamma, 0);
        assertEquals(gamma0.getName(), "p");

        TextToken gamma1 = get(gamma, 1);

        assertEquals(gamma1.getText(), "Hi!");

        checkType(gamma, 2, TokenType.END_ELEMENT);
    }

    @Test
    public void extension_point() throws Exception
    {
        ComponentTemplate template = parse("extension_point.tml");

        ExtensionPointToken expansion = get(template.getTokens(), 2);

        assertEquals(expansion.getExtentionPointId(), "title");

        List<TemplateToken> title = template.getExtensionPointTokens("title");

        assertEquals(title.size(), 3);

        StartElementToken title0 = get(title, 0);
        assertEquals(title0.getName(), "h1");

        TextToken title1 = get(title, 1);
        assertEquals(title1.getText(), "Default Title");

        checkType(title, 2, TokenType.END_ELEMENT);
    }
}
