package org.apache.tapestry;

/**
 * Constant values for common event names fired by Tapestry components.
 */
public class EventConstants
{
    /**
     * Default client event name, "action", used in most situations.
     */
    public static final String ACTION = "action";
    /**
     * Event triggered when a page is activated (for rendering). The component event handler will be passed the context
     * provided by the passivate event.
     */
    public static final String ACTIVATE = "activate";
    /**
     * Event triggered when a link for a page is generated. The event handler for the page may provide an object, or an
     * array of objects, as the context for the page. These values will become part of the page's context, and will be
     * provided back when the page is activated.
     */
    public static final String PASSIVATE = "passivate";
}
