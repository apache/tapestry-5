// Copyright 2007-2014 The Apache Software Foundation
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
import org.apache.tapestry5.util.EnumValueEncoder;

public class PaletteGroupedDemo
{
    @Inject
    private ComponentResources resources;
    
    @Inject private RequireJsModeHelper requireJsModeHelper;

    @Persist
    @Property
    private List<ProgrammingLanguage> languages;
    
    @Persist
    @Property
    private boolean reorder;

    @Inject
    private TypeCoercer typeCoercer;

    private static final Iterable<ProgrammingLanguage> FUNC = Arrays.asList(
            ProgrammingLanguage.ERLANG, ProgrammingLanguage.HASKELL, ProgrammingLanguage.LISP);
    private static final Iterable<ProgrammingLanguage> OO = Arrays.asList(ProgrammingLanguage.JAVA,
            ProgrammingLanguage.RUBY);
    
    void beginRender()
    {
        requireJsModeHelper.importModule("palette-demo");
    }

    void onPrepareFromDemo()
    {
        if (this.languages == null)
        {
            this.languages = new ArrayList<>();
        }
    }

    public SelectModel getLanguageModel()
    {
        return new AbstractSelectModel()
        {

            @Override
            public List<OptionGroupModel> getOptionGroups()
            {
                List<OptionGroupModel> groups = new ArrayList<>();
                groups.add(new OptionGroupModelImpl("func", false,
                        toOptionModels(FUNC)));
                groups.add(new OptionGroupModelImpl("oo", false, toOptionModels(OO)));
                return groups;
            }

            @Override
            public List<OptionModel> getOptions()
            {
                // TODO Auto-generated method stub
                return null;
            }

        };
    }

    private List<OptionModel> toOptionModels(Iterable<ProgrammingLanguage> languages)
    {
        List<OptionModel> options = new ArrayList<>();
        for (ProgrammingLanguage enumValue : languages)
        {
            options.add(new OptionModelImpl(enumValue.name(), enumValue));
        }
        return options;
    }

    @SuppressWarnings("unchecked")
    public ValueEncoder getLanguageEncoder()
    {
        return new EnumValueEncoder(this.typeCoercer, ProgrammingLanguage.class);
    }
}
