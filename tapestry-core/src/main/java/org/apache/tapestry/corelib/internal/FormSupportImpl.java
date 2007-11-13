// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.corelib.internal;

import org.apache.tapestry.ComponentAction;
import org.apache.tapestry.Field;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.Defense.*;
import org.apache.tapestry.ioc.internal.util.IdAllocator;
import org.apache.tapestry.json.JSONArray;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.FormSupport;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Provides support to components enclosed by a form when the form is rendering (allowing the
 * components to registry form submit callback commands), and also during form submission time.
 * <p/>
 * TODO: Most methods should only be invokable depending on whether the form is rendering or
 * processing a submission.
 */
public class FormSupportImpl implements FormSupport
{
    private final IdAllocator _idAllocator = new IdAllocator();

    private final String _clientId;

    private final ObjectOutputStream _actions;

    private final JSONObject _validations = new JSONObject();


    private List<Runnable> _commands;

    private String _encodingType;

    /**
     * Constructor used when processing a form submission.
     */
    public FormSupportImpl()
    {
        this(null, null);
    }

    /**
     * Constructor used when rendering.
     */
    public FormSupportImpl(String clientId, ObjectOutputStream actions)
    {
        _clientId = clientId;
        _actions = actions;
    }

    public String allocateElementName(String id)
    {
        return _idAllocator.allocateId(id);
    }

    public <T> void store(T component, ComponentAction<T> action)
    {
        Component castComponent = cast(component, Component.class, "component");
        notNull(action, "action");

        String completeId = castComponent.getComponentResources().getCompleteId();

        try
        {
            // Writing the complete id is not very efficient, but the GZip filter
            // should help out there.
            _actions.writeUTF(completeId);
            _actions.writeObject(action);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(InternalMessages.componentActionNotSerializable(completeId, ex), ex);
        }
    }

    public <T> void storeAndExecute(T component, ComponentAction<T> action)
    {
        store(component, action);

        action.execute(component);
    }

    public void defer(Runnable command)
    {
        if (_commands == null) _commands = newList();

        _commands.add(notNull(command, "command"));
    }

    public void executeDeferred()
    {
        if (_commands == null) return;

        for (Runnable r : _commands)
            r.run();

        _commands.clear();
    }

    public String getClientId()
    {
        return _clientId;
    }

    public String getEncodingType()
    {
        return _encodingType;
    }

    public void setEncodingType(String encodingType)
    {
        notBlank(encodingType, "encodingType");

        if (_encodingType != null && !_encodingType.equals(encodingType))
            throw new IllegalStateException(InternalMessages.conflictingEncodingType(_encodingType, encodingType));

        _encodingType = encodingType;
    }

    public void addValidation(Field field, String validationName, String message, Object constraint)
    {
        String fieldId = field.getClientId();

        JSONArray specs = null;

        if (_validations.has(fieldId)) specs = _validations.getJSONArray(fieldId);
        else
        {
            specs = new JSONArray();
            _validations.put(fieldId, specs);
        }

        JSONArray thisSpec = new JSONArray();

        thisSpec.put(validationName);
        thisSpec.put(message);

        if (constraint != null) thisSpec.put(constraint);

        specs.put(thisSpec);
    }

    /**
     * Returns the combined validation data collected via {@link #addValidation(org.apache.tapestry.Field, String, String, Object)}.
     * The keys of this object are the client ids of the {@link org.apache.tapestry.Field}s, the values
     * are an array of validation specifications for that field. Each validation specification is itself
     * an array of two or three values: the validation name (i.e., a method of the client-side Tapestry.Validation
     * object), the message if the field is invalid and, optionally, the constraint value.
     *
     * @return the validation object
     */
    public JSONObject getValidations()
    {
        return _validations;
    }

}