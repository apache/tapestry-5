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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.corelib.data.InsertPosition;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.services.ClientBehaviorSupport;

public class ClientBehaviorSupportImpl implements ClientBehaviorSupport
{
    private void unsupported(String name, String message)
    {
        throw new UnsupportedOperationException(String.format("ClientBehaviorSupport.%s is not longer supported. %s",
                name, message));
    }

    public void addZone(String clientId, String showFunctionName, String updateFunctionName)
    {
        unsupported("addZone", "Use the data-container-type attribute set to 'zone'.");
    }

    public void linkZone(String linkId, String elementId, Link eventLink)
    {
        unsupported("linkZone", "Use the data-update-zone attribute on the triggering element, instead.");
    }

    public void addFormFragment(String clientId, String showFunctionName, String hideFunctionName)
    {
        addFormFragment(clientId, false, showFunctionName, hideFunctionName, null);
    }

    public void addFormFragment(String clientId, boolean alwaysSubmit, String showFunctionName, String hideFunctionName)
    {
        addFormFragment(clientId, false, showFunctionName, hideFunctionName, null);
    }

    public void addFormFragment(String clientId, boolean alwaysSubmit, String showFunctionName, String hideFunctionName, String visibilityBoundFunctionName)
    {
        unsupported("addFormFragment", "Use the core/form-fragment module instead.");
    }

    public void addFormInjector(String clientId, Link link, InsertPosition insertPosition, String showFunctionName)
    {
        unsupported("addFormInjector", "FormInjector compnent was removed in 5.4.");
    }

    public void addValidation(Field field, String validationName, String message, Object constraint)
    {
        unsupported("addFormInjector", "Encode client field validation as attributes and document-level event handlers. See the core/validation module.");
    }
}
