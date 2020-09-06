// Copyright 2009, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.FieldConduit;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Looks for the {@link SessionAttribute} annotation and converts read and write access on such
 * fields into calls to the {@link Session#getAttribute(String)} and {@link Session#setAttribute(String, Object)}.
 */
public class SessionAttributeWorker implements ComponentClassTransformWorker2
{
    private final Request request;

    private class SessionKeyConduit implements FieldConduit<Object>
    {
        private final String key;

        public SessionKeyConduit(String key)
        {
            this.key = key;
        }

        public Object get(Object instance, InstanceContext context)
        {
            Session session = getSession();

            if (session == null)
            {
                return null;
            }

            return session.getAttribute(key);
        }

        public void set(Object instance, InstanceContext context, Object newValue)
        {
            request.getSession(true).setAttribute(key, newValue);
        }

        private Session getSession()
        {
            return request.getSession(false);
        }
    }

    public SessionAttributeWorker(Request request)
    {
        this.request = request;
    }


    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getFieldsWithAnnotation(SessionAttribute.class))
        {
            convertFieldToSessionAccess(field);
        }
    }

    private void convertFieldToSessionAccess(PlasticField field)
    {
        SessionAttribute annotation = field.getAnnotation(SessionAttribute.class);

        field.claim(annotation);

        String key = determineSessionKey(field, annotation.value());

        field.setConduit(new SessionKeyConduit(key));
    }

    private String determineSessionKey(PlasticField field, String value)
    {
        return value.equals("") ? field.getName() : value;
    }
}
