// Copyright 2014 The Apache Software Foundation
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

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.integration.app1.data.Entity;
import org.apache.tapestry5.integration.app1.data.IncidentData;
import org.apache.tapestry5.internal.OptionGroupModelImpl;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.util.AbstractSelectModel;



public class OptionGroupForm {

    @Property
    @Persist
    private Entity entity;

    private static final List<Entity> entityList = Arrays.asList(new Entity("1", "label1"), new Entity("2", "label2"), new Entity("3", "label3"));

    void onPrepare()
    {
        if (entity == null)
        {
            entity = new Entity();
            entity.setId("1");
            entity.setLabel("label1");
        }
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "form")
    public void onFormSuccess() {
        // It's OK we should come here on form submission
    }

    @OnEvent(value = EventConstants.FAILURE, component = "form")
    public void onFormFailure() throws Exception {
        throw new Exception("The form should have been successfully submitted");
    }

    public SelectModel getModel() {
        return new AbstractSelectModel() {

            private List<OptionGroupModel> groupModels = null;

            public List<OptionModel> getOptions() {
                return null;
            }

            public List<OptionGroupModel> getOptionGroups() {
                if (groupModels == null) {
                    computeModel();
                }
                return groupModels;
            }

            private void computeModel() {
                groupModels = new ArrayList<OptionGroupModel>();
                for (Entity entity : entityList) {
                    List<OptionModel> options = new ArrayList<OptionModel>();
                    options.add(new OptionModelImpl(entity.getLabel(), entity));

                    OptionGroupModel groupModel = new OptionGroupModelImpl(entity.getLabel(), false, options);
                    groupModels.add(groupModel);
                }

            }
        };
    }
}
