// Copyright 2008, 2009, 2010 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.MixinClasses;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.TransformField;

public class TransformMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(TransformMessages.class);

    public static String fieldInjectionError(String className, String fieldName, Throwable cause)
    {
        return MESSAGES.format("field-injection-error", className, fieldName, cause);
    }

    public static String illegalNumberOfPageActivationContextHandlers(List<TransformField> fields)
    {
        List<String> names = CollectionFactory.newList();

        for (TransformField field : fields)
        {
            names.add(field.getName());
        }

        return MESSAGES.format("illegal-number-of-page-activation-context-handlers", InternalUtils.joinSorted(names));
    }

        public static String illegalNumberOfPageActivationContextHandlers2(List<PlasticField> fields)
    {
        List<String> names = CollectionFactory.newList();

        for (PlasticField field : fields)
        {
            names.add(field.getName());
        }

        return MESSAGES.format("illegal-number-of-page-activation-context-handlers", InternalUtils.joinSorted(names));
    }

    public static String badMixinConstraintLength(MixinClasses mixin, String fieldName)
    {
        return MESSAGES.format("bad-mixin-constraint-length", mixin.value().length, fieldName, mixin.order().length);
    }

    /** @since 5.2.0 */
    public static String bindParameterOnlyOnMixin(String boundParameterName, ComponentResources resources)
    {
        return MESSAGES.format("bind-parameter-only-on-mixin", boundParameterName, resources.getComponentModel()
                .getComponentClassName());
    }

}
