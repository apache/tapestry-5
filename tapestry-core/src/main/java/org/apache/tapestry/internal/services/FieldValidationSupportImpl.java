package org.apache.tapestry.internal.services;

import org.apache.tapestry.*;
import org.apache.tapestry.corelib.internal.InternalMessages;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.ComponentEventException;
import org.apache.tapestry.services.FieldValidationSupport;
import org.apache.tapestry.services.ValidationMessagesSource;

public class FieldValidationSupportImpl implements FieldValidationSupport
{
    static final String PARSE_CLIENT_EVENT = "parseClient";
    static final String TO_CLIENT_EVENT = "toClient";
    static final String VALIDATE_EVENT = "validate";

    private final ValidationMessagesSource _messagesSource;

    public FieldValidationSupportImpl(ValidationMessagesSource messagesSource)
    {
        _messagesSource = messagesSource;
    }

    @SuppressWarnings({"unchecked"})
    public String toClient(Object value, ComponentResources componentResources, Translator translator)
    {
        final Holder<String> resultHolder = Holder.create();

        ComponentEventHandler handler = new ComponentEventHandler()
        {
            public boolean handleResult(Object result, Component component, String methodDescription)
            {
                // What's nice is that the ComponentEventException will automatically identify
                // the method description.

                if (!(result instanceof String))
                    throw new RuntimeException(InternalMessages.toClientShouldReturnString());

                resultHolder.put((String) result);

                return true;
            }
        };

        componentResources.triggerEvent(TO_CLIENT_EVENT, new Object[]{value}, handler);

        if (resultHolder.hasValue()) return resultHolder.get();

        return translator.toClient(value);

    }

    public Object parseClient(String clientValue, ComponentResources componentResources, Translator translator)
            throws ValidationException
    {
        final Holder<Object> resultHolder = Holder.create();

        ComponentEventHandler handler = new ComponentEventHandler()
        {
            public boolean handleResult(Object result, Component component, String methodDescription)
            {
                resultHolder.put(result);
                return true;
            }
        };

        try
        {
            componentResources.triggerEvent(PARSE_CLIENT_EVENT, new Object[]{clientValue}, handler);
        }
        catch (ComponentEventException ex)
        {
            ValidationException ve = ex.get(ValidationException.class);

            if (ve != null) throw ve;

            throw ex;
        }

        if (resultHolder.hasValue()) return resultHolder.get();

        // Otherwise, let the normal translator do the job.

        Messages messages = _messagesSource.getValidationMessages(componentResources.getLocale());

        return translator.parseClient(clientValue, messages);
    }

    public void validate(Object value, ComponentResources componentResources, FieldValidator validator)
            throws ValidationException
    {
        validator.validate(value);

        try
        {
            componentResources.triggerEvent(VALIDATE_EVENT, new Object[]{value}, null);
        }
        catch (ComponentEventException ex)
        {
            ValidationException ve = ex.get(ValidationException.class);

            if (ve != null) throw ve;

            throw ex;
        }
    }
}
