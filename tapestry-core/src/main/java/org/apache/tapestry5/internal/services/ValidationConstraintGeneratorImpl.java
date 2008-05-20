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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.services.ValidationConstraintGenerator;

import java.util.List;

public class ValidationConstraintGeneratorImpl implements ValidationConstraintGenerator
{
    private final List<ValidationConstraintGenerator> configuration;

    public ValidationConstraintGeneratorImpl(final List<ValidationConstraintGenerator> configuration)
    {
        this.configuration = configuration;
    }

    public List<String> buildConstraints(Class propertyType, AnnotationProvider annotationProvider)
    {
        notNull(propertyType, "propertyType");
        notNull(annotationProvider, "annotationProvider");

        List<String> result = CollectionFactory.newList();

        for (ValidationConstraintGenerator g : configuration)
        {
            List<String> constraints = g.buildConstraints(propertyType, annotationProvider);

            if (constraints != null)
                result.addAll(constraints);
        }

        // TODO: How to handle duplicate or conflicting constraints from different generators?

        return result;
    }
}
