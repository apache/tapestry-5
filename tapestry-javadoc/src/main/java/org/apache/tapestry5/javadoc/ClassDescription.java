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

import java.util.Map;

import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;

public class ClassDescription
{
    private final ClassDoc classDoc;

    private final Map<String, ParameterDescription> parameters = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, String> publishedParameters = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, String> events = CollectionFactory.newCaseInsensitiveMap();

    public ClassDescription(ClassDoc classDoc)
    {
        this.classDoc = classDoc;

        loadEvents();
        loadParameters();
    }

    private void loadEvents()
    {
        AnnotationDesc eventsAnnotation = getAnnotation(classDoc, Events.class);

        if (eventsAnnotation == null)
            return;

        // Events has only a single attribute: value(), so we know its the first element
        // in the array.

        ElementValuePair pair = eventsAnnotation.elementValues()[0];

        AnnotationValue annotationValue = pair.value();
        AnnotationValue[] values = (AnnotationValue[]) annotationValue.value();

        for (AnnotationValue eventValue : values)
        {
            String event = (String) eventValue.value();
            int ws = event.indexOf(' ');

            String name = ws < 0 ? event : event.substring(0, ws);
            String description = ws < 0 ? "" : event.substring(ws + 1).trim();

            events.put(name, description);
        }
    }

    private void loadParameters()
    {

    }

    private AnnotationDesc getAnnotation(ProgramElementDoc source, Class annotationType)
    {
        String name = annotationType.getName();

        for (AnnotationDesc ad : source.annotations())
        {
            if (ad.annotationType().qualifiedTypeName().equals(name)) { return ad; }
        }

        return null;
    }

    public String getClassName()
    {
        return classDoc.qualifiedName();
    }

    public Map<String, ParameterDescription> getParameters()
    {
        return parameters;
    }

    public String getSuperClassName()
    {
        return classDoc.superclass().qualifiedName();
    }

    /**
     * Case insensitive map, keyed on parameter name, value is class name of component from which the parameter is
     * published.
     */
    public Map<String, String> getPublishedParameters()
    {
        return publishedParameters;
    }

    /**
     * Case insensitive map, keyed on event name, value is optional description (often blank).
     */
    public Map<String, String> getEvents()
    {
        return events;
    }
}
