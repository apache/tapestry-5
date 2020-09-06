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

package org.apache.tapestry5.integration.app1.pages;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.AbstractOptionModel;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.util.AbstractSelectModel;

public class MultiZoneUpdateInsideForm
{
    @Inject
    private Request request;

    @Component(id = "selectValue1", parameters =
    { "model=select1Model", "encoder=select1Model" })
    private Select select1;

    @Property
    private SelectModel select1Model;

    @Property
    private SelectObj selectValue1;

    @Component(id = "selectValue2", parameters =
    { "model=select2Model", "encoder=select2Model" })
    private Select select2;

    @Property
    private SelectModel select2Model;

    @Property
    private SelectObj selectValue2;

    @Component(id = "select1ValueZone")
    private Zone select1ValueZone;

    @Component(id = "select2ValueZone")
    private Zone select2ValueZone;
    
    public Object[] getSelectContext() {
        return new Object[] {13, RetentionPolicy.RUNTIME};
    }

    public class SelectObj
    {
        final int id;
        final String label;

        public SelectObj(int id, String label)
        {
            this.id = id;
            this.label = label;
        }

        public int getId()
        {
            return id;
        }

        public String getLabel()
        {
            return label;
        }
    }

    public class SelectObjModel extends AbstractSelectModel implements ValueEncoder<SelectObj>
    {
        private final List<SelectObj> options;

        public SelectObjModel(List<SelectObj> options)
        {
            this.options = options;
        }

        public List<OptionGroupModel> getOptionGroups()
        {
            return null;
        }

        public List<OptionModel> getOptions()
        {
            assert options != null;
            return F.flow(options).map(new Mapper<SelectObj, OptionModel>()
                        {
                            public OptionModel map(final SelectObj input)
                            {
                                return new AbstractOptionModel()
                                {
                                    public Object getValue()
                                    {
                                        return input;
                                    }
            
                                    public String getLabel()
                                    {
                                        return input.getLabel();
                                    }
                                };
                            }
                        }).toList();
        }

        public String toClient(SelectObj value)
        {
            return String.valueOf(value.getId());
        }

        public SelectObj toValue(String clientValue)
        {
            int id = Integer.parseInt(clientValue);

            for (SelectObj so : options)
            {
                if (so.id == id)
                    return so;
            }

            return null;
        }
    }

    void onActivate(EventContext ctx)
    {
        List<SelectObj> select1List = new ArrayList();
        select1List.add(new SelectObj(0, "0 pre ajax"));
        select1List.add(new SelectObj(1, "1 pre ajax"));
        select1List.add(new SelectObj(2, "2 pre ajax"));
        select1List.add(new SelectObj(3, "3 pre ajax"));
        select1List.add(new SelectObj(4, "4 pre ajax"));
        select1Model = new SelectObjModel(select1List);

        List<SelectObj> select2List = new ArrayList();
        select2List.add(new SelectObj(0, "0 pre ajax"));
        select2List.add(new SelectObj(1, "1 pre ajax"));
        select2List.add(new SelectObj(2, "2 pre ajax"));
        select2List.add(new SelectObj(3, "3 pre ajax"));
        select2Model = new SelectObjModel(select2List);
    }

    @Log
    public Object onValueChangedFromSelectValue1(SelectObj selectObj, Integer integer, RetentionPolicy retentionPolicy)
    {
        final String suffix = String.format(", number %03d, retention policy %s", integer, retentionPolicy);
        List<SelectObj> select2List = new ArrayList();
        select2List.add(new SelectObj(4, "4 post ajax" + suffix));
        select2List.add(new SelectObj(5, "5 post ajax" + suffix));
        select2List.add(new SelectObj(6, "6 post ajax" + suffix));
        select2List.add(new SelectObj(7, "7 post ajax" + suffix));
        select2Model = new SelectObjModel(select2List);

        if (request.isXHR())
        {
            return new MultiZoneUpdate("select1ValueZone", select1ValueZone.getBody()).add("select2ValueZone",
                    select2ValueZone.getBody());
        }
        else
        {
            return this;
        }
    }

}
