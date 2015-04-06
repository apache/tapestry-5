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

package org.apache.tapestry5.beanvalidator;

import org.apache.tapestry5.MarkupWriter;

import java.util.Map;
import java.util.Set;

/**
 * Applies client-side validation constraints based on a particular JSR 303 annotation.
 *
 * Note: converted from a final class to an interface as part of 5.4.
 */
public interface ClientConstraintDescriptor
{
    /**
     * The annotation class that drives this descriptor.
     */
    Class getAnnotationClass();

    /**
     * Names of attributes from the {@link javax.validation.metadata.ConstraintDescriptor} that are relevant.
     */
    Set<String> getAttributes();

    /**
     * Applies the validation
     *
     * @param writer
     *         used to write new attributes into the HTML tag for the user interface element
     * @param message
     *         error message to present to user when the constraint is violated
     * @param attributes
     *         {@linkplain #getAttributes()} selected attributes} from the {@link javax.validation.metadata.ConstraintDescriptor}
     */
    void applyClientValidation(MarkupWriter writer, String message, Map<String, Object> attributes);
}
