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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationException;

import java.util.List;

/**
 * Aggregates together a number of field validator instances as a single unit.
 */
public final class CompositeFieldValidator implements FieldValidator
{
    private final FieldValidator[] validators;

    public CompositeFieldValidator(List<FieldValidator> validators)
    {
        this.validators = validators.toArray(new FieldValidator[validators.size()]);
    }

    @SuppressWarnings("unchecked")
    public void validate(Object value) throws ValidationException
    {
        for (FieldValidator fv : validators)
            fv.validate(value);
    }

    public void render(MarkupWriter writer)
    {
        for (FieldValidator fv : validators)
            fv.render(writer);
    }

    public boolean isRequired()
    {
        for (FieldValidator fv : validators)
        {
            if (fv.isRequired()) return true;
        }

        return false;
    }

}
