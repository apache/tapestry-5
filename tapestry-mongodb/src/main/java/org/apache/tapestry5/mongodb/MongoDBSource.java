package org.apache.tapestry5.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 *
 */
@UsesOrderedConfiguration(ServerAddress.class)
public interface MongoDBSource
{
    /**
     * @return the {@link MongoClient} database connection object.
     */
    public MongoClient getMongo();
}
