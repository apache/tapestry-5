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
// limitations under the License.package org.apache.tapestry5.internal.services;
package org.apache.tapestry5.internal.services;

import java.util.Collections;
import java.util.Set;

import org.apache.tapestry5.internal.InternalSymbols;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

public class FormControlNameManagerImpl implements FormControlNameManager
{
    
    final private Set<String> names;

    public FormControlNameManagerImpl(
            @Symbol(InternalSymbols.PRE_SELECTED_FORM_NAMES) String preselectedFormNames)
    {
        this.names = Collections.unmodifiableSet(CollectionFactory.<String,String>newSet(TapestryInternalUtils.splitAtCommas(preselectedFormNames)));
    }

    @Override
    public Set<String> getPreselectedNames()
    {
        return names;
    }

    @Override
    public boolean isPreselected(String name)
    {
        return names.contains(name.toLowerCase());
    }

}
