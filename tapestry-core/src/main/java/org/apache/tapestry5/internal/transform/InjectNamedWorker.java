// Copyright 2010 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.transform;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformField;

/**
 * Processes the combination of {@link javax.inject.Inject} and {@link javax.inject.Named} annotations.
 * 
 * @since 5.3
 */
public class InjectNamedWorker implements ComponentClassTransformWorker
{
    private final ObjectLocator locator;

    private final ComponentClassCache cache;

    public InjectNamedWorker(ObjectLocator locator, ComponentClassCache cache)
    {
        this.locator = locator;
        this.cache = cache;
    }

    @SuppressWarnings("unchecked")
    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
    	
    	List<TransformField> fields = transformation.matchFields(new Predicate<TransformField>() 
    	{

			public boolean accept(TransformField field) 
			{
				return field.getAnnotation(Inject.class) != null && field.getAnnotation(Named.class) != null;
			}
		});
    	
        for (TransformField field : fields)
        {
        	Named annotation = field.getAnnotation(Named.class);

            field.claim(annotation);

            Class fieldType = cache.forName(field.getType());

            Object service = locator.getService(annotation.value(), fieldType);

            field.inject(service);
        }
    }
}
