package org.apache.tapestry5.integration.app1;

import java.util.List;

import org.apache.tapestry5.AbstractOptionModel;
import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.util.AbstractSelectModel;

public  class SelectObjModel extends AbstractSelectModel implements ValueEncoder<SelectObj>
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