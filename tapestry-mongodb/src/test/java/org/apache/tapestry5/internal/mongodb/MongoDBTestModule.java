package org.apache.tapestry5.internal.mongodb;

import com.mongodb.ServerAddress;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.mongodb.MongoDBSymbols;

import java.net.UnknownHostException;

/**
 *
 */
public class MongoDBTestModule
{

    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(MongoDBSymbols.DEFAULT_DB_NAME, "TapestryMongoTest");
    }


    public static void contributeMongoDBSource(OrderedConfiguration<ServerAddress> configuration)
    {
        try
        {
            configuration.add("test", new ServerAddress("localhost", 12345));
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
    }
}
