// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.bindings.PropBinding;
import org.apache.tapestry5.internal.services.PersistentFieldManager;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.structure.PageResetListener;
import org.apache.tapestry5.internal.transform.ParameterConduit;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;

/**
 * An extension of {@link org.apache.tapestry5.ComponentResources} that represents additional
 * methods that are private
 * to the framework and not exposed in any public APIs.
 */
public interface InternalComponentResources extends ComponentResources,
        InternalComponentResourcesCommon, RenderCommand
{
    /**
     * Get the current persisted value of the field.
     * 
     * @param fieldName
     *            the name of the field to access
     * @return the value stored for the field, or null if no value is currently stored
     */
    Object getFieldChange(String fieldName);

    /**
     * Checks to see if there is a value stored for the indicated field.
     */
    boolean hasFieldChange(String fieldName);

    /**
     * Posts a change to a persistent field. If the component is still loading, then this change is
     * ignored. Otherwise,
     * it is propagated, via the
     * {@link Page#persistFieldChange(org.apache.tapestry5.ComponentResources, String, Object)
     * page} to the {@link PersistentFieldManager}.
     */
    void persistFieldChange(String fieldName, Object newValue);

    /**
     * Allows the resources to cleanup any render-time only data.
     */
    void postRenderCleanup();


    /**
     * Delegates to {@link Page#addResetListener(org.apache.tapestry5.internal.structure.PageResetListener)}.
     * 
     * @param listener
     *            to register
     */
    void addPageResetListener(PageResetListener listener);

    /**
     * Gets a previously stored ParameterConduit, allowing PCs to be shared between a component
     * and a mixin of that component.
     * 
     * @since 5.2.0
     */
    ParameterConduit getParameterConduit(String parameterName);

    /**
     * Stores a ParameterConduit for later access. Tthis occurs inside a component's
     * {@link PageLifecycleListener#containingPageDidLoad()} lifecycle
     * method.
     * 
     * @since 5.2.0
     */
    void setParameterConduit(String parameterName, ParameterConduit conduit);
    
    
    /**
     * Returns the name of the bound property if {@link PropBinding} is used and the expression points to a property on a bean (e.g. user.name).
     * Otherwise this method returns null.
     * 
     * @param parameterName name of the parameter
     * 
     * @since 5.2.0
     */
    String getPropertyName(String parameterName);
}
