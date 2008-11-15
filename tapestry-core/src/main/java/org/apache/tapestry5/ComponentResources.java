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

package org.apache.tapestry5;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleListener;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Provides a component instance with the resources provided by the framework. In many circumstances, the resources
 * object can be considered the component itself; in others, it is the {@linkplain #getComponent() component property},
 * an instance of a class provided by the application developer (though transformed in many ways while being loaded)
 * that is the true component. In reality, it is the combination of the resources object with the user class instance
 * that forms the components; neither is useful without the other.
 */
public interface ComponentResources extends ComponentResourcesCommon
{
    /**
     * Returns the base resource for the component, which will represent the class's location within the classpath (this
     * is used to resolve relative assets).
     */
    Resource getBaseResource();

    /**
     * Returns the component model object that defines the behavior of the component.
     */
    ComponentModel getComponentModel();

    /**
     * Returns the component this object provides resources for.
     */
    Component getComponent();

    /**
     * Returns the component which contains this component, or null for the root component. For mixins, this returns the
     * componet to which the mixin is attached.
     */
    Component getContainer();

    /**
     * Returns the {@link ComponentResources} for the container, or null if the this is the root component (that has no
     * container). As a special case, for a mixin, this returns the core component's resources.
     */
    ComponentResources getContainerResources();

    /**
     * Returns the {@link Messages} from the container, or null if this is the root component (with no container). As a
     * special case, for a mixin, this return the core component's messages.
     */
    Messages getContainerMessages();

    /**
     * Returns the page that contains this component. Technically, the page itself is an internal object in Tapestry and
     * this returns the root component of the actual page, but from an application developer point of view, this is the
     * page.
     */
    Component getPage();

    /**
     * Returns an embedded component, given the component's id.
     *
     * @param embeddedId selects the embedded component (case is ignored)
     * @throws IllegalArgumentException if this component does not contain a component with the given id
     */

    Component getEmbeddedComponent(String embeddedId);

    /**
     * Returns true if the named parameter is bound, false if not.
     */
    boolean isBound(String parameterName);

    /**
     * Obtains an annotation provided by a parameter.
     *
     * @param parameterName  name of parameter to search for the annotation
     * @param annotationType the type of annotation
     * @return the annotation if found or null otherwise
     */
    <T extends Annotation> T getParameterAnnotation(String parameterName, Class<T> annotationType);

    /**
     * Indentifies all parameters that are not formal parameters and writes each as a attribute/value pair into the
     * current element of the markup writer.
     *
     * @param writer to which {@link MarkupWriter#attributes(Object[]) attributes} will be written
     */
    void renderInformalParameters(MarkupWriter writer);

    /**
     * Returns the message catalog for this component.
     */
    Messages getMessages();

    /**
     * Returns the actual type of the bound parameter, or null if the parameter is not bound. This is primarily used
     * with property bindings, and is used to determine the actual type of the property, rather than the type of
     * parameter (remember that type coercion automatically occurs, which can mask significant differences between the
     * parameter type and the bound property type).
     *
     * @param parameterName used to select the parameter (case is ignored)
     * @return the type of the bound parameter, or null if the parameter is not bound
     * @see Binding#getBindingType()
     */
    Class getBoundType(String parameterName);

    /**
     * Returns an annotation provider, used to obtain annotations related to the parameter.
     *
     * @param parameterName used to select the parameter (case is ignored)
     * @return the annotation provider, or null if the parameter is not bound
     */
    AnnotationProvider getAnnotationProvider(String parameterName);

    /**
     * Used to access an informal parameter that's a Block.
     *
     * @param parameterName the name of the informal parameter (case is ignored)
     * @return the informal Block parameter, or null if not bound
     */
    Block getBlockParameter(String parameterName);

    /**
     * Returns a previously stored render variable.
     *
     * @param name of the variable (case will be ignored)
     * @return the variable's value
     * @throws IllegalArgumentException if the name doesn't correspond to a stored value
     */
    Object getRenderVariable(String name);

    /**
     * Stores a render variable, accessible with the provided name.
     *
     * @param name  of value to store
     * @param value value to store (may not be null)
     * @throws IllegalStateException if the component is not currently rendering
     */
    void storeRenderVariable(String name, Object value);

    /**
     * Adds a listener object that will be notified about page lifecycle events.
     */
    void addPageLifecycleListener(PageLifecycleListener listener);


    /**
     * Discards all persistent field changes for the page containing the component.  Changes are eliminated from
     * persistent storage (such as the {@link org.apache.tapestry5.services.Session}) which will take effect in the
     * <em>next</em> request (the attached page instance is not affected).
     */
    void discardPersistentFieldChanges();

    /**
     * Returns the name of element that represents the component in its template, or null if not known.
     *
     * @return the element name or null
     */
    String getElementName();

    /**
     * Returns a list of the names of any informal parameters bound to this component.
     *
     * @return the name sorted alphabetically
     * @see org.apache.tapestry5.annotations.SupportsInformalParameters
     */
    List<String> getInformalParameterNames();

    /**
     * Reads an informal parameter and {@linkplain org.apache.tapestry5.ioc.services.TypeCoercer coercers) the bound
     * value to the indicated type.
     *
     * @param name name of informal parameter
     * @param type output value type
     * @return instance of type
     */
    <T> T getInformalParameter(String name, Class<T> type);
}
