// Copyright 2007, 2008 The Apache Software Foundation
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

/**
 * Defines the possible options and option groups for a &lt;select&gt; [X]HTML element.
 * <p/>
 * Primarily used by the {@link org.apache.tapestry5.corelib.components.Select} component, but potentially used by
 * anything similar, that needs to present a list of options to the user. Generally paired with a {@link
 * org.apache.tapestry5.ValueEncoder} to create client-side representations of server-side values.
 *
 * @see org.apache.tapestry5.corelib.components.Palette
 */
public interface SelectModel
{
    /**
     * The list of groups, each containing some number of individual options.
     *
     * @return the groups, or null
     */
    List<OptionGroupModel> getOptionGroups();

    /**
     * The list of ungrouped options, which appear after any grouped options. Generally, a model will either provide
     * option groups or ungrouped options, but not both.
     *
     * @return the ungrouped options, or null
     */
    List<OptionModel> getOptions();

    /**
     * Allows access to all the {@link OptionGroupModel}s and {@link OptionModel}s within the SelectModel.
     */
    void visit(SelectModelVisitor visitor);
}
