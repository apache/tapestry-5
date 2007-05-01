// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.test.pagelevel;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry.dom.Document;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.dom.Node;
import org.apache.tapestry.internal.TapestryAppInitializer;
import org.apache.tapestry.internal.services.ActionLinkTarget;
import org.apache.tapestry.internal.services.ComponentInvocation;
import org.apache.tapestry.internal.services.LocalizationSetter;
import org.apache.tapestry.internal.services.PageLinkTarget;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.internal.NullAnnotationProvider;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.util.StrategyRegistry;
import org.apache.tapestry.services.ApplicationGlobals;
import org.apache.tapestry.services.ComponentClassResolver;

/**
 * This class is used to run a Tapestry app in an in-process testing environment. You can ask it to
 * render a certain page and check the DOM object created. You can also ask it to click on a link
 * element in the DOM object to get the next page. Because no servlet container is required, it is
 * very fast and you can directly debug into your code in your IDE.
 */
public class PageTester implements ComponentInvoker
{
    private Registry _registry;

    private ComponentInvocationMapForPageTester _invocationMap = new ComponentInvocationMapForPageTester();

    private FormParameterLookupForPageTester _formParameterLookup = new FormParameterLookupForPageTester();

    private CookiesForPageTester _cookies;

    // For the moment, a page tester instance works in a single session.
    private SessionHolderForPageTester _sessionHolder = new SessionHolderForPageTester();

    private StrategyRegistry<ComponentInvoker> _invokerRegistry;

    private Locale _preferedLanguage;

    private LocalizationSetter _localizationSetter;

    public static final String DEFAULT_CONTEXT_PATH = "src/main/webapp";

    private final String _contextPath;

    /**
     * Initializes a PageTester without overriding any services and assuming that the context root
     * is in src/main/webapp.
     * 
     * @see #PageTester(String, String, String, Map)
     */
    public PageTester(String appPackage, String appName)
    {
        this(appPackage, appName, DEFAULT_CONTEXT_PATH);
    }

    /**
     * @see #PageTester(String, String, String, Map)
     */
    @SuppressWarnings("unchecked")
    public PageTester(String appPackage, String appName, String contextPath)
    {
        this(appPackage, appName, contextPath, Collections.EMPTY_MAP);
    }

    /**
     * Initializes a PageTester that acts as a browser and a servlet container to test drive your
     * Tapestry pages.
     * 
     * @param appPackage
     *            The same value you would specify using the tapestry.app-package context parameter.
     *            As this testing environment is not run in a servlet container, you need to specify
     *            it.
     * @param appName
     *            The same value you would specify as the filter name. It is used to form the name
     *            of the module builder for your app. If you don't have one, pass an empty string.
     * @param contextPath
     *            The path to the context root so that Tapestry can find the templates (if they're
     *            put there).
     * @param serviceOverrides
     *            The mock implementation (value) for some services (key).
     */
    public PageTester(String appPackage, String appName, String contextPath,
            Map<String, Object> serviceOverrides)
    {
        _preferedLanguage = Locale.ENGLISH;
        _contextPath = contextPath;
        _cookies = new CookiesForPageTester();
        _registry = new TapestryAppInitializer(appPackage, appName, "test",
                addDefaultOverrides(serviceOverrides)).getRegistry();

        // This is normally done by the ApplicationInitializer pipeline service.

        _registry.getService(ComponentClassResolver.class).setApplicationPackage(appPackage);

        _localizationSetter = _registry.getService("LocalizationSetter", LocalizationSetter.class);

        ApplicationGlobals globals = _registry.getObject(
                ApplicationGlobals.class,
                new NullAnnotationProvider());

        globals.store(new ContextForPageTester(_contextPath));

        buildInvokersRegistry();
    }

    private void buildInvokersRegistry()
    {
        Map<Class, ComponentInvoker> map = newMap();
        map.put(PageLinkTarget.class, new PageLinkInvoker(_registry));
        map.put(ActionLinkTarget.class, new ActionLinkInvoker(_registry, this, _invocationMap));

        _invokerRegistry = new StrategyRegistry<ComponentInvoker>(ComponentInvoker.class, map);
    }

    private Map<String, Object> addDefaultOverrides(Map<String, Object> serviceOverrides)
    {

        Map<String, Object> modifiedOverrides = newMap(serviceOverrides);
        addDefaultOverride(modifiedOverrides, "ContextPathSource", new FooContextPathSource());
        addDefaultOverride(modifiedOverrides, "URLEncoder", new NoOpURLEncoder());
        addDefaultOverride(modifiedOverrides, "ComponentInvocationMap", _invocationMap);
        addDefaultOverride(modifiedOverrides, "FormParameterLookup", _formParameterLookup);
        addDefaultOverride(modifiedOverrides, "SessionHolder", _sessionHolder);
        addDefaultOverride(modifiedOverrides, "CookieSource", _cookies);
        addDefaultOverride(modifiedOverrides, "CookieSink", _cookies);

        return modifiedOverrides;
    }

