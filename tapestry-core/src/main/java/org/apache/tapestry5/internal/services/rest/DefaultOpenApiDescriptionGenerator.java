// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.tapestry5.internal.services.rest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.ActivationContextParameter;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.RequestBody;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.RestInfo;
import org.apache.tapestry5.annotations.StaticActivationContextValue;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.util.CommonsUtils;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.PageSource;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;
import org.apache.tapestry5.services.rest.MappedEntityManager;
import org.apache.tapestry5.services.rest.OpenApiDescriptionGenerator;
import org.apache.tapestry5.services.rest.OpenApiTypeDescriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@linkplain OpenApiDescriptionGenerator} that generates lots, if not most, of the application's 
 * OpenAPI 3.0 documentation.
 * 
 * @since 5.8.0
 */
public class DefaultOpenApiDescriptionGenerator implements OpenApiDescriptionGenerator 
{
    
    final private static Logger LOGGER = LoggerFactory.getLogger(DefaultOpenApiDescriptionGenerator.class);
    
    final private OpenApiTypeDescriber typeDescriber;
    
    final private BaseURLSource baseUrlSource;
    
    final private SymbolSource symbolSource;
    
    final private ComponentMessagesSource componentMessagesSource;
    
    final private ThreadLocale threadLocale;
    
    final private PageSource pageSource;
    
    final private ThreadLocal<Messages> messages;
    
    final private ComponentClassResolver componentClassResolver;
    
    final private PageRenderLinkSource pageRenderLinkSource;
    
    final private Request request;
    
    final Set<Class<?>> entities;
    
    final private static String KEY_PREFIX = "openapi.";
    
    final private String basePath;
    
    private final Map<String, Class<?>> stringToClassMap = new HashMap<>();
    
    public DefaultOpenApiDescriptionGenerator(
            final OpenApiTypeDescriber typeDescriber,
            final MappedEntityManager mappedEntityManager,
            final BaseURLSource baseUrlSource, 
            final SymbolSource symbolSource, 
            final ComponentMessagesSource componentMessagesSource,
            final ThreadLocale threadLocale,
            final PageSource pageSource,
            final ComponentClassResolver componentClassResolver,
            final PageRenderLinkSource pageRenderLinkSource,
            final Request request) 
    {
        super();
        
        this.typeDescriber = typeDescriber;
        this.baseUrlSource = baseUrlSource;
        this.symbolSource = symbolSource;
        this.componentMessagesSource = componentMessagesSource;
        this.threadLocale = threadLocale;
        this.pageSource = pageSource;
        this.componentClassResolver = componentClassResolver;
        this.pageRenderLinkSource = pageRenderLinkSource;
        this.request = request;
        entities = mappedEntityManager.getEntities();
        
        messages = new ThreadLocal<>();
        basePath = symbolSource.valueForSymbol(SymbolConstants.OPENAPI_BASE_PATH);
        
        if (!basePath.startsWith("/") || !basePath.endsWith("/"))
        {
            throw new RuntimeException(String.format(
                    "The value of the %s (%s) configuration symbol is '%s' is invalid. "
                    + "It should start with a slash and not end with one", 
                        SymbolConstants.OPENAPI_BASE_PATH, 
                        "SymbolConstants.OPENAPI_BASE_PATH", basePath));
        }
        
        stringToClassMap.put("boolean", boolean.class);
        stringToClassMap.put("byte", byte.class);
        stringToClassMap.put("short", short.class);
        stringToClassMap.put("int", int.class);
        stringToClassMap.put("long", long.class);
        stringToClassMap.put("float", float.class);
        stringToClassMap.put("double", double.class);
        stringToClassMap.put("char", char.class);
        
        for (Class<?> entity : entities) {
            stringToClassMap.put(entity.getName(), entity);
        }
        
    }

