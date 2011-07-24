// Copyright 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.testng.annotations.Test;

public class HeartbeatDeferredWorkerTest extends InternalBaseTestCase
{
    private final HeartbeatDeferredWorker worker = new HeartbeatDeferredWorker(null);

    public String shouldReturnVoid()
    {
        return null;
    }

    public void noCheckedException() throws Exception
    {
    }

    @Test
    public void non_void_method_will_fail()
    {
        testFailure(
                PlasticUtils.getMethodDescription(getClass(), "shouldReturnVoid"),
                "as it is not a void method");
    }

    @Test
    public void checked_exceptions_will_fail()
    {
        testFailure(PlasticUtils.getMethodDescription(getClass(), "noCheckedException"),
                "as it throws checked exceptions");
    }

    private void testFailure(MethodDescription description, String messageFragment)
    {
        PlasticMethod method = newMock(PlasticMethod.class);

        boolean isVoid = description.returnType.equals("void");
        expect(method.isVoid()).andReturn(isVoid);

        if (isVoid)
        {
            expect(method.getDescription()).andReturn(description).atLeastOnce();
        }

        expect(method.getMethodIdentifier()).andReturn("<MethodId>");

        replay();

        try
        {
            worker.deferMethodInvocations(method);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, messageFragment);
        }

        verify();
    }
}
