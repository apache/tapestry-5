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

package org.apache.tapestry5.test;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Visitor;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.SingleKeySymbolProvider;
import org.apache.tapestry5.internal.TapestryAppInitializer;
import org.apache.tapestry5.internal.test.PageTesterContext;
import org.apache.tapestry5.internal.test.PageTesterModule;
import org.apache.tapestry5.internal.test.TestableRequest;
import org.apache.tapestry5.internal.test.TestableResponse;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.ApplicationGlobals;
import org.apache.tapestry5.services.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * This class is used to run a Tapestry app in a single-threaded, in-process testing environment. You can ask it to
 * render a certain page and check the DOM object created. You can also ask it to click on a link element in the DOM
 * object to get the next page. Because no servlet container is required, it is very fast and you can directly debug
 * into your code in your IDE.
 */
public class PageTester
{
    @SuppressWarnings({ "FieldCanBeLocal" })
    private final Logger logger = LoggerFactory.getLogger(PageTester.class);

    private final Registry registry;

    private final TestableRequest request;

    private final TestableResponse response;

    // private final StrategyRegistry<ComponentInvoker> invokerRegistry;

    private final RequestHandler requestHandler;

    public static final String DEFAULT_CONTEXT_PATH = "src/main/webapp";

    private static final String DEFAULT_SUBMIT_VALUE_ATTRIBUTE = "Submit Query";

    /**
     * Initializes a PageTester without overriding any services and assuming that the context root is in
     * src/main/webapp.
     *
     * @see #PageTester(String, String, String, Class[])
     */
    public PageTester(String appPackage, String appName)
    {
        this(appPackage, appName, DEFAULT_CONTEXT_PATH);
    }

    /**
     * Initializes a PageTester that acts as a browser and a servlet container to test drive your Tapestry pages.
     *
     * @param appPackage    The same value you would specify using the tapestry.app-package context parameter. As this
     *                      testing environment is not run in a servlet container, you need to specify it.
     * @param appName       The same value you would specify as the filter name. It is used to form the name of the
     *                      module class for your app. If you don't have one, pass an empty string.
     * @param contextPath   The path to the context root so that Tapestry can find the templates (if they're put
     *                      there).
     * @param moduleClasses Classes of additional modules to load
     */
    public PageTester(String appPackage, String appName, String contextPath, Class... moduleClasses)
    {
        Defense.notBlank(appPackage, "appPackage");
        Defense.notBlank(appName, "appName");
        Defense.notBlank(contextPath, "contextPath");

        SymbolProvider provider = new SingleKeySymbolProvider(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, appPackage);

        TapestryAppInitializer initializer = new TapestryAppInitializer(logger, provider, appName,
                                                                        PageTesterModule.TEST_MODE);

        initializer.addModules(PageTesterModule.class);
        initializer.addModules(moduleClasses);
        initializer.addModules(provideExtraModuleDefs());

        registry = initializer.createRegistry();

        request = registry.getService(TestableRequest.class);
        response = registry.getService(TestableResponse.class);

        ApplicationGlobals globals = registry.getObject(ApplicationGlobals.class, null);

        globals.storeContext(new PageTesterContext(contextPath));

        requestHandler = registry.getService("RequestHandler", RequestHandler.class);

        request.setLocale(Locale.ENGLISH);
    }

    /**
     * Overridden in subclasses to provide additional module definitions beyond those normally located. This
     * implementation returns an empty array.
     */
    protected ModuleDef[] provideExtraModuleDefs()
    {
        return new ModuleDef[0];
    }


    /**
     * Invoke this method when done using the PageTester; it shuts down the internal {@link
     * org.apache.tapestry5.ioc.Registry} used by the tester.
     */
    public void shutdown()
    {
        registry.shutdown();
    }


    /**
     * Returns the Registry that was created for the application.
     */
    public Registry getRegistry()
    {
        return registry;
    }

    /**
     * Allows a service to be retrieved via its service interface.  Use {@link #getRegistry()} for more complicated
     * queries.
     *
     * @param serviceInterface used to select the service
     */
    public <T> T getService(Class<T> serviceInterface)
    {
        return registry.getService(serviceInterface);
    }

