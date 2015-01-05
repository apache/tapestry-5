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

/**
 * Due to how control names and client ids are allocated inside during an Ajax request, it is difficult to
 * to connect input data and field validation errors to the fields, since the control name and client id are different
 * during the processing of the submitted form data and during the subsequent render. Starting in 5.4, the
 * key used to identify a field inside the {@link org.apache.tapestry5.ValidationTracker is this new validation id,
 * which is assigned on first read.
 * <p/>
 * If a field inplements {@link org.apache.tapestry5.Field} but not Field2, then the control name is used as the
 * validation id (which will work correctly during non-Ajax requests).
 * <p/>
 * This assumes a "flat" field structure, where a given component renders only once (not multiple times, inside
 * a {@link org.apache.tapestry5.corelib.components.Loop}.
 *
 * @since 5.4
 */
public interface Field2 extends Field
{
    /**
     * Returns a request-scoped unique validation id for the field. This returns the same value regardless of how
     * many times the field is rendered, which means that the behavior will be incorrect when the
     * field component is placed inside a loop.
     */
    String getValidationId();
}
