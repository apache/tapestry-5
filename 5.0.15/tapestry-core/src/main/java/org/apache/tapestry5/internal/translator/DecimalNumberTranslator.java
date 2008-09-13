package org.apache.tapestry5.internal.translator;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.services.FormSupport;

/**
 * Base class for {@link org.apache.tapestry5.Translator} instances that represent decimal numbers (numbers that may
 * have a decimal point).
 */
public abstract class DecimalNumberTranslator<T> extends AbstractTranslator<T>
{
    protected DecimalNumberTranslator(String name, Class<T> type)
    {
        super(name, type, "number-format-exception");
    }

    public void render(Field field, String message, MarkupWriter writer, FormSupport formSupport)
    {
        formSupport.addValidation(field, "decimalnumber", message, null);
    }
}