    /**
     * Renders a page specified by its name.
     *
     * @param pageName The name of the page to be rendered.
     * @return The DOM created. Typically you will assert against it.
     */
    public Document renderPage(String pageName)
    {
        request.clear().setPath("/" + pageName);

        while (true)
        {
            try
            {
                response.clear();

                boolean handled = requestHandler.service(request, response);

                if (!handled)
                {
                    throw new RuntimeException(
                            String.format("Request was not handled: '%s' may not be a valid page name.", pageName));
                }

                Link link = response.getRedirectLink();

                if (link != null)
                {
                    setupRequestFromLink(link);
                    continue;
                }

                Document result = response.getRenderedDocument();

                if (result == null)
                    throw new RuntimeException(
                            String.format("Render of page '%s' did not result in a Document.", pageName));


                return result;
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }

        }

    }

    /**
     * Simulates a click on a link.
     *
     * @param linkElement The Link object to be "clicked" on.
     * @return The DOM created. Typically you will assert against it.
     */

    public Document clickLink(Element linkElement)
    {
        Defense.notNull(linkElement, "link");

        validateElementName(linkElement, "a");

        String href = extractNonBlank(linkElement, "href");

        setupRequestFromURI(href);

        return runComponentEventRequest();
    }

    private String extractNonBlank(Element element, String attributeName)
    {
        String result = element.getAttribute(attributeName);

        if (InternalUtils.isBlank(result))
            throw new RuntimeException(
                    String.format("The %s attribute of the <%s> element was blank or missing.",
                                  element.getName(), attributeName));

        return result;
    }

    private void validateElementName(Element element, String expectedElementName)
    {
        if (!element.getName().equalsIgnoreCase(expectedElementName))
            throw new RuntimeException(
                    String.format("The element must be type '%s', not '%s'.", expectedElementName, element.getName()));
    }

    private Document runComponentEventRequest()
    {
        while (true)
        {
            response.clear();

            try
            {
                boolean handled = requestHandler.service(request, response);

                if (!handled)
                    throw new RuntimeException(String.format("Request for path '%s' was not handled by Tapestry.",
                                                             request.getPath()));

                Link link = response.getRedirectLink();

                if (link != null)
                {
                    setupRequestFromLink(link);
                    continue;
                }

                Document result = response.getRenderedDocument();

                if (result == null)
                    throw new RuntimeException(
                            String.format("Render request '%s' did not result in a Document.", request.getPath()));

                return result;
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }

        }


    }

    private void setupRequestFromLink(Link link)
    {
        setupRequestFromURI(link.toRedirectURI());
    }

    private void setupRequestFromURI(String URI)
    {
        String linkPath = stripContextFromPath(URI);

        int comma = linkPath.indexOf('?');

        String path = comma < 0
                      ? linkPath
                      : linkPath.substring(0, comma);

        request.clear().setPath(path);

        if (comma > 0)
            decodeParametersIntoRequest(path.substring(comma + 1));
    }

    private void decodeParametersIntoRequest(String queryString)
    {
        if (InternalUtils.isNonBlank(queryString))
            throw new RuntimeException("Have not yet implemented this method.");
    }

    private String stripContextFromPath(String path)
    {
        String contextPath = request.getContextPath();

        if (contextPath.equals("")) return path;

        if (!path.startsWith(contextPath))
            throw new RuntimeException(String.format("Path '%s' does not start with context path '%s'.",
                                                     path, contextPath));

        return path.substring(contextPath.length());
    }

    /**
     * Simulates a submission of the form specified. The caller can specify values for the form fields, which act as
     * overrides on the values stored inside the elements.
     *
     * @param form       the form to be submitted.
     * @param parameters the query parameter name/value pairs
     * @return The DOM created. Typically you will assert against it.
     */
    public Document submitForm(Element form, Map<String, String> parameters)
    {
        Defense.notNull(form, "form");

        validateElementName(form, "form");

        request.clear().setPath(stripContextFromPath(extractNonBlank(form, "action")));

        pushFieldValuesIntoRequest(form);

        overrideParameters(parameters);

        // addHiddenFormFields(form);

        // ComponentInvocation invocation = getInvocation(form);

        return runComponentEventRequest();
    }

