// Copyright 2007-2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.integration.app1.data.ProgrammingLanguage;
import org.apache.tapestry5.internal.OptionGroupModelImpl;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.services.ajax.RequireJsModeHelper;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.util.AbstractSelectModel;
import org.apache.tapestry5.util.EnumSelectModel;
import org.apache.tapestry5.util.EnumValueEncoder;

public class PaletteDemo
{
    @Inject
    private ComponentResources resources;

    @Persist
    @Property
    private List<ProgrammingLanguage> languages;

    @Persist
    @Property
    private boolean reorder;

    @Inject
    private TypeCoercer typeCoercer;

    @Inject
    private RequireJsModeHelper requireJsModeHelper;
    
    void beginRender()
    {
        requireJsModeHelper.importModule("palette-demo");
    }

    void onPrepareFromDemo()
    {
        if (languages == null)
        {
            languages = new ArrayList<ProgrammingLanguage>();
        }
    }

    public SelectModel getLanguageModel()
    {
        return new EnumSelectModel(ProgrammingLanguage.class, resources.getMessages());
    }

    @SuppressWarnings("unchecked")
    public ValueEncoder getLanguageEncoder()
    {
        return new EnumValueEncoder(typeCoercer, ProgrammingLanguage.class);
    }

    public SelectModel getGroupedModel()
    {
        return new AbstractSelectModel()
        {

            @Override
            public List<OptionGroupModel> getOptionGroups()
            {
                List<OptionGroupModel> groups = new ArrayList<>();
                groups.add(new OptionGroupModelImpl("group1", false,
                        Arrays.asList(new OptionModelImpl("1"))));
                groups.add(new OptionGroupModelImpl("group2", false,
                        Arrays.asList(new OptionModelImpl("1"))));
                return null;
            }

            @Override
            public List<OptionModel> getOptions()
            {
                // TODO Auto-generated method stub
                return null;
            }

        };
    }
}
