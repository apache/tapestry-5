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

package org.apache.tapestry5.corelib.internal;

import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.internal.services.ClientBehaviorSupport;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.services.FormSupport;

import java.util.List;

/**
 * Provides support to components enclosed by a form when the form is rendering (allowing the components to registry
 * form submit callback commands), and also during form submission time.
 * <p/>
 * TODO: Most methods should only be invokable depending on whether the form is rendering or processing a submission.
 */
public class FormSupportImpl implements FormSupport
{
    private final ClientBehaviorSupport clientBehaviorSupport;

    private final boolean clientValidationEnabled;

    private final IdAllocator idAllocator;

    private final String clientId;

    private final ComponentActionSink actionSink;

    private List<Runnable> commands;

    private String encodingType;

    /**
     * Constructor used when processing a form submission.
     */
    public FormSupportImpl()
    {
        this(null, null, null, false, null);
    }

    /**
     * Constructor used when rendering.
     */
    public FormSupportImpl(String clientId, ComponentActionSink actionSink, ClientBehaviorSupport clientBehaviorSupport,
                           boolean clientValidationEnabled)
    {
        this(clientId, actionSink, clientBehaviorSupport, clientValidationEnabled, new IdAllocator());
    }

    /**
     * Full constructor.
     */
    public FormSupportImpl(String clientId, ComponentActionSink actionSink, ClientBehaviorSupport clientBehaviorSupport,
                           boolean clientValidationEnabled, IdAllocator idAllocator)
    {
        this.clientId = clientId;
        this.actionSink = actionSink;
        this.clientBehaviorSupport = clientBehaviorSupport;
        this.clientValidationEnabled = clientValidationEnabled;
        this.idAllocator = idAllocator;
    }

    public String getFormId()
    {
        return clientId;
    }

    public String allocateControlName(String id)
    {
        return idAllocator.allocateId(id);
    }

    public <T> void store(T component, ComponentAction<T> action)
    {
        actionSink.store(component, action);
    }

    public <T> void storeAndExecute(T component, ComponentAction<T> action)
    {
        actionSink.store(component, action);

        action.execute(component);
    }

    public void defer(Runnable command)
    {
        if (commands == null) commands = CollectionFactory.newList();

        commands.add(Defense.notNull(command, "command"));
    }

    public void executeDeferred()
    {
        if (commands == null) return;

        for (Runnable r : commands)
            r.run();

        commands.clear();
    }

    public String getClientId()
    {
        return clientId;
    }

    public String getEncodingType()
    {
        return encodingType;
    }

    public void setEncodingType(String encodingType)
    {
        Defense.notBlank(encodingType, "encodingType");

        if (this.encodingType != null && !this.encodingType.equals(encodingType))
            throw new IllegalStateException(InternalMessages.conflictingEncodingType(this.encodingType, encodingType));

        this.encodingType = encodingType;
    }

    public void addValidation(Field field, String validationName, String message, Object constraint)
    {
        if (clientValidationEnabled)
            clientBehaviorSupport.addValidation(field, validationName, message, constraint);
    }


}
