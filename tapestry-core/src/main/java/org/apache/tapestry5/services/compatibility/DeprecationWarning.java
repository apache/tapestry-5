// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.services.compatibility;

import org.apache.tapestry5.ComponentResources;

/**
 * Used to report deprecation warnings for components.
 *
 * @since 5.4
 */
public interface DeprecationWarning
{

    /**
     * Used to identify a component parameter that has been deprecated. Typically, such parameters are simply
     * ignored, but the message should be specific about this.
     *
     * @param resources
     *         identifies the component, including its location
     * @param parameterName
     *         name of the deprecated parameter
     * @param message
     *         message to display; typically explains what action will be taken, such as simply ignoring the parameter entirely. This should clarify
     *         the issue to the developer, guiding them towards resolving the deprecation, typically be eliminating
     *         the parameter entirely.
     */
    void componentParameter(ComponentResources resources, String parameterName, String message);

    /**
     * The most common case: checks to see if the parameter name is bound and if so emits a warning that the parameter
     * is ignored and will be removed.
     *
     * @param resources
     *         identifies the component, including its location
     * @param parameterNames
     *         names of the deprecated parameters
     */
    void ignoredComponentParameters(ComponentResources resources, String... parameterNames);

    /**
     * Used to identify a specific parameter value that is no longer supported. The first time this combination of
     * component, parameter name, and parameter value are provided, an error is logged against this service's
     * logger, including the component type and complete id, name of parameter (but not the value), the message, and the location of
     * the component.
     *
     * @param resources
     *         identifies the component, including its location.
     * @param parameterName
     *         name of parameter containing illegal value.
     * @param parameterValue
     *         value that is not supported (typically, an enum type). May be null.
     * @param message
     *         message to display; typically explains that action will be taken, such as treating the value as an alternate value,
     *         or simply ignoring the value. This should clarify the issue to the developer, guiding them towards resolving
     *         the deprecation, by changing the value, or eliminating the use of the parameter entirely.
     */
    void componentParameterValue(ComponentResources resources, String parameterName, Object parameterValue, String message);
}
