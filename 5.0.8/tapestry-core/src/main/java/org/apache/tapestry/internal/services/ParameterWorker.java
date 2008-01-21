// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Binding;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.bindings.LiteralBinding;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.*;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Responsible for identifying parameters via the {@link org.apache.tapestry.annotations.Parameter}
 * annotation on component fields. This is one of the most complex of the transformations.
 */
public class ParameterWorker implements ComponentClassTransformWorker
{
    private static final String BIND_METHOD_NAME = ParameterWorker.class.getName() + ".bind";

    private final BindingSource _bindingSource;

    public ParameterWorker(BindingSource bindingSource)
    {
        _bindingSource = bindingSource;
    }

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        FieldFilter filter = new FieldFilter()
        {
            public boolean accept(String fieldName, String fieldType)
            {
                Parameter annotation = transformation
                        .getFieldAnnotation(fieldName, Parameter.class);

                return annotation != null && annotation.principal();
            }
        };

        List<String> principleFieldNames = transformation.findFields(filter);

        convertFieldsIntoParameters(transformation, model, principleFieldNames);

        // Now convert the rest.

        List<String> fieldNames = transformation.findFieldsWithAnnotation(Parameter.class);

        convertFieldsIntoParameters(transformation, model, fieldNames);
    }

    private void convertFieldsIntoParameters(ClassTransformation transformation, MutableComponentModel model,
                                             List<String> fieldNames)
    {
        for (String name : fieldNames)
            convertFieldIntoParameter(name, transformation, model);
    }

    private void convertFieldIntoParameter(String name, ClassTransformation transformation, MutableComponentModel model)
    {
        Parameter annotation = transformation.getFieldAnnotation(name, Parameter.class);

        String parameterName = getParameterName(name, annotation.name());

        model.addParameter(parameterName, annotation.required(), annotation.defaultPrefix());

        String type = transformation.getFieldType(name);

        boolean cache = annotation.cache();

        String cachedFieldName = transformation.addField(Modifier.PRIVATE, "boolean", name + "_cached");

        String resourcesFieldName = transformation.getResourcesFieldName();

        String invariantFieldName = addParameterSetup(name, annotation.defaultPrefix(), annotation.value(),
                                                      parameterName, cachedFieldName, cache, type, resourcesFieldName,
                                                      transformation);

        addReaderMethod(name, cachedFieldName, invariantFieldName, cache, parameterName, type, resourcesFieldName,
                        transformation);

        addWriterMethod(name, cachedFieldName, cache, parameterName, type, resourcesFieldName, transformation);

        transformation.claimField(name, annotation);
    }

    /**
     * Returns the name of a field that stores whether the parameter binding is invariant.
     */
    private String addParameterSetup(String fieldName, String defaultPrefix, String defaultBinding,
                                     String parameterName, String cachedFieldName, boolean cache, String fieldType,
                                     String resourcesFieldName, ClassTransformation transformation)
    {
        String defaultFieldName = transformation.addField(Modifier.PRIVATE, fieldType, fieldName + "_default");

        String invariantFieldName = transformation.addField(Modifier.PRIVATE, "boolean", fieldName + "_invariant");

        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        addDefaultBindingSetup(parameterName, defaultPrefix, defaultBinding, resourcesFieldName, transformation,
                               builder);

        builder.addln("%s = %s.isInvariant(\"%s\");", invariantFieldName, resourcesFieldName, parameterName);

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

            builder.addln("if (! %s)", invariantFieldName);
            builder.begin();
            builder.addln("%s = %s;", fieldName, defaultFieldName);
            builder.addln("%s = false;", cachedFieldName);
            builder.end();

            transformation.extendMethod(TransformConstants.POST_RENDER_CLEANUP_SIGNATURE, builder
                    .toString());
        }

        return invariantFieldName;
    }

    private void addDefaultBindingSetup(String parameterName, String defaultPrefix, String defaultBinding,
                                        String resourcesFieldName, ClassTransformation transformation,
                                        BodyBuilder builder)
    {
        if (InternalUtils.isNonBlank(defaultBinding))
        {
            builder.addln("if (! %s.isBound(\"%s\"))", resourcesFieldName, parameterName);

            String bindingFactoryFieldName = transformation.addInjectedField(BindingSource.class, "bindingSource",
                                                                             _bindingSource);

            builder
                    .addln("  %s.bindParameter(\"%s\", %s.newBinding(\"default %2$s\", %1$s, \"%s\", \"%s\"));",
                           resourcesFieldName, parameterName, bindingFactoryFieldName, defaultPrefix, defaultBinding);

            return;

        }

        // If no default binding expression provided in the annotation, then look for a default
        // binding method to provide the binding.

        final String methodName = "default" + InternalUtils.capitalize(parameterName);

        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(TransformMethodSignature signature)
            {
                return signature.getParameterTypes().length == 0 && signature.getMethodName().equals(methodName);
            }
        };

        // This will match exactly 0 or 1 methods, and if it matches, we know the name
        // of the method.

        List<TransformMethodSignature> signatures = transformation.findMethods(filter);

        if (signatures.isEmpty()) return;

        builder.addln("if (! %s.isBound(\"%s\"))", resourcesFieldName, parameterName);
        builder.addln("  %s(\"%s\", %s, %s());", BIND_METHOD_NAME, parameterName, resourcesFieldName, methodName);
    }

    private void addWriterMethod(String fieldName, String cachedFieldName, boolean cache, String parameterName,
                                 String fieldType, String resourcesFieldName, ClassTransformation transformation)
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

        builder.addln("if (%s.isBound(\"%s\"))", resourcesFieldName, parameterName);
        builder.addln("  %s.writeParameter(\"%s\", ($w)$1);", resourcesFieldName, parameterName);

        builder.addln("%s = $1;", fieldName);

        if (cache) builder.addln("%s = %s.isRendering();", cachedFieldName, resourcesFieldName);

        builder.end();

        String methodName = transformation.newMemberName("update_parameter", parameterName);

        TransformMethodSignature signature = new TransformMethodSignature(Modifier.PRIVATE, "void", methodName,
                                                                          new String[]{fieldType}, null);

        transformation.addMethod(signature, builder.toString());

        transformation.replaceWriteAccess(fieldName, methodName);
    }

    /**
     * Adds a private method that will be the replacement for read-access to the field.
     */
    private void addReaderMethod(String fieldName, String cachedFieldName, String invariantFieldName, boolean cache,
                                 String parameterName, String fieldType, String resourcesFieldName,
                                 ClassTransformation transformation)
    {
        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        // While the component is still loading, or when the value for the component is cached,
        // or if the value is not bound, then return the current value of the field.

        builder.addln("if (%s || ! %s.isLoaded() || ! %<s.isBound(\"%s\")) return %s;", cachedFieldName,
                      resourcesFieldName, parameterName, fieldName);

        String cast = TransformUtils.getWrapperTypeName(fieldType);

        // The ($r) cast will convert the result to the method return type; generally
        // this does nothing. but for primitive types, it will unwrap
        // the wrapper type back to a primitive.  We pass the desired type name
        // to readParameter(), since its easier to convert it properly to
        // a type on that end than in the generated code.

        builder.addln("%s result = ($r) ((%s) %s.readParameter(\"%s\", \"%2$s\"));", fieldType, cast,
                      resourcesFieldName, parameterName);

        // If the binding is invariant, then it's ok to cache. Othewise, its only
        // ok to cache if a) the @Parameter says to cache and b) the component
        // is rendering at the point when field is accessed.

        builder.add("if (%s", invariantFieldName);

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

        return InternalUtils.stripMemberPrefix(fieldName);
    }

    public static void bind(String parameterName, InternalComponentResources resources, Object value)
    {
        if (value == null) return;

        if (value instanceof Binding)
        {
            Binding binding = (Binding) value;

            resources.bindParameter(parameterName, binding);
            return;
        }

        resources.bindParameter(parameterName, new LiteralBinding("default " + parameterName, value, null));
    }
}
