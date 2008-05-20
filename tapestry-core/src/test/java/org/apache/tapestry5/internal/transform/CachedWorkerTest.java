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

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.TransformMethodSignature;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Mostly just testing error conditions here. Functionality testing in integration tests.
 */
@Test
public class CachedWorkerTest extends TapestryTestCase
{
    public void must_have_return_type() throws Exception
    {
        ClassTransformation ct = mockClassTransformation();
        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "void", "getFoo", new String[0],
                                                                    new String[0]);

        expect(ct.findMethodsWithAnnotation(Cached.class)).andReturn(Arrays.asList(sig));

        replay();
        try
        {
            new CachedWorker(null).transform(ct, null);
            fail("did not throw");
        }
        catch (IllegalArgumentException e)
        {
        }
        verify();
    }

    public void must_not_have_parameters() throws Exception
    {
        ClassTransformation ct = mockClassTransformation();
        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "java.lang.Object", "getFoo",
                                                                    new String[] { "boolean" }, new String[0]);

        expect(ct.findMethodsWithAnnotation(Cached.class)).andReturn(Arrays.asList(sig));

        replay();
        try
        {
            new CachedWorker(null).transform(ct, null);
            fail("did not throw");
        }
        catch (IllegalArgumentException e)
        {
        }
        verify();
    }
}
