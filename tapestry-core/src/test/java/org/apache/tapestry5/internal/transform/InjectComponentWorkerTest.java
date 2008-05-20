// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.test.TapestryTestCase;
import static org.easymock.EasyMock.contains;
import static org.easymock.EasyMock.same;
import org.testng.annotations.Test;

public class InjectComponentWorkerTest extends TapestryTestCase
{

    private static final String CLASS_NAME = Grid.class.getName();

    @Test
    public void default_id_from_field_name()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        InjectComponent annotation = newMock(InjectComponent.class);
        ComponentClassTransformWorker worker = new InjectComponentWorker();

        train_findFieldsWithAnnotation(ct, InjectComponent.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", InjectComponent.class, annotation);
        train_getFieldType(ct, "myfield", CLASS_NAME);
        train_getResourcesFieldName(ct, "resources");
        expect(annotation.value()).andReturn("");
        ct.makeReadOnly("myfield");

        ct.extendMethod(same(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE),
                        contains(
                                "myfield = (" + CLASS_NAME + ") resources.getEmbeddedComponent(\"myfield\");"));


        replay();

        worker.transform(ct, model);

        verify();
    }

    @Test
    public void explicit_component_id_provided_as_annotation()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        InjectComponent annotation = newMock(InjectComponent.class);
        ComponentClassTransformWorker worker = new InjectComponentWorker();

        train_findFieldsWithAnnotation(ct, InjectComponent.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", InjectComponent.class, annotation);
        train_getFieldType(ct, "myfield", CLASS_NAME);
        train_getResourcesFieldName(ct, "resources");
        expect(annotation.value()).andReturn("id_provided_as_annotation").atLeastOnce();
        ct.makeReadOnly("myfield");

        ct.extendMethod(same(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE),
                        contains(
                                "myfield = (" + CLASS_NAME + ") resources.getEmbeddedComponent(\"id_provided_as_annotation\");"));

        replay();

        worker.transform(ct, model);

        verify();
    }

}
