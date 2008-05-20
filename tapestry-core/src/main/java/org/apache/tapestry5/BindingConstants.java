package org.apache.tapestry5;

/**
 * Constants for the built-in binding prefixes.  These are often used with the {@link
 * org.apache.tapestry5.annotations.Parameter#defaultPrefix()} annotation attribute.
 */
public class BindingConstants
{
    /**
     * Binding expression prefix used for literal strings.
     */
    public static final String LITERAL = "literal";
    /**
     * Binding expression prefix used to bind to a property of the component. When {@link
     * org.apache.tapestry5.annotations.Parameter#defaultPrefix()} is not specified, the default is PROP.
     */
    public static final String PROP = "prop";

    public static final String NULLFIELDSTRATEGY = "nullfieldstrategy";

    public static final String COMPONENT = "component";

    public static final String MESSAGE = "message";

    public static final String VALIDATE = "validate";

    public static final String TRANSLATE = "translate";

    public static final String BLOCK = "block";

    public static final String ASSET = "asset";

    public static final String VAR = "var";
}
