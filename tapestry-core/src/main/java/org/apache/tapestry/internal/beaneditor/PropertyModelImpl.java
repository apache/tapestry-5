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

package org.apache.tapestry.internal.beaneditor;

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.internal.TapestryUtils;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.services.ClassFabUtils;

public class PropertyModelImpl implements PropertyModel
{
    private final BeanModel _model;

    private final String _name;

    private final PropertyConduit _conduit;

    private final String _id;

    private String _label;

    private int _order;

    private String _editorType;

    private boolean _sortable;

    public PropertyModelImpl(BeanModel model, final String name, final PropertyConduit conduit,
            Messages messages)
    {
        _model = model;
        _name = name;
        _conduit = conduit;

        _id = TapestryUtils.extractIdFromPropertyExpression(name);
        _order = TapestryUtils.defaultOrder(conduit);

        _label = TapestryUtils.defaultLabel(_id, messages, name);

        // Primitive types need to be converted to wrapper types before checking to see
        // if they are sortable.

        Class wrapperType = ClassFabUtils.getWrapperType(getPropertyType());

        _sortable = Comparable.class.isAssignableFrom(wrapperType);
    }

    public String getId()
    {
        return _id;
    }

    public Class getPropertyType()
    {
        return _conduit == null ? Object.class : _conduit.getPropertyType();
    }

    public PropertyConduit getConduit()
    {
        return _conduit;
    }

    public PropertyModel label(String label)
    {
        notBlank(label, "label");

        _label = label;

        return this;
    }

    public String getLabel()
    {
        return _label;
    }

    public String getPropertyName()
    {
        return _name;
    }

    public int getOrder()
    {
        return _order;
    }

    public PropertyModel order(int order)
    {
        _order = order;

        return this;
    }

    public BeanModel model()
    {
        return _model;
    }

    public PropertyModel editorType(String editorType)
    {
        _editorType = editorType;

        return this;
    }

    public String getEditorType()
    {
        return _editorType;
    }

    public boolean isSortable()
    {
        return _sortable;
    }

    public PropertyModel sortable(boolean sortable)
    {
        _sortable = sortable;

        return this;
    }
}
