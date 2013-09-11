//  Copyright 2011-2013 The Apache Software Foundation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.apache.tapestry5.test;

import java.lang.annotation.*;

/**
 * To be used on Selenium-based integration tests that extend {@link SeleniumTestCase} as an alternative to using a
 * TestNG XML configuration file. Using the XML file, it's intricate to run <em>individual</em> test classes or
 * methods using IDEA's or Eclipse's TestNG integration.
 *
 * <b>Parameters coming from a TestNG XML configuration file take precedence over those supplied with the annotation.</b>
 *
 * Configures the container to be started for the tests and the browser to be used.
 * @since 5.4
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TapestryTestConfiguration
{
    /**
     * The folder for the web application root relative to the working directory. Defaults to "src/main/webapp".
     */
    String webAppFolder() default "src/main/webapp";

    /**
     * Which container to use. Can be one of {@link SeleniumTestCase#JETTY_7} or {@link SeleniumTestCase#TOMCAT_6}.
     * Defaults to {@link SeleniumTestCase#JETTY_7}.
     */
    String container() default SeleniumTestCase.JETTY_7;

    /**
     * The context path to make the application available under. Defaults to "", i.e. the context root.
     */
    String contextPath() default "";

    /**
     * The port to listen on for HTTP requests. Defaults to "9090".
     */
    int port() default 9090;

    /**
     * The port to listen on fot HTTPS requests. Defaults to "8443".
     */
    int sslPort() default 8443;

    /**
     * The browser start command to use with Selenium. Defaults to "*firefox".
     */
    String browserStartCommand() default "*firefox";
}