    private void addDefaultOverride(Map<String, Object> serviceOverrides, String serviceId,
            Object overridingImpl)
    {
        if (!serviceOverrides.containsKey(serviceId))
        {
            serviceOverrides.put(serviceId, overridingImpl);
        }

    }

    /** You should call it after use */
    public void shutdown()
    {
        if (_registry != null)
        {
            _registry.shutdown();
        }
    }

    /**
     * Renders a page specified by its name.
     * 
     * @param pageName
     *            The name of the page to be rendered.
     * @return The DOM created. Typically you will assert against it.
     */
    public Document renderPage(String pageName)
    {
        return invoke(new ComponentInvocation(new PageLinkTarget(pageName), new String[0], null));
    }

    /**
     * Simulates a click on a link.
     * 
     * @param link
     *            The Link object to be "clicked" on.
     * @return The DOM created. Typically you will assert against it.
     */
    public Document clickLink(Element link)
    {
        Defense.notNull(link, "link");
        ComponentInvocation invocation = getInvocation(link);
        return invoke(invocation);
    }

    private ComponentInvocation getInvocation(Element element)
    {
        ComponentInvocation invocation = _invocationMap.get(element);
        if (invocation == null) { throw new IllegalArgumentException(
                "No component invocation object is associated with the Element"); }
        return invocation;
    }

    public Document invoke(ComponentInvocation invocation)
    {
        // It is critical to clear the map before invoking an invocation (render a page or click a
        // link).
        _invocationMap.clear();
        setThreadLocale();
        ComponentInvoker invoker = _invokerRegistry.getByInstance(invocation.getTarget());
        return invoker.invoke(invocation);
    }

    private void setThreadLocale()
    {
        _localizationSetter.setThreadLocale(_preferedLanguage);
    }

    /**
     * Simulates a submission of the form specified. The caller can specify values for the form
     * fields.
     * 
     * @param form
     *            the form to be submitted.
     * @param fieldValues
     *            the field values keyed on field names.
     * @return The DOM created. Typically you will assert against it.
     */
    public Document submitForm(Element form, Map<String, String> fieldValues)
    {
        Defense.notNull(form, "form");
        _formParameterLookup.clear();
        _formParameterLookup.addFieldValues(fieldValues);
        addHiddenFormFields(form);
        ComponentInvocation invocation = getInvocation(form);
        return invoke(invocation);
    }

    /**
     * Simulates a submission of the form by clicking the specified submit button. The caller can
     * specify values for the form fields.
     * 
     * @param submitButton
     *            the submit button to be clicked.
     * @param fieldValues
     *            the field values keyed on field names.
     * @return The DOM created. Typically you will assert against it.
     */
    public Document clickSubmit(Element submitButton, Map<String, String> fieldValues)
    {
        final String DEFAULT_SUBMIT_VALUE_ATTRIBUTE = "Submit Query";
        Defense.notNull(submitButton, "submitButton");
        assertIsSubmit(submitButton);
        Element form = getFormAncestor(submitButton);
        String value = submitButton.getAttribute("value");
        if (value == null)
        {
            value = DEFAULT_SUBMIT_VALUE_ATTRIBUTE;
        }
        fieldValues.put(submitButton.getAttribute("name"), value);
        return submitForm(form, fieldValues);
    }

    private void assertIsSubmit(Element element)
    {
        if (element.getName().equals("input"))
        {
            String type = element.getAttribute("type");
            if (type != null && type.equals("submit")) { return; }
        }
        throw new IllegalArgumentException("The specified element is not a submit button");
    }

    private Element getFormAncestor(Element element)
    {
        while (true)
        {
            if (element == null) { throw new IllegalArgumentException(
                    "The given element is not contained in a form"); }
            if (element.getName().equalsIgnoreCase("form")) { return element; }
            element = element.getParent();
        }
    }

    private void addHiddenFormFields(Element element)
    {
        if (isHiddenFormField(element))
        {
            _formParameterLookup.addFieldValue(element.getAttribute("name"), element
                    .getAttribute("value"));
        }
        for (Node child : element.getChildren())
        {
            if (child instanceof Element)
            {
                addHiddenFormFields((Element) child);
            }
        }

    }

    private boolean isHiddenFormField(Element element)
    {
        return element.getName().equals("input") && "hidden".equals(element.getAttribute("type"));
    }

    public void setPreferedLanguage(Locale preferedLanguage)
    {
        _preferedLanguage = preferedLanguage;
    }
}
