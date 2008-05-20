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

import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.annotations.ResponseEncoding;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class ResponseEncodingWorkerTest extends TapestryTestCase
{
    @Test
    public void annotation_missing()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_getAnnotation(ct, ResponseEncoding.class, null);

        replay();

        new ResponseEncodingWorker().transform(ct, model);

        verify();
    }

    @Test
    public void annotation_present()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        String value = "UTF-8";

        ResponseEncoding annotation = newMock(ResponseEncoding.class);

        train_getAnnotation(ct, ResponseEncoding.class, annotation);

        expect(annotation.value()).andReturn(value);

        model.setMeta(MetaDataConstants.RESPONSE_ENCODING, value);

        replay();

        new ResponseEncodingWorker().transform(ct, model);

        verify();
    }
}
