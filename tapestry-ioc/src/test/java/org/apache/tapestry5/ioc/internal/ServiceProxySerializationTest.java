// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

import java.io.*;

public class ServiceProxySerializationTest extends IOCTestCase
{
    @Test
    public void serialization_deserialization() throws Exception
    {
        Registry r = buildRegistry();

        TypeCoercer proxy = r.getService(TypeCoercer.class);

        byte[] serialized = serialize(proxy);

        TypeCoercer proxy2 = deserialize(TypeCoercer.class, serialized);

        assertSame(proxy2, proxy, "De-serialized proxy is same object if Registry unchanged.");

        r.shutdown();

        r = buildRegistry();

        TypeCoercer proxy3 = deserialize(TypeCoercer.class, serialized);

        assertNotNull(proxy3);
        assertNotSame(proxy3, proxy, "New proxy should be different, as it is from a different Registry.");

        r.shutdown();
    }

    @Test
    public void deserialize_with_no_registry() throws Exception
    {
        Registry r = buildRegistry();

        TypeCoercer proxy = r.getService(TypeCoercer.class);

        byte[] serialized = serialize(proxy);

        r.shutdown();

        try
        {
            deserialize(TypeCoercer.class, serialized);
            unreachable();
        }
        catch (Exception ex)
        {
            assertMessageContains(ex,
                                  "Service token for service 'TypeCoercer' can not be converted back into a proxy because no proxy provider has been registered");
        }
    }

    private byte[] serialize(Object object) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(object);

        oos.close();

        return baos.toByteArray();
    }

    private <T> T deserialize(Class<T> type, byte[] serialized) throws IOException, ClassNotFoundException
    {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialized));

        Object raw = ois.readObject();

        ois.close();

        return type.cast(raw);
    }
}
