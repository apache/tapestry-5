// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5;

import org.apache.tapestry5.dom.Element;

/**
 * Base implementation of {@link ValidationDecorator} that does nothing. Subclasses may override specific methods,
 * knowing that all other methods do nothing at all.
 */
public class BaseValidationDecorator implements ValidationDecorator
{

    public void beforeLabel(Field field)
    {
    }

    public void afterLabel(Field field)
    {
    }

    public void afterField(Field field)
    {
    }

    public void beforeField(Field field)
    {
    }

    public void insideField(Field field)
    {
    }

    public void insideLabel(Field field, Element labelElement)
    {
    }

}
