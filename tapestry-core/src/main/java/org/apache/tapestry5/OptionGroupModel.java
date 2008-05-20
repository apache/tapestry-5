// Copyright 2007 The Apache Software Foundation
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

import java.util.List;
import java.util.Map;

/**
 * Defines a group of related options. Options may be enabled or disabled as a group. Corresponds to the [X]HTML element
 * &lt;optgroup&gt;.
 */
public interface OptionGroupModel
{
    /**
     * Localized, user-presentable label for the group.
     */
    String getLabel();

    /**
     * If true, the group (and all options within it) will be disabled. Note that some browsers do not honor the
     * disabled attribute property.
     *
     * @return true if a disabled attribute should be rendered.
     */
    boolean isDisabled();

    /**
     * Additional attributes to render with the &lt;optgroup&gt;. This is often used to render the CSS class attribute.
     * May return null.
     */
    Map<String, String> getAttributes();

    /**
     * The list of options within the group.
     */
    List<OptionModel> getOptions();
}
