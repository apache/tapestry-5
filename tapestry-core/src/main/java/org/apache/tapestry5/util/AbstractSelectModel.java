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

package org.apache.tapestry5.util;

import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.SelectModelVisitor;

import java.util.List;

/**
 * Base class for {@link SelectModel} implementations, whose primary job is to provide the {@link
 * #visit(SelectModelVisitor)} method.
 */
public abstract class AbstractSelectModel implements SelectModel
{
    public final void visit(SelectModelVisitor visitor)
    {
        List<OptionGroupModel> groups = getOptionGroups();

        if (groups != null)
        {
            for (OptionGroupModel groupModel : groups)
            {
                visitor.beginOptionGroup(groupModel);

                visitOptions(groupModel.getOptions(), visitor);

                visitor.endOptionGroup(groupModel);
            }
        }

        visitOptions(getOptions(), visitor);
    }

    private void visitOptions(List<OptionModel> options, SelectModelVisitor vistor)
    {
        if (options != null)
        {
            for (OptionModel optionModel : options)
                vistor.option(optionModel);
        }
    }

}
