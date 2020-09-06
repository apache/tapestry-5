// Copyright 2007, 2009, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.javadoc;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.util.SimpleDocTreeVisitor;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import javax.lang.model.util.SimpleTypeVisitor9;
import java.util.List;
import java.util.Map;

public class ClassDescription
{
    public final DocletEnvironment env;

    public final TypeElement classDoc;

    public final Map<String, ParameterDescription> parameters = CollectionFactory.newCaseInsensitiveMap();

    /**
     * Case insensitive map, keyed on event name, value is optional description (often blank).
     */
    public final Map<String, String> events = CollectionFactory.newCaseInsensitiveMap();

    public ClassDescription(DocletEnvironment env)
    {
        this.env = env;
        this.classDoc = null;
    }

    public ClassDescription(TypeElement classDoc, ClassDescriptionSource source, DocletEnvironment env)
    {
        this.classDoc = classDoc;
        this.env = env;

        loadEvents();
        loadParameters(source);

        TypeMirror parentDoc = classDoc.getSuperclass();

        if (parentDoc != null
                && !StringUtils.equals(Object.class.getName(), classDoc.toString()))
        {
            String className = parentDoc.accept(new SimpleTypeVisitor9<String, Object>()
            {
                @Override
                public String visitDeclared(DeclaredType t, Object o)
                {
                    return t.asElement().asType().toString();
                }
            }, null);

            ClassDescription parentDescription = source.getDescription(className);

            mergeInto(events, parentDescription.events);
            mergeInto(parameters, parentDescription.parameters);
        }
    }

    private void loadEvents()
    {
        AnnotationMirror eventsAnnotation = getAnnotation(classDoc, Events.class);

        if (eventsAnnotation == null)
            return;

        // Events has only a single attribute: value(), so we know its the first element
        // in the array.

        AnnotationValue annotationValue = eventsAnnotation.getElementValues().values().iterator().next();

        annotationValue.accept(new SimpleAnnotationValueVisitor9<Void, Void>()
        {
            @Override
            public Void visitArray(List<? extends AnnotationValue> values, Void aVoid)
            {
                for (AnnotationValue eventValue : values)
                {
                    String event = (String) eventValue.getValue();
                    int ws = event.indexOf(' ');

                    String name = ws < 0 ? event : event.substring(0, ws);
                    String description = ws < 0 ? "" : event.substring(ws + 1).trim();

                    events.put(name, description);
                }
                return null;
            }
        }, null);
    }

    private static <K, V> void mergeInto(Map<K, V> target, Map<K, V> source)
    {
        for (K key : source.keySet())
        {
            if (!target.containsKey(key))
            {
                V value = source.get(key);
                target.put(key, value);
            }
        }
    }

    private void loadParameters(ClassDescriptionSource source)
    {
        for (VariableElement fd : ElementFilter.fieldsIn(classDoc.getEnclosedElements()))
        {
            if (fd.getModifiers().contains(Modifier.STATIC))
                continue;

            if (!fd.getModifiers().contains(Modifier.PRIVATE))
                continue;

            Map<String, String> values = getAnnotationValues(fd, Parameter.class);

            if (values != null)
            {
                String name = values.get("name");

                if (name == null)
                    name = fd.getSimpleName().toString().replaceAll("^[$_]*", "");

                ParameterDescription pd = new ParameterDescription(
                        fd,
                        name,
                        fd.asType().toString(),
                        get(values, "value", ""),
                        get(values, "defaultPrefix", BindingConstants.PROP),
                        getBoolean(values, "required", false),
                        getBoolean(values, "allowNull", true),
                        getBoolean(values, "cache", true),
                        getSinceTagValue(fd),
                        env.getElementUtils().isDeprecated(fd),
                        e -> env.getDocTrees().getDocCommentTree(e));

                parameters.put(name, pd);

                continue;
            }

            values = getAnnotationValues(fd, Component.class);

            if (values != null)
            {
                String names = get(values, "publishParameters", "");

                if (InternalUtils.isBlank(names))
                    continue;

                for (String name : names.split("\\s*,\\s*"))
                {
                    ParameterDescription pd = getPublishedParameterDescription(source, fd, name);
                    parameters.put(name, pd);
                }

            }

        }
    }

    private ParameterDescription getPublishedParameterDescription(
            ClassDescriptionSource source, VariableElement fd, String name)
    {
        String currentClassName = fd.asType().toString();

        while (true)
        {
            ClassDescription componentCD = source.getDescription(currentClassName);

            if (componentCD.classDoc == null)
                //  TODO FQN for fd
                throw new IllegalArgumentException(
                        String.format("Published parameter '%s' from %s not found.", name, fd.getSimpleName()));

            if (componentCD.parameters.containsKey(name)) { return componentCD.parameters.get(name); }

            currentClassName = componentCD.classDoc.getSuperclass().toString();
        }
    }

    private String getSinceTagValue(Element doc)
    {
        final DocCommentTree tree = env.getDocTrees().getDocCommentTree(doc);

        if (tree == null)
        {
            return "";
        }

        for (DocTree tag : tree.getBlockTags())
        {
            return tag.accept(new SimpleDocTreeVisitor<String, Void>("")
            {
                @Override
                public String visitSince(SinceTree node, Void aVoid)
                {
                    return node.getBody().toString();
                }
            }, null);
        }

        return "";
    }

    private static boolean getBoolean(Map<String, String> map, String key, boolean defaultValue)
    {
        if (map.containsKey(key))
            return Boolean.parseBoolean(map.get(key));

        return defaultValue;
    }

    private static String get(Map<String, String> map, String key, String defaultValue)
    {
        if (map.containsKey(key))
            return map.get(key);

        return defaultValue;
    }

    private static AnnotationMirror getAnnotation(Element source, Class annotationType)
    {
        String name = annotationType.getName();

        for (AnnotationMirror ad : source.getAnnotationMirrors())
        {
            if (ad.getAnnotationType().toString().equals(name)) { return ad; }
        }

        return null;
    }

    private static Map<String, String> getAnnotationValues(Element source, Class annotationType)
    {
        AnnotationMirror annotation = getAnnotation(source, annotationType);

        if (annotation == null)
            return null;

        Map<String, String> result = CollectionFactory.newMap();

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> pair : annotation.getElementValues().entrySet())
        {
            result.put(pair.getKey().getSimpleName().toString(), pair.getValue().getValue().toString());
        }

        return result;
    }
}
