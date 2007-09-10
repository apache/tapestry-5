package org.apache.tapestry.beaneditor;

/**
 * Controls the position of newly added {@link PropertyModel}s inside a {@link BeanModel}.
 */
public enum RelativePosition
{
    /** The new {@link PropertyModel} goes before the existing model. */
    BEFORE,

    /** The new {@link PropertyModel} goes after the existing model. */
    AFTER
}
