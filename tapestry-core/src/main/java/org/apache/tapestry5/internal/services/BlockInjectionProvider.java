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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Id;
import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.internal.transform.ReadOnlyComponentFieldConduit;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.FieldConduit;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.transform.InjectionProvider2;

/**
 * Identifies fields of type {@link Block} that have the {@link Inject} annotation and converts them
 * into read-only
 * fields containing the injected Block from the template. The annotation's value is the id of the
 * block to inject; if
 * omitted, the block id is deduced from the field id.
 *
 * Must be scheduled before {@link DefaultInjectionProvider} because it uses the same annotation, Inject, with a
 * different interpretation.
 */
public class BlockInjectionProvider implements InjectionProvider2
{
    private static final String BLOCK_TYPE_NAME = Block.class.getName();

    public boolean provideInjection(PlasticField field, ObjectLocator locator, MutableComponentModel componentModel)
    {
        if (!field.getTypeName().equals(BLOCK_TYPE_NAME))
        {
            return false;
        }

        Id annotation = field.getAnnotation(Id.class);

        String blockId = getBlockId(field.getName(), annotation);

        FieldConduit<Object> conduit = createConduit(field, blockId);

        field.setConduit(conduit);

        return true; // claim the field
    }

    private FieldConduit<Object> createConduit(PlasticField field, final String blockId)
    {
        final String className = field.getPlasticClass().getClassName();
        final String fieldName = field.getName();

        return new ReadOnlyComponentFieldConduit(className, fieldName)
        {
            public Object get(Object instance, InstanceContext context)
            {
                ComponentResources resources = context.get(ComponentResources.class);

                return resources.getBlock(blockId);
            }
        };
    }

    private String getBlockId(String fieldName, Id annotation)
    {
        return annotation != null ? annotation.value() : InternalUtils.stripMemberName(fieldName);
    }
}
