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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.integration.app1.data.ProgrammingLanguage;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.util.EnumSelectModel;
import org.apache.tapestry5.util.EnumValueEncoder;

import java.util.List;

public class PaletteDemo
{
    @Inject
    private ComponentResources resources;

    @Persist
    private List<ProgrammingLanguage> languages;

    @Persist
    private boolean reorder;

    public boolean isReorder()
    {
        return reorder;
    }

    public void setReorder(boolean reorder)
    {
        this.reorder = reorder;
    }

    public List<ProgrammingLanguage> getLanguages()
    {
        return languages;
    }

    public void setLanguages(List<ProgrammingLanguage> selected)
    {
        languages = selected;
    }

    public SelectModel getLanguageModel()
    {
        return new EnumSelectModel(ProgrammingLanguage.class, resources.getMessages());
    }

    @SuppressWarnings("unchecked")
    public ValueEncoder getLanguageEncoder()
    {
        return new EnumValueEncoder(ProgrammingLanguage.class);
    }

    void onActionFromReset()
    {
        reorder = false;
        languages = null;
    }
}
