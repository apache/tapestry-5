// Copyright 2006, 2007 The Apache Software Foundation
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

import java.lang.annotation.Annotation;
import java.util.List;

import javax.inject.Named;

import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.InjectionProvider;
import org.apache.tapestry5.services.TransformField;

/**
 * Performs injection triggered by any field annotated with the {@link org.apache.tapestry5.ioc.annotations.Inject}
 * annotation or the {@link javax.inject.Inject} annotation.
 * <p/>
 * The implementation of this worker mostly delegates to a chain of command of
 * {@link org.apache.tapestry5.services.InjectionProvider}s.
 */
public class InjectWorker implements ComponentClassTransformWorker
{
    private final ObjectLocator locator;

    // Really, a chain of command

    private final InjectionProvider injectionProvider;

    private final OperationTracker tracker;

    public InjectWorker(ObjectLocator locator, InjectionProvider injectionProvider, OperationTracker tracker)
    {
        this.locator = locator;
        this.injectionProvider = injectionProvider;
        this.tracker = tracker;
    }

    public final void transform(final ClassTransformation transformation, final MutableComponentModel model)
    {
    	List<TransformField> fields = matchFields(transformation);
    	
        for (final TransformField field : fields)
        {
        	final String fieldName = field.getName();
        	
            tracker.run("Injecting field " + fieldName, new Runnable()
            {
                public void run()
                {

                    Inject inject = field.getAnnotation(Inject.class);
                    
                    Annotation annotation = inject == null? field.getAnnotation(javax.inject.Inject.class): inject;

                    try
                    {
                        String fieldType = field.getType();

                        Class type = transformation.toClass(fieldType);

                        boolean success = injectionProvider.provideInjection(fieldName, type, locator, transformation,
                                model);

                        if (success)
                            field.claim(annotation);
                    }
                    catch (RuntimeException ex)
                    {
                        throw new RuntimeException(TransformMessages.fieldInjectionError(transformation.getClassName(),
                                fieldName, ex), ex);
                    }
                }
            });
        }
    }
    
    private List<TransformField> matchFields(final ClassTransformation transformation)
    {	
    	Predicate<TransformField> predicate = new Predicate<TransformField>()
    	{
			public boolean accept(TransformField field) 
			{
				return field.getAnnotation(Inject.class) != null 
						|| (field.getAnnotation(javax.inject.Inject.class) != null && field.getAnnotation(Named.class) == null);
			}
		};
    	return transformation.matchFields(predicate);
    }
}
