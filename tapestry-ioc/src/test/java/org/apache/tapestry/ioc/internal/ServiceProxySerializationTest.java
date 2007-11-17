package org.apache.tapestry.ioc.internal;

import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.ioc.test.IOCTestCase;
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

        byte[] serialized = baos.toByteArray();
        return serialized;
    }

    private <T> T deserialize(Class<T> type, byte[] serialized) throws IOException, ClassNotFoundException
    {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialized));

        Object raw = ois.readObject();

        ois.close();

        return type.cast(raw);
    }
}
