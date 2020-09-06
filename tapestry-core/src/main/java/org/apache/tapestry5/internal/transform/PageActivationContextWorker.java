// Copyright 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.FieldHandle;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.ComponentEventHandler;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Provides the page activation context handlers.
 *
 * @see org.apache.tapestry5.annotations.PageActivationContext
 */
public class PageActivationContextWorker implements ComponentClassTransformWorker2
{
    private static final Comparator<PlasticField> INDEX_COMPARATOR = new Comparator<PlasticField>()
    {
        public int compare(PlasticField field1, PlasticField field2) {
            int index1 = field1.getAnnotation(PageActivationContext.class).index();
            int index2 = field2.getAnnotation(PageActivationContext.class).index();

            int compare = index1 < index2 ? -1 : (index1 > index2 ? 1 : 0);
            if (compare == 0)
            {
                compare = field1.getName().compareTo(field2.getName());
            }
            return compare;
        }
    };

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        List<PlasticField> fields = plasticClass.getFieldsWithAnnotation(PageActivationContext.class);

        if (!fields.isEmpty())
        {
            transformFields(support, fields);
        }
    }

    private void transformFields(TransformationSupport support, List<PlasticField> fields)
    {
        List<PlasticField> sortedFields = CollectionFactory.newList(fields);
        Collections.sort(sortedFields, INDEX_COMPARATOR);
        validateSortedFields(sortedFields);

        PlasticField firstField = sortedFields.get(0);
        PageActivationContext firstAnnotation = firstField.getAnnotation(PageActivationContext.class);

        // these arrays reduce memory usage and allow the PlasticField instances to be garbage collected
        FieldHandle[] handles = new FieldHandle[sortedFields.size()];
        String[] typeNames = new String[sortedFields.size()];

        int i = 0;
        for (PlasticField field : sortedFields) {
            handles[i] = field.getHandle();
            typeNames[i] = field.getTypeName();
            ++i;
        }

        if (firstAnnotation.activate())
        {
            support.addEventHandler(EventConstants.ACTIVATE, 1,
                    "PageActivationContextWorker activate event handler", createActivationHandler(handles, typeNames));
        }

        if (firstAnnotation.passivate())
        {
            support.addEventHandler(EventConstants.PASSIVATE, 0,
                    "PageActivationContextWorker passivate event handler", createPassivateHandler(handles));
        }

        // We don't claim the field, and other workers may even replace it with a FieldConduit.
    }

    private void validateSortedFields(List<PlasticField> sortedFields) {
        List<Integer> expectedIndexes = CollectionFactory.newList();
        List<Integer> actualIndexes = CollectionFactory.newList();
        Set<Boolean> activates = CollectionFactory.newSet();
        Set<Boolean> passivates = CollectionFactory.newSet();

        for (int i = 0; i < sortedFields.size(); ++i) {
            PlasticField field = sortedFields.get(i);
            PageActivationContext annotation = field.getAnnotation(PageActivationContext.class);
            expectedIndexes.add(i);
            actualIndexes.add(annotation.index());
            activates.add(annotation.activate());
            passivates.add(annotation.passivate());
        }

        List<String> errors = CollectionFactory.newList(); 
        if (!expectedIndexes.equals(actualIndexes)) {
            errors.add(String.format("Index values must start at 0 and increment by 1 (expected [%s], found [%s])", 
                    InternalUtils.join(expectedIndexes), InternalUtils.join(actualIndexes)));
        }
        if (activates.size() > 1) {
            errors.add("Illegal values for 'activate' (all fields must have the same value)");
        }
        if (passivates.size() > 1) {
            errors.add("Illegal values for 'passivate' (all fields must have the same value)");
        }
        if (!errors.isEmpty()) {
            throw new RuntimeException(String.format("Invalid values for @PageActivationContext: %s", InternalUtils.join(errors)));
        }
    }

    private static ComponentEventHandler createActivationHandler(final FieldHandle[] handles, final String[] fieldTypes)
    {
        return new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                int count = Math.min(handles.length, event.getEventContext().getCount());
                for (int i = 0; i < count; ++i)
                {
                    String fieldType = fieldTypes[i];
                    FieldHandle handle = handles[i];
                    Object value = event.coerceContext(i, fieldType);
                    handle.set(instance, value);
                }
            }
        };
    }

    private static ComponentEventHandler createPassivateHandler(final FieldHandle[] handles)
    {
        return new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                Object result;
                if (handles.length == 1) {
                    // simple / common case for a single @PageActivationContext
                    result = handles[0].get(instance);
                } else {
                    LinkedList<Object> list = CollectionFactory.newLinkedList();

                    // iterate backwards
                    for (int i = handles.length - 1; i > -1; i--) {
                        FieldHandle handle = handles[i];
                        Object value = handle.get(instance);

                        // ignore trailing nulls
                        if (value != null || !list.isEmpty()) {
                            list.addFirst(value);
                        }
                    }
                    result = list.isEmpty() ? null : list;
                }

                event.storeResult(result);
            }
        };
    }
}