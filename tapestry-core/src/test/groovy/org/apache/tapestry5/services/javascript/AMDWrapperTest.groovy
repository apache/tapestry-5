package org.apache.tapestry5.services.javascript

import org.apache.tapestry5.commons.Resource
import org.apache.tapestry5.test.TapestryTestCase
import org.testng.annotations.Test;

class AMDWrapperTest extends TapestryTestCase {

    @Test
    void AMDwrapper_without_dependencies(){
        Resource resource = newMock(Resource)
        expect(resource.openStream()).andReturn(new ByteArrayInputStream("alert('Hello World!');".bytes))

        replay()

        def wrapper = new AMDWrapper(resource)

        def moduleConfiguration = wrapper.asJavaScriptModuleConfiguration()

        assertFalse(moduleConfiguration.needsConfiguration)
        assertEquals(moduleConfiguration.resource.openStream().text,
                """define([], function(){
alert('Hello World!');
});""")
    }

    @Test
    void AMDWrapper_with_named_dependencies(){
        Resource resource = newMock(Resource)
        expect(resource.openStream()).andReturn(new ByteArrayInputStream('$("body").css("background-color", "pink");'.bytes))

        replay()

        def wrapper = new AMDWrapper(resource)
        wrapper.require("jquery", '$')

        def moduleConfiguration = wrapper.asJavaScriptModuleConfiguration()
        assertFalse(moduleConfiguration.needsConfiguration)
        assertEquals(moduleConfiguration.resource.openStream().text,
                '''define(["jquery"], function($){
$("body").css("background-color", "pink");
});''')

    }

    @Test
    void AMDWrapper_with_return_expression(){
        Resource resource = newMock(Resource)
        expect(resource.openStream()).andReturn(new ByteArrayInputStream('var myImportantVar = 42;'.bytes))

        replay()

        def wrapper = new AMDWrapper(resource)
        wrapper.setReturnExpression("myImportantVar")

        def moduleConfiguration = wrapper.asJavaScriptModuleConfiguration()
        assertFalse(moduleConfiguration.needsConfiguration)
        assertEquals(moduleConfiguration.resource.openStream().text,
                '''define([], function(){
var myImportantVar = 42;
return myImportantVar;
});''')
    }
}
