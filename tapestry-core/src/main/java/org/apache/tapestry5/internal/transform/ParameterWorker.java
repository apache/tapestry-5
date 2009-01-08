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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.ParameterAccess;
import org.apache.tapestry5.internal.bindings.LiteralBinding;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.*;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

/**
 * Responsible for identifying parameters via the {@link org.apache.tapestry5.annotations.Parameter} annotation on
 * component fields. This is one of the most complex of the transformations.
 */
public class ParameterWorker implements ComponentClassTransformWorker
{
    private static final String BIND_METHOD_NAME = ParameterWorker.class.getName() + ".bind";

    private final BindingSource bindingSource;

    private ComponentDefaultProvider defaultProvider;

    public ParameterWorker(BindingSource bindingSource, ComponentDefaultProvider defaultProvider)
    {
        this.bindingSource = bindingSource;
        this.defaultProvider = defaultProvider;
    }

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> fieldNames = transformation.findFieldsWithAnnotation(Parameter.class);

        for (int pass = 0; pass < 2; pass++)
        {
            Iterator<String> i = fieldNames.iterator();

            while (i.hasNext())
            {
                String fieldName = i.next();

                Parameter annotation = transformation.getFieldAnnotation(fieldName, Parameter.class);

                // Process the principal annotations on the first pass, handle the others
                // on the second pass.

                boolean process = pass == 0
                                  ? annotation.principal()
                                  : true;

                if (process)
                {
                    convertFieldIntoParameter(fieldName, annotation, transformation, model);

                    i.remove();
                }
            }
        }
    }

    private void convertFieldIntoParameter(String name, Parameter annotation, ClassTransformation transformation,
                                           MutableComponentModel model)
    {
        transformation.claimField(name, annotation);

        String parameterName = getParameterName(name, annotation.name());

        model.addParameter(parameterName, annotation.required(), annotation.allowNull(), annotation.defaultPrefix());

        String type = transformation.getFieldType(name);

        boolean cache = annotation.cache();

        String cachedFieldName = transformation.addField(Modifier.PRIVATE, "boolean", name + "_cached");

        String resourcesFieldName = transformation.getResourcesFieldName();

        String accessFieldName = addParameterSetup(name, annotation.defaultPrefix(), annotation.value(),
                                                   parameterName, cachedFieldName, cache, type, resourcesFieldName,
                                                   transformation, annotation.autoconnect());

        addReaderMethod(name, cachedFieldName, accessFieldName, cache, parameterName, type, resourcesFieldName,
                        transformation);

        addWriterMethod(name, cachedFieldName, accessFieldName, cache, parameterName, type, resourcesFieldName,
                        transformation);
    }

    /**
     * Returns the name of a field that stores whether the parameter binding is invariant.
     */
    private String addParameterSetup(String fieldName, String defaultPrefix, String defaultBinding,
                                     String parameterName, String cachedFieldName, boolean cache, String fieldType,
                                     String resourcesFieldName, ClassTransformation transformation, boolean autoconnect)
    {

        String accessFieldName = transformation.addField(Modifier.PRIVATE, ParameterAccess.class.getName(),
                                                         fieldName + "_access");

        String defaultFieldName = transformation.addField(Modifier.PRIVATE, fieldType, fieldName + "_default");

        BodyBuilder builder = new BodyBuilder().begin();

        addDefaultBindingSetup(parameterName, defaultPrefix, defaultBinding, resourcesFieldName,
                               transformation,
                               builder, autoconnect);

        // Order is (alas) important here: must invoke getParameterAccess() after the binding setup, as
        // that code may invoke InternalComponentResources.bindParameter().

        builder.addln("%s = %s.getParameterAccess(\"%s\");", accessFieldName, resourcesFieldName, parameterName);

        // Store the current value of the field into the default field. This value will
        // be used to reset the field after rendering.

        builder.addln("%s = %s;", defaultFieldName, fieldName);
        builder.end();

        transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, builder
                .toString());

        // Now, when the component completes rendering, ensure that any variant parameters are
        // are returned to default value. This isn't necessary when the parameter is not cached,
        // because (unless the binding is invariant), there's no value to get rid of (and if it is
        // invariant, there's no need to get rid of it).

        if (cache)
        {
            builder.clear();

            builder.addln("if (! %s.isInvariant())", accessFieldName);
            builder.begin();
            builder.addln("%s = %s;", fieldName, defaultFieldName);
            builder.addln("%s = false;", cachedFieldName);
            builder.end();

            // Clean up after the component renders.

            String body = builder.toString();

            transformation.extendMethod(TransformConstants.POST_RENDER_CLEANUP_SIGNATURE, body);

            // And again, when the page is detached (TAPESTRY-2460)

            transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_DETACH_SIGNATURE, builder.toString());
        }

        return accessFieldName;
    }

    private void addDefaultBindingSetup(String parameterName, String defaultPrefix, String defaultBinding,
                                        String resourcesFieldName,
                                        ClassTransformation transformation,
                                        BodyBuilder builder, boolean autoconnect)
    {
        if (InternalUtils.isNonBlank(defaultBinding))
        {
            builder.addln("if (! %s.isBound(\"%s\"))", resourcesFieldName, parameterName);

            String bindingFactoryFieldName = transformation.addInjectedField(BindingSource.class, "bindingSource",
                                                                             bindingSource);

            builder
                    .addln("  %s.bindParameter(\"%s\", %s.newBinding(\"default %2$s\", %1$s, \"%s\", \"%s\"));",
                           resourcesFieldName, parameterName, bindingFactoryFieldName, defaultPrefix, defaultBinding);

            return;
        }

        if (autoconnect)
        {
            String defaultProviderFieldName = transformation.addInjectedField(ComponentDefaultProvider.class,
                                                                              "defaultProvider", defaultProvider);

            builder.addln("if (! %s.isBound(\"%s\"))", resourcesFieldName, parameterName);

            builder.addln("  %s.bindParameter(\"%s\", %s.defaultBinding(\"%s\", %s));", resourcesFieldName,
                          parameterName, defaultProviderFieldName, parameterName, resourcesFieldName);
            return;
        }

        // If no default binding expression provided in the annotation, then look for a default
        // binding method to provide the binding.

        final String methodName = "default" + parameterName;

        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(TransformMethodSignature signature)
            {
                return signature.getParameterTypes().length == 0
                        && signature.getMethodName().equalsIgnoreCase(methodName);
            }
        };

        // This will match exactly 0 or 1 methods, and if it matches, we know the name
        // of the method.

        List<TransformMethodSignature> signatures = transformation.findMethods(filter);

        if (signatures.isEmpty()) return;

        // Because the check was case-insensitive, we need to determine the actual
        // name.

        String actualMethodName = signatures.get(0).getMethodName();

        builder.addln("if (! %s.isBound(\"%s\"))", resourcesFieldName, parameterName);
        builder.addln("  %s(\"%s\", %s, ($w) %s());",
                      BIND_METHOD_NAME,
                      parameterName,
                      resourcesFieldName,
                      actualMethodName);
    }

    private void addWriterMethod(String fieldName, String cachedFieldName, String accessFieldName, boolean cache,
                                 String parameterName,
                                 String fieldType, String resourcesFieldName,
                                 ClassTransformation transformation)
    {
        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        // Before the component is loaded, updating the property sets the default value
        // for the parameter. The value is stored in the field, but will be
        // rolled into default field inside containingPageDidLoad().

        builder.addln("if (! %s.isLoaded())", resourcesFieldName);
        builder.begin();
        builder.addln("%s = $1;", fieldName);
        builder.addln("return;");
        builder.end();

        // Always start by updating the parameter; this will implicitly check for
        // read-only or unbound parameters. $1 is the single parameter
        // to the method.

        builder.addln("%s.write(($w)$1);", accessFieldName);

        builder.addln("%s = $1;", fieldName);

        if (cache) builder.addln("%s = %s.isRendering();", cachedFieldName, resourcesFieldName);

        builder.end();

        String methodName = transformation.newMemberName("update_parameter", parameterName);

        TransformMethodSignature signature = new TransformMethodSignature(Modifier.PRIVATE, "void", methodName,
                                                                          new String[] {fieldType}, null);

        transformation.addMethod(signature, builder.toString());

        transformation.replaceWriteAccess(fieldName, methodName);
    }

    /**
     * Adds a private method that will be the replacement for read-access to the field.
     */
    private void addReaderMethod(String fieldName, String cachedFieldName, String accessFieldName, boolean cache,
                                 String parameterName, String fieldType, String resourcesFieldName,
                                 ClassTransformation transformation)
    {
        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        // While the component is still loading, or when the value for the component is cached,
        // or if the value is not bound, then return the current value of the field.

        builder.addln("if (%s || ! %s.isLoaded() || ! %s.isBound()) return %s;", cachedFieldName,
                      resourcesFieldName, accessFieldName, fieldName);

        String cast = TransformUtils.getWrapperTypeName(fieldType);

        // The ($r) cast will convert the result to the method return type; generally
        // this does nothing. but for primitive types, it will unwrap
        // the wrapper type back to a primitive.  We pass the desired type name
        // to readParameter(), since its easier to convert it properly to
        // a type on that end than in the generated code.

        builder.addln("%s result = ($r) ((%s) %s.read(\"%2$s\"));", fieldType, cast, accessFieldName);

        // If the binding is invariant, then it's ok to cache. Othewise, its only
        // ok to cache if a) the @Parameter says to cache and b) the component
        // is rendering at the point when field is accessed.

        builder.add("if (%s.isInvariant()", accessFieldName);

        if (cache) builder.add(" || %s.isRendering()", resourcesFieldName);

        builder.addln(")");
        builder.begin();
        builder.addln("%s = result;", fieldName);
        builder.addln("%s = true;", cachedFieldName);
        builder.end();

        builder.addln("return result;");
        builder.end();

        String methodName = transformation.newMemberName("read_parameter", parameterName);

        TransformMethodSignature signature = new TransformMethodSignature(Modifier.PRIVATE, fieldType, methodName, null,
                                                                          null);

        transformation.addMethod(signature, builder.toString());

        transformation.replaceReadAccess(fieldName, methodName);
    }

    private String getParameterName(String fieldName, String annotatedName)
    {
        if (InternalUtils.isNonBlank(annotatedName)) return annotatedName;

        return InternalUtils.stripMemberName(fieldName);
    }

    /**
     * Invoked from generated code as part of the handling of parameter default methods.
     */
    public static void bind(String parameterName, InternalComponentResources resources, Object value)
    {
        if (value == null) return;

        if (value instanceof Binding)
        {
            Binding binding = (Binding) value;

            resources.bindParameter(parameterName, binding);
            return;
        }

        resources.bindParameter(parameterName, new LiteralBinding(null, "default " + parameterName, value));
    }
}
