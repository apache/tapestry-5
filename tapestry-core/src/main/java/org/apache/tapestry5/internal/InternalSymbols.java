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

import org.apache.tapestry5.ioc.annotations.IncompatibleChange;

public class InternalSymbols
{
    /**
     * Comma-separated list of pre-allocated Form component control names. Basically, this exists to
     * work around name collisions on the client side. Starting in 5.3, these names are
     * also pre-allocated as ids.
     *
     *
     * @since 5.2.0
     */
    @IncompatibleChange(release = "5.4", details = "Renamed from PRE_SELECTED_FORM_NAMES.")
    public static final String RESERVED_FORM_CONTROL_NAMES = "tapestry.reserved-form-control-names";
}