    @Override
    public JSONObject generate(JSONObject documentation) 
    {

        // Making sure all pages have been loaded and transformed
        for (String pageName : componentClassResolver.getPageNames())
        {
            try
            {
                pageSource.getPage(pageName);
            }
            catch (Exception e)
            {
                // Ignoring exception, since some classes may not
                // be instantiable.
                LOGGER.warn(String.format(
                        "Exception while intantiating page %s for OpenAPI description generation,", 
                        pageName), e);
                e.printStackTrace();
            }
        }

        messages.set(componentMessagesSource.getApplicationCatalog(threadLocale.getLocale()));

        if (documentation == null)
        {
            documentation = new JSONObject();
        }
        
        documentation.put("openapi", symbolSource.valueForSymbol(SymbolConstants.OPENAPI_VERSION));
        
        generateInfo(documentation);
        
        JSONArray servers = new JSONArray();
        servers.add(new JSONObject("url", baseUrlSource.getBaseURL(request.isSecure()) + 
                basePath.substring(0, basePath.length() - 1))); // removing the last slash
        
        documentation.put("servers", servers);
        
        try
        {
            addPaths(documentation);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        
        generateSchemas(documentation);
        
        return documentation;
        
    }

    private void generateInfo(JSONObject documentation) {
        JSONObject info = new JSONObject();
        putIfNotEmpty(info, "title", SymbolConstants.OPENAPI_TITLE);
        putIfNotEmpty(info, "description", SymbolConstants.OPENAPI_DESCRIPTION);
        info.put("version", getValueFromSymbolNoPrefix(SymbolConstants.OPENAPI_APPLICATION_VERSION).orElse("?"));
        documentation.put("info", info);
    }
    
    private void addPaths(JSONObject documentation) throws NoSuchMethodException, SecurityException 
    {
        
        List<Page> pagesWithRestEndpoints = pageSource.getAllPages().stream()
                .filter(DefaultOpenApiDescriptionGenerator::hasRestEndpoint)
                .collect(Collectors.toList());
        
        JSONObject paths = new JSONObject();
        JSONArray tags = new JSONArray();
        
        for (Page page : pagesWithRestEndpoints) 
        {
            processPageClass(page, paths, tags);
        }
        
        documentation.put("tags", tags);
        documentation.put("paths", paths);
        
    }

    private void processPageClass(Page page, JSONObject paths, JSONArray tags) throws NoSuchMethodException {
        final Class<?> pageClass = page.getRootComponent().getClass();

        final String tagName = addPageTag(tags, pageClass);
        
        ComponentModel model = page.getRootComponent().getComponentResources().getComponentModel();
        
        JSONArray methodsAsJson = getMethodsAsJson(model);
        
        List<Method> methods = toMethods(methodsAsJson, pageClass);
        
        for (Method method : methods) 
        {
            processMethod(method, pageClass, paths, tagName);
        }
    }

    private String addPageTag(JSONArray tags, final Class<?> pageClass) 
    {
        final String tagName = getValue(pageClass, "tag.name").orElse(pageClass.getSimpleName());
        JSONObject tag = new JSONObject();
        tag.put("name", tagName);
        putIfNotEmpty(tag, "description", getValue(pageClass, "tag.description"));
        tags.add(tag);
        return tagName;
    }

    private JSONArray getMethodsAsJson(ComponentModel model) 
    {
        JSONArray methodsAsJson = new JSONArray();
        while (model != null)
        {
            final String meta = model.getMeta(
                    InternalConstants.REST_ENDPOINT_EVENT_HANDLER_METHODS);
            if (meta != null)
            {
                JSONArray thisMethodArray = new JSONArray(meta);
                addElementsIfNotPresent(methodsAsJson, thisMethodArray);
            }
            model = model.getParentModel();
        }
        return methodsAsJson;
    }

    private void processMethod(Method method, final Class<?> pageClass, JSONObject paths, final String tagName) 
    {
        final String uri = getPath(method, pageClass);
        final JSONObject path;
        if (paths.containsKey(uri))
        {
            path = paths.getJSONObject(uri);
        }
        else
        {
            path = new JSONObject();
            paths.put(uri, path);
        }
        
        final String httpMethod = getHttpMethod(method);
        
        if (path.containsKey(httpMethod))
        {
            throw new RuntimeException(String.format(
                    "There are at least two different REST endpoints for path %s and HTTP method %s in class %s",
                    uri, httpMethod.toUpperCase(), pageClass.getName()));
        }
        else
        {
            
            final JSONObject methodDescription = new JSONObject();
            
            putIfNotEmpty(methodDescription, "summary", getValue(method, uri, httpMethod, "summary"));
            putIfNotEmpty(methodDescription, "description", getValue(method, uri, httpMethod, "description"));
            
            JSONArray methodTags = new JSONArray();
            methodTags.add(tagName);
            methodDescription.put("tags", methodTags);
            
            processResponses(method, uri, httpMethod, methodDescription);

            processParameters(method, uri, httpMethod, methodDescription);
            
            path.put(httpMethod, methodDescription);
        }
    }

    private void processParameters(Method method, final String uri, final String httpMethod, final JSONObject methodDescription) {
        JSONArray parametersAsJsonArray = new JSONArray();
        for (Parameter parameter : method.getParameters())
        {
            final JSONObject parameterDescription = new JSONObject();
            if (!isIgnored(parameter) && 
                    !parameter.isAnnotationPresent(StaticActivationContextValue.class))
            {
                parameterDescription.put("in", "path");
            }
            else if (parameter.isAnnotationPresent(RequestParameter.class))
            {
                parameterDescription.put("in", "query");
            }
            else if (parameter.isAnnotationPresent(RequestBody.class))
            {
                processRequestBody(method, uri, httpMethod, methodDescription, parametersAsJsonArray, parameter);
            }
            if (!parameterDescription.isEmpty())
            {
//                Optional<String> parameterName = getValue(method, uri, httpMethod, parameter, "name");
//                parameterDescription.put("name", parameterName.orElse(parameter.getName()));
                parameterDescription.put("name", getParameterName(parameter));
                getValue(method, uri, httpMethod, parameter, "description")
                    .ifPresent((v) -> parameterDescription.put("description", v));
                typeDescriber.describe(parameterDescription, parameter);
                
                parametersAsJsonArray.add(parameterDescription);
            }
        }
        
        if (!parametersAsJsonArray.isEmpty())
        {
            methodDescription.put("parameters", parametersAsJsonArray);
        }
    }

    private void processRequestBody(Method method,
            final String uri,
            final String httpMethod,
            final JSONObject methodDescription,
            JSONArray parametersAsJsonArray,
            Parameter parameter) {
        JSONObject requestBodyDescription = new JSONObject();
        requestBodyDescription.put("required", 
                !(parameter.getAnnotation(RequestBody.class).allowEmpty()));
        getValue(method, uri, httpMethod, "requestbody.description")
            .ifPresent((v) -> requestBodyDescription.put("description", v));
        
        RestInfo restInfo = method.getAnnotation(RestInfo.class);
        if (restInfo != null)
        {
            JSONObject contentDescription = new JSONObject();
            for (String contentType : restInfo.consumes()) 
            {
                JSONObject schemaDescription = new JSONObject();
                typeDescriber.describe(schemaDescription, parameter);
                schemaDescription.remove("required");
                contentDescription.put(contentType, schemaDescription);
            }
            requestBodyDescription.put("content", contentDescription);
        }
        methodDescription.put("requestBody", requestBodyDescription);
    }

    private String getParameterName(Parameter parameter) {
        String name = null;
        final RequestParameter requestParameter = parameter.getAnnotation(RequestParameter.class);
        if (requestParameter != null && !CommonsUtils.isBlank(requestParameter.value()))
        {
            name = requestParameter.value();
        }
        ActivationContextParameter activationContextParameter = parameter.getAnnotation(ActivationContextParameter.class);
        if (activationContextParameter != null && !CommonsUtils.isBlank(activationContextParameter.value()))
        {
            name = activationContextParameter.value();
        }
        if (CommonsUtils.isBlank(name))
        {
            name = parameter.getName();
        }
        return name;
    }

    private void processResponses(Method method, final String uri, final String httpMethod, final JSONObject methodDescription) {
        JSONObject responses = new JSONObject();
        JSONObject defaultResponse = new JSONObject();
        int statusCode = httpMethod.equals("post") || httpMethod.equals("put") ? 
                HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK;
        putIfNotEmpty(defaultResponse, "description", getValue(method, uri, httpMethod, statusCode));
        responses.put(String.valueOf(statusCode), defaultResponse);

        String[] produces = getProducedMediaTypes(method);
        if (produces != null && produces.length > 0)
        {
            JSONObject contentDescription = new JSONObject();
            for (String mediaType : produces)
            {
                JSONObject responseTypeDescription = new JSONObject();
                typeDescriber.describeReturnType(responseTypeDescription, method);
                contentDescription.put(mediaType, responseTypeDescription);
            }
            defaultResponse.put("content", contentDescription);
        }

        methodDescription.put("responses", responses);
    }
    
    private String[] getProducedMediaTypes(Method method) {
        
        String[] produces = CommonsUtils.EMPTY_STRING_ARRAY;
        RestInfo restInfo = method.getAnnotation(RestInfo.class);
        if (isNonEmptyConsumes(restInfo))
        {
            produces = restInfo.produces();
        }
        else
        {
            restInfo = method.getDeclaringClass().getAnnotation(RestInfo.class);
            if (isNonEmptyProduces(restInfo))
            {
                produces = restInfo.produces();
            }
        }
        
        return produces;
    }

    private void addElementsIfNotPresent(JSONArray accumulator, JSONArray array) 
    {
        if (array != null)
        {
            for (int i = 0; i < array.size(); i++)
            {
                JSONObject method = array.getJSONObject(i);
                boolean present = isPresent(accumulator, method);
                if (!present)
                {
                    accumulator.add(method);
                }
            }
        }
    }
    
    private boolean isNonEmptyConsumes(RestInfo restInfo)
    {
        return restInfo != null && !(restInfo.produces().length == 1 && "".equals(restInfo.produces()[0]));
    }
    
    private boolean isNonEmptyProduces(RestInfo restInfo)
    {
        return restInfo != null && !(restInfo.produces().length == 1 && "".equals(restInfo.produces()[0]));
    }

    private boolean isPresent(JSONArray array, JSONObject object) 
    {
        boolean present = false;
        for (int i = 0; i < array.size(); i++)
        {
            if (object.equals(array.getJSONObject(i)))
            {
                present = false;
            }
        }
        return present;
    }

    private Optional<String> getValue(Class<?> clazz, String property) 
    {
        Optional<String> value = getValue(
                KEY_PREFIX + clazz.getName() + "." + property);
        if (!value.isPresent())
        {
            value = getValue(
                    KEY_PREFIX + clazz.getSimpleName() + "." + property);
        }
        return value;
    }

    private Optional<String> getValue(Method method, String path, String httpMethod, String property) 
    {
        return getValue(method, path + "." + httpMethod + "." + property, false);
    }

    public Optional<String> getValue(Method method, String path, String httpMethod, Parameter parameter, String property) 
    {
        return getValue(method, path, httpMethod, "parameter." + getParameterName(parameter), property);
    }
    
    public Optional<String> getValue(Method method, String path, String httpMethod, int statusCode) 
    {
        return getValue(method, path, httpMethod, "response", String.valueOf(statusCode));
    }

    public Optional<String> getValue(Method method, String path, String httpMethod, String middle, String propertyName) 
    {
        Optional<String> value = getValue(method, path + "." + httpMethod + "." + middle + "." + String.valueOf(propertyName), true);
        if (!value.isPresent())
        {
            value = getValue(method, httpMethod + "." + middle + "." + propertyName, false);
        }
        if (!value.isPresent())
        {
            value = getValue(method, middle + "." + propertyName, false);
        }
        if (!value.isPresent())
        {
            value = getValue(middle + "." + propertyName);
        }
        return value;
    }

    public Optional<String> getValue(Method method, final String suffix, final boolean skipClassNameLookup) 
    {
        Optional<String> value = Optional.empty();

        if (!skipClassNameLookup)
        {
            value = getValue(
                    KEY_PREFIX + method.getDeclaringClass().getName() + "." + suffix);
            if (!value.isPresent())
            {
                value = getValue(
                        KEY_PREFIX + method.getDeclaringClass().getSimpleName() + "." + suffix);
            }
        }
        if (!value.isPresent())
        {
            value = getValue(KEY_PREFIX + suffix);
        }
        return value;
    }

    private List<Method> toMethods(JSONArray methodsAsJson, Class<?> pageClass) throws NoSuchMethodException, SecurityException 
    {
        List<Method> methods = new ArrayList<>(methodsAsJson.size());
        for (Object object : methodsAsJson)
        {
            JSONObject methodAsJason = (JSONObject) object;
            final String name = methodAsJason.getString("name");
            final JSONArray parametersAsJson = methodAsJason.getJSONArray("parameters");
            @SuppressWarnings("rawtypes")
            List<Class> parameterTypes = parametersAsJson.stream()
                .map(o -> ((String) o))
                .map(s -> toClass(s))
                .collect(Collectors.toList());
            methods.add(findMethod(pageClass, name, parameterTypes));
        }
        return methods;
    }

    @SuppressWarnings("rawtypes")
    public Method findMethod(Class<?> pageClass, final String name, List<Class> parameterTypes) throws NoSuchMethodException 
    {
        Method method = null;
        try
        {
            method = pageClass.getDeclaredMethod(name, 
                    parameterTypes.toArray(new Class[parameterTypes.size()]));
        }
        catch (NoSuchMethodException e)
        {
            // Let's try the supertypes
            List<Class> superTypes = new ArrayList<>();
            superTypes.add(pageClass.getSuperclass());
            superTypes.addAll((Arrays.asList(pageClass.getInterfaces())));
            for (Class clazz : superTypes)
            {
                if (clazz != null && !clazz.equals(Object.class))
                {
                    method = findMethod(clazz, name, parameterTypes);
                    if (method != null)
                    {
                        break;
                    }
                }
            }
        }
        if (method == null && pageClass.getName().equals("org.apache.tapestry5.integration.app1.pages.rest.RestTypeDescriptionsDemo"))
        {
            System.out.println("WTF!");
        }
        // In case of the same class being loaded from different classloaders,
        // let's try to find the method in a different way.
//        if (method == null)
//        {
//            for (Method m : pageClass.getDeclaredMethods())
//            {
//                if (name.equals(m.getName()) && parameterTypes.size() == m.getParameterCount())
//                {
//                    boolean matches = true;
//                    for (int i = 0; i < parameterTypes.size(); i++)
//                    {
//                        if (!(parameterTypes.get(i)).getName().equals(
//                                m.getParameterTypes()[i].getName()))
//                        {
//                            matches = false;
//                            break;
//                        }
//                    }
//                    if (matches)
//                    {
//                        method = m;
//                        break;
//                    }
//                }
//            }
//        }
        return method;
    }
    
    private Class<?> toClass(String string)
    {
        Class<?> clasz = stringToClassMap.get(string);
        if (clasz == null)
        {
            try 
            {
                clasz = Thread.currentThread().getContextClassLoader().loadClass(string);
            } catch (ClassNotFoundException e) 
            {
                throw new RuntimeException(e);
            }
            stringToClassMap.put(string, clasz);
        }
        return clasz;
    }

    private String getPath(Method method, Class<?> pageClass)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(pageRenderLinkSource.createPageRenderLink(pageClass).toString());
        for (Parameter parameter : method.getParameters())
        {
            if (!isIgnored(parameter))
            {
                builder.append("/");
                final StaticActivationContextValue staticValue = parameter.getAnnotation(StaticActivationContextValue.class);
                if (staticValue != null)
                {
                    builder.append(staticValue.value());
                }
                else
                {
                    builder.append("{");
                    builder.append(getParameterName(parameter));
                    builder.append("}");
                }
            }
        }
        String path = builder.toString();
        if (!path.startsWith(basePath))
        {
            throw new RuntimeException(String.format("Method %s has path %s, which "
                    + "doesn't start with base path %s. It's likely you need to adjust the "
                    + "base path and/or the endpoint paths",
                    method, path, basePath));
        }
        else
        {
            path = path.substring(basePath.length() - 1); // keep the slash
            path = path.replace("//", "/"); // remove possible double slashes
        }
        return path;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean isIgnored(Parameter parameter)
    {
        boolean ignored = false;
        for (Class clazz : InternalConstants.INJECTED_PARAMETERS)
        {
            if (parameter.getAnnotation(clazz) != null)
            {
                ignored = true;
                break;
            }
        }
        return ignored;
    }

    private void putIfNotEmpty(JSONObject object, String propertyName, Optional<String> value)
    {
        value.ifPresent((v) -> object.put(propertyName, v));
    }
    
    private void putIfNotEmpty(JSONObject object, String propertyName, String key)
    {
        getValue(key).ifPresent((value) -> object.put(propertyName, value));
    }
    
    private Optional<String> getValue(String key)
    {
        Optional<String> value = getValueFromMessages(key);
        return value.isPresent() ? value : getValueFromSymbol(key);
    }

    private Optional<String> getValueFromMessages(String key)
    {
        logMessageLookup(key);
        final String value = messages.get().get(key.replace("tapestry.", "")).trim();
        return value.startsWith("[") && value.endsWith("]") ? Optional.empty() : Optional.of(value);
    }

    private void logSymbolLookup(String key) {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Looking up symbol  " + key);
        }
    }
    
