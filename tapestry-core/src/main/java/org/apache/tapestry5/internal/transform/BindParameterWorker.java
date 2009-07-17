// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.services.*;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.annotations.BindParameter;
import org.apache.tapestry5.internal.*;
import org.apache.tapestry5.internal.bindings.LiteralBinding;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.Binding;

import java.util.List;
import java.util.Iterator;
import java.util.Arrays;
import java.lang.reflect.Modifier;

/**
 * Responsible for identifying, via the {@link org.apache.tapestry5.annotations.BindParameter} annotation,
 * mixin fields that should be bound to a core-component parameter value.
 *
 * @since 5.2.0.0
 */
public class BindParameterWorker implements ComponentClassTransformWorker
{

    private static final String EQUAL_METHOD_NAME = BindParameterWorker.class.getName() + ".equal";

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> fieldNames = transformation.findFieldsWithAnnotation(BindParameter.class);

        for(String fieldName : fieldNames)
        {
            BindParameter annotation = transformation.getFieldAnnotation(fieldName, BindParameter.class);
            convertFieldIntoContainerBoundParameter(fieldName, annotation, transformation, model);
        }

    }

    private void convertFieldIntoContainerBoundParameter(String name, BindParameter annotation, ClassTransformation transformation,
                                           MutableComponentModel model)
    {
        transformation.claimField(name, annotation);

        String boundParameterName = getBoundParameterName(name, annotation.name());
        String[] parentParameterNames = getParentParameterNames(name, annotation.value());


        String type = transformation.getFieldType(name);

        //we can't do this exactly the same as parameter. We can't know at transformation time which parameter
        //this thing will be linked to, because it could be wired to any number of different components.
        //So we have to wait until runtime to examine caching and whether we should cache, rather than
        //constructing the class differently based on caching or not.
        String cachedFieldName = transformation.addField(Modifier.PRIVATE, "boolean", name + "_cached");

        String resourcesFieldName = transformation.getResourcesFieldName();

        String accessFieldName = addBoundParameterSetup(name,
                boundParameterName, parentParameterNames,
                cachedFieldName, type, resourcesFieldName,
                transformation);

        addReaderMethod(name, cachedFieldName, accessFieldName, boundParameterName, type, resourcesFieldName,
                        transformation);

        addWriterMethod(name, cachedFieldName, accessFieldName, boundParameterName, type, resourcesFieldName,
                        transformation);
    }


    /**
     * Returns the name of a field that stores whether the parameter binding is invariant.
     */
    private String addBoundParameterSetup(String fieldName, String boundParameterName, String[] parentParameterNames,
                                     String cachedFieldName, String fieldType,
                                     String resourcesFieldName, ClassTransformation transformation)
    {
        String accessFieldName = transformation.addField(Modifier.PRIVATE, ParameterAccess.class.getName(),
                fieldName + "_access");

        String parentNamesField = transformation.addField(Modifier.PRIVATE, String[].class.getName(),
                fieldName + "_parentparameternames");

        String defaultFieldName = transformation.addField(Modifier.PRIVATE, fieldType, fieldName + "_default");

        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("%s = new String[%d];",parentNamesField,parentParameterNames.length);

        for(int i=0;i<parentParameterNames.length;i++)
        {
            builder.addln("%s[%d]=\"%s\";",parentNamesField,i,parentParameterNames[i]);
        }

        builder.addln("%s = %s.getContainerBoundParameterAccess(\"%s\",%s);",
                accessFieldName,
                resourcesFieldName,
                boundParameterName,
                parentNamesField);

        // Store the current value of the field into the default field. This value will
        // be used to reset the field after rendering.

        builder.addln("%s = %s;", defaultFieldName, fieldName);


        addListenerSetup(fieldName, fieldType, boundParameterName, parentParameterNames, accessFieldName,  builder,
                         transformation);

        builder.end();

        transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, builder
                .toString());

        // Now, when the component completes rendering, ensure that any variant parameters are
        // are returned to default value. This isn't necessary when the parameter is not cached,
        // because (unless the binding is invariant), there's no value to get rid of (and if it is
        // invariant, there's no need to get rid of it).
        // as with reader/writer methods, we have to do the caching check at runtime.
        builder.clear();

        builder.addln("if (%s.shouldCache() && ! %1$s.isInvariant())", accessFieldName);
        builder.begin();
        builder.addln("%s = %s;", fieldName, defaultFieldName);
        builder.addln("%s = false;", cachedFieldName);
        builder.end();

        // Clean up after the component renders.

        String body = builder.toString();

        transformation.extendMethod(TransformConstants.POST_RENDER_CLEANUP_SIGNATURE, body);

        // And again, when the page is detached (TAPESTRY-2460)

        transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_DETACH_SIGNATURE, body);

        return accessFieldName;
    }

    private void addListenerSetup(
            String fieldName,
            String fieldType,
            String boundParameterName,
            String[] parentParameterNames,
            String accessFieldName,
            BodyBuilder builder,
            ClassTransformation transformation)
    {
        transformation.addImplementedInterface(ParameterChangeListener.class);
        builder.addln("%s.registerParameterChangeListener($0);",accessFieldName);

        TransformMethodSignature signature = new TransformMethodSignature(Modifier.PUBLIC, "void", "parameterChanged",
                new String[] {ParameterChangedEvent.class.getName()}, null);

        BodyBuilder changedBody = new BodyBuilder().begin();
        //by this point, we know that there is at least one entry in parent Parameter Names.
        changedBody.add("if (%s($1, \"%s\")", EQUAL_METHOD_NAME, parentParameterNames[0]);
        for(int i=1; i<parentParameterNames.length; i++)
        {
            changedBody.add(" || %s($1, \"%s\")", EQUAL_METHOD_NAME, parentParameterNames[i]);
        }
        changedBody.add(")").begin();

        String cast = TransformUtils.getWrapperTypeName(fieldType);

        if (TransformUtils.isPrimitive(fieldType))
            changedBody.addln("%s = ((%s) $1.getNewValue()).%s();",
                    fieldName, cast, TransformUtils.getUnwrapperMethodName(fieldType));
        else
            changedBody.addln("%s = (%s) $1.getNewValue();",fieldName, cast);

        changedBody.addln("return;").end();

        changedBody.end();

        transformation.extendMethod(signature,changedBody.toString());

    }

    private void addWriterMethod(String fieldName, String cachedFieldName, String accessFieldName,
                                 String boundParameterName, String fieldType,
                                 String resourcesFieldName, ClassTransformation transformation)
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

        //unregistering the listener from the parameter change listener list avoids double-setting the field,
        builder.addln("%s.unregisterParameterChangeListener($0);",accessFieldName);

        // Always start by updating the parameter; this will implicitly check for
        // read-only or unbound parameters. $1 is the single parameter
        // to the method.
        builder.addln("%s.write(($w)$1);", accessFieldName);
        builder.addln("%s = $1;",fieldName);
        builder.addln("%s.registerParameterChangeListener($0);",accessFieldName);

        //note that there's no way of knowing at class transformation time which component a mixin will
        //be associated with and, further more, no way of knowing which @Parameter a mixin field will be
        //@BindParameter'ed to.  So we have to generate caching code that works at runtime, rather than
        //including or not including caching logic at transformation time.
        builder.addln("if (%s.shouldCache())",accessFieldName).begin();
        builder.addln("%s = %s.isRendering();",cachedFieldName, resourcesFieldName).end();
        builder.end();

        String methodName = transformation.newMemberName("update_boundparameter", boundParameterName);

        TransformMethodSignature signature = new TransformMethodSignature(Modifier.PRIVATE, "void", methodName,
                new String[] {fieldType}, null);

        transformation.addMethod(signature, builder.toString());

        builder.clear();

        //add the catch because if we don't re-register the class as a parameter change listener, it's value
        //could wind up stale, and write can throw an exception.
        builder.begin();
        builder.addln("%s.registerParameterChangeListener($0);", accessFieldName);
        builder.addln("throw $e;");
        builder.end();

        transformation.addCatch(signature,Exception.class.getName(),builder.toString());

        transformation.replaceWriteAccess(fieldName, methodName);
    }

    /**
     * Adds a private method that will be the replacement for read-access to the field.
     */
    private void addReaderMethod(String fieldName, String cachedFieldName, String accessFieldName,
                                 String boundParameterName, String fieldType, String resourcesFieldName,
                                 ClassTransformation transformation)
    {
        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        // While the component is still loading, or when the value for the component is cached,
        // or if the value is not bound, then return the current value of the field.

        builder.addln("if ((%s.shouldCache() && %s) || ! %s.isLoaded() || ! %s.isBound()) return %s;",
                accessFieldName, cachedFieldName, resourcesFieldName, accessFieldName, fieldName);

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

        builder.add("if (%s.isInvariant() || (%1$s.shouldCache() && %s.isRendering()))",
                accessFieldName, resourcesFieldName);

        builder.begin();
        builder.addln("%s = result;", fieldName);
        builder.addln("%s = true;", cachedFieldName);
        builder.end();

        builder.addln("return result;");
        builder.end();

        String methodName = transformation.newMemberName("read_boundparameter", boundParameterName);

        TransformMethodSignature signature = new TransformMethodSignature(Modifier.PRIVATE, fieldType, methodName, null,
                null);

        transformation.addMethod(signature, builder.toString());

        transformation.replaceReadAccess(fieldName, methodName);
    }

    private String getBoundParameterName(String fieldName, String annotatedName)
    {
        if (InternalUtils.isNonBlank(annotatedName)) return annotatedName;

        return InternalUtils.stripMemberName(fieldName);
    }

    private String[] getParentParameterNames(String fieldName, String... names)
    {
        List<String> temp = CollectionFactory.newList(names);
        for(Iterator<String> it = temp.iterator();it.hasNext(); )
        {
            String name =it.next();
            if (InternalUtils.isBlank(name)) it.remove();
        }
        if (temp.isEmpty())
            return new String[] {InternalUtils.stripMemberName(fieldName)};

        return temp.toArray(new String[temp.size()]);
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

    public static <T> boolean equal(T left, T right)
    {
        return TapestryInternalUtils.isEqual(left,right);
    }

}
