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

package org.apache.tapestry5.services;

import org.apache.tapestry5.MarkupWriter;

/**
 * Default object renderer as a catch all for class <code>Object</code>.
 * 
 * @since 5.2.0
 */
public class DefaultObjectRenderer implements ObjectRenderer<Object>
{
    public void render(Object object, MarkupWriter writer)
    {
        try
        {
            writer.write(String.valueOf(object));
        }
        catch (Exception ex)
        {
            writer.element("span", "class", "t-render-object-error");

            String message = ex.getMessage();

            String exceptionClassName = ex.getClass().getName();

            String exceptionId = message == null ? exceptionClassName : String.format("(%s) %s", ex
                    .getClass().getName(), message);

            writer.writef("Exception rendering description for object of type %s: %s", object
                    .getClass().getName(), exceptionId);

            writer.end();

        }
    }
}
