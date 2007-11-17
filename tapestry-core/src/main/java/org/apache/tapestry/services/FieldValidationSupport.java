package org.apache.tapestry.services;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.Translator;
import org.apache.tapestry.ValidationException;

/**
 * Services to help with field {@linkplain org.apache.tapestry.Validator validation} and
 * {@linkplain org.apache.tapestry.Translator translation}. This service encapsulates
 * the logic that mixes normal configured/declared validation/translation with
 * events triggered on the component.
 */
public interface FieldValidationSupport
{
    /**
     * A wrapper around {@link org.apache.tapestry.Translator#toClient(Object)} that first
     * fires a "toclient" event on the component to see if it can perform the conversion.
     *
     * @param value              to be converted to a client-side string
     * @param componentResources used to fire events on the component
     * @param translator         used if the component does not provide a non-null value
     * @return the translated value
     */
    String toClient(Object value, ComponentResources componentResources, Translator translator);

    /**
     * A wrapper around {@link org.apache.tapestry.Translator#parseClient(String, org.apache.tapestry.ioc.Messages)}.
     * First a "parseclient" event is fired; the translator is only invoked if that returns null.
     *
     * @param clientValue        the value provided by the client (may be null)
     * @param componentResources used to trigger events
     * @param translator         translator that will do the work if the component event returns null
     * @return the input parsed to an object
     * @throws org.apache.tapestry.ValidationException
     *          if the value can't be parsed
     */
    Object parseClient(String clientValue, ComponentResources componentResources, Translator translator)
            throws ValidationException;

    /**
     * Performs validation on a parsed value from the client.  Normal validations occur first,
     * then a "validate" event is triggered on the component.
     *
     * @param value              parsed value from the client
     * @param componentResources used to trigger events
     * @param validator          performs normal validations
     * @throws ValidationException if the value is not valid
     */
    void validate(Object value, ComponentResources componentResources, FieldValidator validator)
            throws ValidationException;
}