// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.Block;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.integration.app1.data.CarMaker;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.util.EnumSelectModel;
import org.apache.tapestry5.util.EnumValueEncoder;

import java.util.Arrays;
import java.util.List;

public class SelectZoneDemo
{

    @Inject
    private Messages messages;

    @Property
    @Persist
    private CarMaker carMaker;

    @Property
    @Persist
    private String carModel;

    @Inject
    @Property
    private Block modelBlock;

    @Property
    @Persist
    private List<String> availableModels;

    @Inject
    private TypeCoercer typeCoercer;


    public Object onValueChanged(final CarMaker maker)
    {
        availableModels = findAvailableModels(maker);

        return this.modelBlock;
    }

    public List<String> findAvailableModels(final CarMaker maker)
    {
        switch (maker)
        {
            case AUDI:
                return Arrays.asList("A4", "A6", "A8");
            case BMW:
                return Arrays.asList("3 Series", "5 Series", "7 Series");
            case MERCEDES:
                return Arrays.asList("C-Class", "E-Class", "S-Class");
            default:
                return Arrays.asList();
        }
    }

    public SelectModel getMakeModel()
    {
        return new EnumSelectModel(CarMaker.class, this.messages);
    }

    public ValueEncoder<CarMaker> getMakeEncoder()
    {
        return new EnumValueEncoder<CarMaker>(typeCoercer, CarMaker.class);
    }

}
