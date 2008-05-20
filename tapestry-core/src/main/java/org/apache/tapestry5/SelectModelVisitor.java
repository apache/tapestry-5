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

/**
 * Callback interface that allows for visiting the option groups and option models of a select model in correct render
 * order.
 */
public interface SelectModelVisitor
{
    /**
     * Invoked once for each {@link OptionGroupModel}, just before invoking {@link #option(OptionModel)} for each
     * embedded option within the group.
     */
    void beginOptionGroup(OptionGroupModel groupModel);

    /**
     * Invoked for each option within a group, and at the end, for each ungrouped option.
     *
     * @param optionModel
     */
    void option(OptionModel optionModel);

    /**
     * Invoked after all options within the group have been visited.
     */
    void endOptionGroup(OptionGroupModel groupModel);
}
