// Copyright 2010 The Apache Software Foundation
//
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.BeanEditor;
import org.apache.tapestry5.corelib.components.PropertyEditor;

/**
 * Contribution to {@link BeanBlockSource} identifying a block that is used to edit a property.
 * 
 * @see BeanEditor
 * @see BeanEditForm
 * @see PropertyEditor
 * @see PropertyEditContext
 * @since 5.2.2
 */
public class EditBlockContribution extends BeanBlockContribution
{
    public EditBlockContribution(String dataType, String pageName, String blockId)
    {
        super(dataType, pageName, blockId, true);
    }
}
