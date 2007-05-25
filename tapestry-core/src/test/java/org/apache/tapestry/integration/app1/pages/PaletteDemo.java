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

package org.apache.tapestry.integration.app1.pages;

import java.util.List;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.SelectModel;
import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.integration.app1.data.ProgrammingLanguage;
import org.apache.tapestry.util.EnumSelectModel;
import org.apache.tapestry.util.EnumValueEncoder;

public class PaletteDemo
{
    @Inject
    private ComponentResources _resources;

    @Persist
    private List<ProgrammingLanguage> _languages;

    public List<ProgrammingLanguage> getLanguages()
    {
        return _languages;
    }

    public void setLanguages(List<ProgrammingLanguage> selected)
    {
        _languages = selected;
    }

    public SelectModel getLanguageModel()
    {
        return new EnumSelectModel(ProgrammingLanguage.class, _resources.getMessages());
    }

    @SuppressWarnings("unchecked")
    public ValueEncoder getLanguageEncoder()
    {
        return new EnumValueEncoder(ProgrammingLanguage.class);
    }
}
