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

import org.apache.tapestry.beaneditor.BeanEditorModel;
import org.apache.tapestry.beaneditor.PropertyConduit;
import org.apache.tapestry.beaneditor.PropertyEditModel;
import org.apache.tapestry.ioc.internal.util.Defense;

public class PropertyEditModelImpl implements PropertyEditModel
{
    private final BeanEditorModel _model;

    private final String _name;

    private String _label;

    private PropertyConduit _conduit;

    private int _order;

    private Class _propertyType = Object.class;

    private String _editorType;

    public PropertyEditModelImpl(BeanEditorModel model, final String name)
    {
        _model = model;
        _name = name;
    }

    public Class getPropertyType()
    {
        return _propertyType;
    }

    public PropertyEditModel propertyType(Class propertyType)
    {
        Defense.notNull(propertyType, "propertyType");

        _propertyType = propertyType;

        return this;
    }

    public PropertyConduit getConduit()
    {
        return _conduit;
    }

    public PropertyEditModel conduit(PropertyConduit conduit)
    {
        _conduit = conduit;

        return this;
    }

    public PropertyEditModel label(String label)
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

    public PropertyEditModel order(int order)
    {
        _order = order;
        return this;
    }

    public PropertyEditModel add(String propertyName)
    {
        return _model.add(propertyName);
    }

    public PropertyEditModel get(String propertyName)
    {
        return _model.get(propertyName);
    }

    public BeanEditorModel model()
    {
        return _model;
    }

    public PropertyEditModel editorType(String editorType)
    {
        _editorType = editorType;

        return this;
    }

    public String getEditorType()
    {
        return _editorType;
    }
}
