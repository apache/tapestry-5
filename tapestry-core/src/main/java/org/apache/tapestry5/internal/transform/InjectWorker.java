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

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.InjectionProvider2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Performs injection triggered by any field annotated with the {@link org.apache.tapestry5.ioc.annotations.Inject}
 * annotation or the {@link javax.inject.Inject} annotation.
 *
 * The implementation of this worker mostly delegates to a chain of command of {@link InjectionProvider2}.
 */
public class InjectWorker implements ComponentClassTransformWorker2
{
    private final ObjectLocator locator;

    // Really, a chain of command

    private final InjectionProvider2 injectionProvider;

    private final OperationTracker tracker;

    private final Predicate<PlasticField> MATCHER = new Predicate<PlasticField>()
    {
        public boolean accept(PlasticField field)
        {
            return field.hasAnnotation(Inject.class) ||
                    field.hasAnnotation(javax.inject.Inject.class);
        }
    };

    public InjectWorker(ObjectLocator locator, InjectionProvider2 injectionProvider, OperationTracker tracker)
    {
        this.locator = locator;
        this.injectionProvider = injectionProvider;
        this.tracker = tracker;
    }

    public void transform(final PlasticClass plasticClass, TransformationSupport support, final MutableComponentModel model)
    {
        for (final PlasticField field : F.flow(plasticClass.getUnclaimedFields()).filter(MATCHER))
        {
            final String fieldName = field.getName();

            tracker.run(String.format("Injecting field  %s.%s", plasticClass.getClassName(), fieldName), new Runnable()
            {
                public void run()
                {
                    try
                    {
                        boolean success = injectionProvider.provideInjection(field, locator, model);

                        if (success)
                        {
                            field.claim("@Inject");
                        }
                    } catch (RuntimeException ex)
                    {
                        throw new RuntimeException(String.format("Error obtaining injected value for field %s.%s: %s", plasticClass.getClassName(), fieldName, ExceptionUtils.toMessage(ex)), ex);
                    }
                }
            });
        }
    }
}