    private void overrideParameters(Map<String, String> fieldValues)
    {
        for (Map.Entry<String, String> e : fieldValues.entrySet())
        {
            request.overrideParameter(e.getKey(), e.getValue());
        }
    }

    private void pushFieldValuesIntoRequest(Element form)
    {
        Visitor visitor = new Visitor()
        {
            public void visit(Element element)
            {
                if (InternalUtils.isNonBlank(element.getAttribute("disabled")))
                    return;

                String name = element.getName();

                if (name.equals("input"))
                {
                    String type = extractNonBlank(element, "type");

                    if (type.equals("radio") || type.equals("checkbox"))
                    {
                        if (InternalUtils.isBlank(element.getAttribute("checked")))
                            return;
                    }

                    // Assume that, if the element is a button/submit, it wasn't clicked,
                    // and therefore, is not part of the submission.

                    if (type.equals("button") || type.equals("submit"))
                        return;

                    // Handle radio, checkbox, text, radio, hidden
                    String value = element.getAttribute("value");

                    if (InternalUtils.isNonBlank(value))
                        request.loadParameter(extractNonBlank(element, "name"), value);

                    return;
                }

                if (name.equals("option"))
                {
                    String value = element.getAttribute("value");

                    // TODO: If value is blank do we use the content, or is the content only the label?

                    if (InternalUtils.isNonBlank(element.getAttribute("selected")))
                    {
                        String selectName = extractNonBlank(findAncestor(element, "select"), "name");

                        request.loadParameter(selectName, value);
                    }

                    return;
                }

                if (name.equals("textarea"))
                {
                    String content = element.getChildMarkup();

                    if (InternalUtils.isNonBlank(content))
                        request.loadParameter(extractNonBlank(element, "name"), content);

                    return;
                }
            }
        };

        form.visit(visitor);
    }

    /**
     * Simulates a submission of the form by clicking the specified submit button. The caller can specify values for the
     * form fields.
     *
     * @param submitButton the submit button to be clicked.
     * @param fieldValues  the field values keyed on field names.
     * @return The DOM created. Typically you will assert against it.
     */
    public Document clickSubmit(Element submitButton, Map<String, String> fieldValues)
    {
        Defense.notNull(submitButton, "submitButton");

        assertIsSubmit(submitButton);

        Element form = getFormAncestor(submitButton);

        request.clear().setPath(stripContextFromPath(extractNonBlank(form, "action")));

        pushFieldValuesIntoRequest(form);

        overrideParameters(fieldValues);

        String value = submitButton.getAttribute("value");

        if (value == null)
            value = DEFAULT_SUBMIT_VALUE_ATTRIBUTE;

        request.overrideParameter(extractNonBlank(submitButton, "name"), value);

        return runComponentEventRequest();
    }

    private void assertIsSubmit(Element element)
    {
        if (element.getName().equals("input"))
        {
            String type = element.getAttribute("type");

            if ("submit".equals(type)) return;
        }

        throw new IllegalArgumentException("The specified element is not a submit button.");
    }

    private Element getFormAncestor(Element element)
    {
        return findAncestor(element, "form");
    }

    private Element findAncestor(Element element, String ancestorName)
    {
        Element e = element;

        while (e != null)
        {
            if (e.getName().equalsIgnoreCase(ancestorName))
                return e;

            e = e.getParent();
        }

        throw new RuntimeException(
                String.format("Could not locate an ancestor element of type '%s'.", ancestorName));

    }

    /**
     * Sets the simulated browser's preferred language, i.e., the value returned from {@link
     * org.apache.tapestry5.services.Request#getLocale()}.
     *
     * @param preferedLanguage preferred language setting
     */
    public void setPreferedLanguage(Locale preferedLanguage)
    {
        request.setLocale(preferedLanguage);
    }
}
