package org.apache.tapestry5.services.javascript

import org.apache.tapestry5.ioc.Resource

import spock.lang.Specification

class AMDWrapperSpec extends Specification {

    def "AMD wrapper without dependencies"(){
        setup:
        Resource resource = Mock()
        when:
        def wrapper = new AMDWrapper(resource)
        def moduleConfiguration = wrapper.asJavaScriptModuleConfiguration()
        then:
        !moduleConfiguration.needsConfiguration
        when:
        def amdModuleContent = moduleConfiguration.resource.openStream().text
        then:
        amdModuleContent ==
                """define([], function(){
alert('Hello World!');
});"""
        1 * resource.openStream() >> new ByteArrayInputStream("alert('Hello World!');".bytes)
    }

    def "AMD wrapper with named dependencies"(){
        setup:
        Resource resource = Mock()
        when:
        def wrapper = new AMDWrapper(resource)
        wrapper.require("jquery", '$')
        def moduleConfiguration = wrapper.asJavaScriptModuleConfiguration()
        then:
        !moduleConfiguration.needsConfiguration
        when:
        def amdModuleContent = moduleConfiguration.resource.openStream().text
        then:
        amdModuleContent ==
                '''define(["jquery"], function($){
$("body").css("background-color", "pink");
});'''
        1 * resource.openStream() >> new ByteArrayInputStream('$("body").css("background-color", "pink");'.bytes)
    }

    def "AMD wrapper with return expression"(){
        setup:
        Resource resource = Mock()
        when:
        def wrapper = new AMDWrapper(resource)
        wrapper.setReturnExpression("myImportantVar")
        def moduleConfiguration = wrapper.asJavaScriptModuleConfiguration()
        then:
        !moduleConfiguration.needsConfiguration
        when:
        def amdModuleContent = moduleConfiguration.resource.openStream().text
        then:
        amdModuleContent ==
                '''define([], function(){
var myImportantVar = 42;
return myImportantVar;
});'''
        1 * resource.openStream() >> new ByteArrayInputStream('var myImportantVar = 42;'.bytes)
    }
}
