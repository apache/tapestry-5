package org.apache.tapestry5.integration.app1;

import org.apache.tapestry5.ValueEncoder;

public class StuffValueEncoder implements ValueEncoder<Stuff>
{
    public String toClient(Stuff value)
    {
        return value.uuid;
    }

    public Stuff toValue(String clientValue)
    {
        return Stuff.ROOT.seek(clientValue);
    }
}