    private void logMessageLookup(String key) {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Looking up message " + key);
        }
    }
    
    private Optional<String> getValueFromSymbol(String key)
    {
        return getValueFromSymbolNoPrefix("tapestry." + key);
    }

    private Optional<String> getValueFromSymbolNoPrefix(final String symbol) {
        String value;
        logSymbolLookup(symbol);
        try
        {
            value = symbolSource.valueForSymbol(symbol);
        }
        catch (RuntimeException e)
        {
            // value not found;
            value = null;
        }
        return Optional.ofNullable(value);
    }
    
    private static final String PREFIX = InternalConstants.HTTP_METHOD_EVENT_PREFIX.toLowerCase();
    
    private static String getHttpMethod(Method method)
    {
        String httpMethod;
        OnEvent onEvent = method.getAnnotation(OnEvent.class);
        if (onEvent != null)
        {
            httpMethod = onEvent.value();
        }
        else
        {
            httpMethod = method.getName().replace("on", "");
        }
        httpMethod = httpMethod.toLowerCase();
        httpMethod = httpMethod.replace(PREFIX, "");
        return httpMethod;
    }

    private static boolean hasRestEndpoint(Page page) 
    {
        return hasRestEndpoint(page.getRootComponent());
    }

    private static boolean hasRestEndpoint(final Component component) 
    {
        final ComponentModel componentModel = component.getComponentResources().getComponentModel();
        return InternalConstants.TRUE.equals(componentModel.getMeta(
                InternalConstants.REST_ENDPOINT_EVENT_HANDLER_METHOD_PRESENT));
    }
    
    private void generateSchemas(JSONObject documentation) 
    {
        if (!entities.isEmpty())
        {
        
            JSONObject components = new JSONObject();
            JSONObject schemas = new JSONObject();
        
            for (Class<?> entity : entities) {
                typeDescriber.describeSchema(entity, schemas);
            }
            
            components.put("schemas", schemas);
            documentation.put("components", components);
            
        }
        
    }

}
