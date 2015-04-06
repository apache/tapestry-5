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

package org.apache.tapestry5.corelib.data;

import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.corelib.components.Select;

/**
 * Possible values of the "secure" parameter for components that use a
 * {@link SelectModel} (such as {@link Select}), to control whether the submitted
 * value must be one of the values in the SelectModel.
 */
public enum SecureOption
{
    /**
     * Always check that the submitted value is found in the SelectModel (and
     * record a validation error if the SelectModel is not provided (null).
     */
    ALWAYS,

    /**
     * Never check that submitted value is found in the SelectModel. It is left
     * to the user of the component to validate the submitted value.
     */
    NEVER,

    /**
     * The default: check that the submitted value is found in the SelectModel,
     * unless the SelectModel is not provided (null) at the time of submission.
     * Since SelectModels are automatically provided for enums but not custom
     * classes, this is the most useful option in cases where you don't want to
     * persist a custom SelectModel across a form submission or recreate it
     * when the form is submitted). 
     */
    AUTO
}
