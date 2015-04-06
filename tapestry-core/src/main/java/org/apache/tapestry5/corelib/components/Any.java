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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Turns any arbitrary (X)HTML element into a component. The element's start and end
 * tags are rendered, including any informal parameters and possibly an id
 * attribute.  The id is provided by {@link JavaScriptSupport#allocateClientId(String)}
 * (so it will be unique on the client side) and is available after the component
 * renders using {@link #getClientId()}. The Any component has no template of its
 * own but does render its body, if any.
 *
 * Some common uses are:
 * <ul>
 * 
 * <li>Applying a mixin to an ordinary HTML element. For example,
 * the following turns an <i>img</i> element into a component that, via the
 * {@link org.apache.tapestry5.corelib.mixins.RenderNotification RenderNotification} mixin, triggers event
 * notifications when it enters the BeginRender and EndRender phases:
 * 
 * <pre>&lt;img t:type="any" t:mixins="renderNotification"&gt;</pre>
 * 
 * And the following renders a <i>td</i> element with the
 * {@link org.apache.tapestry5.corelib.mixins.NotEmpty NotEmpty} mixin to ensure
 * that a non-breaking space (&amp;nbsp;) is rendered if the td element would
 * otherwise be empty:
 * 
 * <pre>&lt;td t:type="any" t:mixins="NotEmpty"&gt;</pre>
 * </li>
 * 
 * <li>Providing a dynamically-generated client ID for an HTML element
 * in a component rendered in a loop or zone (or more than once in a page), for
 * use from JavaScript. (The component class will typically use
 * {@link org.apache.tapestry5.annotations.InjectComponent InjectComponent}
 * to get the component, then call {@link #getClientId()} to retrieve the ID.)
 * 
 * <pre>&lt;table t:type="any" id="clientId"&gt;</pre>
 * 
 * As an alternative to calling getClientId, you can use the
 * {@link org.apache.tapestry5.corelib.mixins.RenderClientId RenderClientId}
 * mixin to force the id attribute to appear in the HTML:
 * 
 * <pre>&lt;table t:type="any" t:mixins="RenderClientId"&gt;</pre>
 * </li>
 * 
 * <li>Dynamically outputting a different HTML element depending on
 * the string value of a property. For example, the following renders an element
 * identified by the "element" property in the corresponding component class:
 * 
 * <pre>&lt;t:any element="prop:element" ... &gt;</pre>
 * </li>
 * 
 * <li>As the base component for a new custom component, especially convenient
 * when the new component should support informal parameters or needs a dynamically
 * generated client ID:
 * 
 * <pre>public class MyComponent extends Any { ... }</pre>
 * </li>
 * </ul>
 * 
 * @tapestrydoc
 */
@SupportsInformalParameters
public class Any implements ClientElement
{
    /**
     * The name of the element to be rendered, typically one of the standard (X)HTML
     * elements, "div", "span", "a", etc., although practically any string will be
     * accepted. The default comes from the template, or is "div" if the template
     * does not specify an element.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String element;

    /**
     * The desired client id, which defaults to the component's id.
     */
    @Parameter(value = "prop:componentResources.id", defaultPrefix = BindingConstants.LITERAL)
    private String clientId;

    private Element anyElement;

    private String uniqueId;

    @Inject
    private ComponentResources resources;

    @Inject
    private JavaScriptSupport javascriptSupport;

    String defaultElement()
    {
        return resources.getElementName("div");
    }

    void beginRender(MarkupWriter writer)
    {
        anyElement = writer.element(element);

        uniqueId = null;

        resources.renderInformalParameters(writer);
    }

    /**
     * Returns the client id. This has side effects: this first time this is called (after the Any component renders
     * its start tag), a unique id is allocated (based on, and typically the same as, the clientId parameter, which
     * defaults to the component's id). The rendered element is updated, with its id attribute set to the unique client
     * id, which is then returned.
     * 
     * @return unique client id for this component
     */
    public String getClientId()
    {
        if (anyElement == null)
            throw new IllegalStateException(String.format(
                    "Unable to provide client id for component %s as it has not yet rendered.", resources
                            .getCompleteId()));

        if (uniqueId == null)
        {
            uniqueId = javascriptSupport.allocateClientId(clientId);
            anyElement.forceAttributes("id", uniqueId);
        }

        return uniqueId;
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end(); // the element
    }

    void inject(JavaScriptSupport javascriptSupport, ComponentResources resources, String element, String clientId)
    {
        this.javascriptSupport = javascriptSupport;
        this.resources = resources;
        this.element = element;
        this.clientId = clientId;
    }
}
