package org.apache.tapestry5.internal.services

import java.time.Duration

import org.apache.tapestry5.internal.test.TestableRequestImpl

import spock.lang.Specification

class CookieBuilderSpec extends Specification {

    static String CONTEXT_PATH = "/ctx"
    
    def createCookiesFixture(cookies)
    {
        
        return new CookiesImpl(
            new TestableRequestImpl(CONTEXT_PATH),
            null,
            [ addCookie: { cookies << it } ] as CookieSink,
            CONTEXT_PATH,
            1_000L * 1_000L)
    }

    def "write Cookie with maxAge as Duration"()
    {
        given:
        def cookies = []
        def cookiesFixture = createCookiesFixture(cookies)
        
        when:
        def builder = cookiesFixture.getBuilder("name", "value")
        builder.maxAge = maxAge
        builder.write()
        
        then:
        cookies.size() == 1
        cookies[0].maxAge == expectedMaxAge
        
        where:
        maxAge                                     | expectedMaxAge
        Duration.ZERO                              | 0
        Duration.ofMillis(-2)                      | -1
        Duration.ofHours(2L)                       | 60 * 60 * 2 
        Duration.ofSeconds(Integer.MAX_VALUE + 1L) | Integer.MAX_VALUE
    }

}
