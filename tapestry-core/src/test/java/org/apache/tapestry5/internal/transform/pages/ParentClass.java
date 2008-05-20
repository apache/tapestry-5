// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform.pages;

import org.apache.tapestry5.annotations.Meta;
import org.apache.tapestry5.annotations.Retain;
import org.apache.tapestry5.internal.transform.InheritedAnnotation;

/**
 * Test class used with {@link org.apache.tapestry5.internal.services.InternalClassTransformationImplTest}
 */
@Meta("foo=bar")
@InheritedAnnotation
public class ParentClass
{
    private int _parentField;

    // Named so that we can force a name conflict

    private String _$conflictField;

    @Retain
    private boolean _annotatedField;

    public void doNothingParentMethod()
    {

    }

    public void _$conflictMethod()
    {

    }

    public String get$conflictField()
    {
        return _$conflictField;
    }

    public void set$conflictField(String field)
    {
        _$conflictField = field;
    }

    public boolean isAnnotatedField()
    {
        return _annotatedField;
    }

    public void setAnnotatedField(boolean annotatedField)
    {
        _annotatedField = annotatedField;
    }

    public int getParentField()
    {
        return _parentField;
    }

    public void setParentField(int parentField)
    {
        _parentField = parentField;
    }

}
