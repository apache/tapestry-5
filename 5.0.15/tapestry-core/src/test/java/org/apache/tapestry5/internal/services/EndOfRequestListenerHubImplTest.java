//  Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.internal.events.EndOfRequestEvent;
import org.apache.tapestry5.internal.events.EndOfRequestListener;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.Request;
import org.easymock.Capture;
import static org.easymock.EasyMock.capture;
import org.testng.annotations.Test;

public class EndOfRequestListenerHubImplTest extends InternalBaseTestCase
{
    @Test
    public void add_and_notify()
    {
        EndOfRequestListenerHub hub = new EndOfRequestListenerHubImpl();
        final Request request = mockRequest();

        EndOfRequestListener listener = newMock(EndOfRequestListener.class);

        Capture<EndOfRequestEvent> eventCapture = newCapture();

        listener.requestDidComplete(capture(eventCapture));

        replay();

        hub.addEndOfRequestListener(listener);

        hub.fire(request);

        verify();

        assertSame(eventCapture.getValue().getRequest(), request);
    }


    @Test
    public void add_remove_notify()
    {
        EndOfRequestListenerHub hub = new EndOfRequestListenerHubImpl();
        final Request request = mockRequest();

        EndOfRequestListener listener = newMock(EndOfRequestListener.class);

        replay();

        hub.addEndOfRequestListener(listener);
        hub.removeEndOfRequestListener(listener);

        hub.fire(request);

        verify();
    }
}
