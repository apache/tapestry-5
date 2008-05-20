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

import java.util.Map;

/**
 * A single option within a {@link OptionGroupModel}. Corresponds closely to the [X]HTML &lt;option&gt; element.
 */
public interface OptionModel
{
    /**
     * The localized, user-presentable label for the option.
     */
    String getLabel();

    /**
     * If true, then a disabled attribute will be rendered with the &lt;option&gt;.
     */
    boolean isDisabled();

    /**
     * Additional attributes to render within the &lt;option&gt;. May return null.
     */
    Map<String, String> getAttributes();

    /**
     * The server-side value represented by this option. This is used to determine which option will be selected. It is
     * also used, via {@link ValueEncoder#toClient(Object)}, to generate the client-side value attribute.
     */
    Object getValue();
}
