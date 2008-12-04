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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.services.BeanBlockSource;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

/**
 * Tests an error case for the PropertyEditor component. The success cases are mixed into the BeanEditForm component's
 * integration tests.
 */
public class PropertyEditorTest extends TapestryTestCase
{
    @Test
    public void no_editor_block_available()
    {
        PropertyModel model = mockPropertyModel();
        PropertyOverrides overrides = mockPropertyOverrides();
        ComponentResources resources = mockComponentResources();
        BeanBlockSource source = newMock(BeanBlockSource.class);
        RuntimeException exception = new RuntimeException("Simulated failure.");
        Messages messages = mockMessages();
        Location l = mockLocation();

        String propertyId = "foo";
        String dataType = "unk";
        String propertyName = "fooProp";
        Object object = "[OBJECT]";
        String formattedMessage = "formatted-message";

        expect(model.getId()).andReturn(propertyId);

        train_getOverrideBlock(overrides, propertyId, null);

        expect(model.getDataType()).andReturn(dataType);

        expect(source.getEditBlock(dataType)).andThrow(exception);
        expect(model.getPropertyName()).andReturn(propertyName);

        train_getLocation(resources, l);

        expect(messages.format("block-error", propertyName, dataType, object, exception))
                .andReturn(formattedMessage);

        replay();

        PropertyEditor pe = new PropertyEditor();

        pe.inject(resources, overrides, model, source, messages, object);

        try
        {
            pe.beginRender();
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertEquals(ex.getMessage(), formattedMessage);
            assertSame(ex.getLocation(), l);
        }

    }

}
