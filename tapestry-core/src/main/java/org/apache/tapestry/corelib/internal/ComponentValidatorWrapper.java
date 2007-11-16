package org.apache.tapestry.corelib.internal;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.runtime.ComponentEventException;

/**
 * Wraps around a component, allowing it to perform input validation using
 * an event.
 */
@SuppressWarnings("unchecked")
public class ComponentValidatorWrapper implements FieldValidator
{
    static final String VALIDATE_EVENT = "validate";

    private final ComponentResources _resources;
    private final FieldValidator _validator;

    public ComponentValidatorWrapper(ComponentResources resources, FieldValidator delegate)
    {
        _resources = resources;
        _validator = delegate;
    }

    /**
     * Invokes a "validate" event on the component, passing the value as context. The
     * delegate {@link org.apache.tapestry.FieldValidator} is invoked <em>first</em>, so
     * the event based validation occurs only if the default validations are passed.
     *
     * @param value
     * @throws ValidationException
     */
    public void validate(Object value) throws ValidationException
    {
        _validator.validate(value);

        try
        {
            _resources.triggerEvent(VALIDATE_EVENT, new Object[]{value}, null);
        }
        catch (ComponentEventException ex)
        {
            ValidationException ve = ex.get(ValidationException.class);

            if (ve != null) throw ve;

            throw ex;
        }
    }

    public void render(MarkupWriter writer)
    {
        _validator.render(writer);
    }
}
