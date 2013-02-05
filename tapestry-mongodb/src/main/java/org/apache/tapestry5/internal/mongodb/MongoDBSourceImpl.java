package org.apache.tapestry5.internal.mongodb;

import com.mongodb.*;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.mongodb.MongoDBSource;
import org.apache.tapestry5.mongodb.MongoDBSymbols;
import org.slf4j.Logger;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Default implementation for {@link org.apache.tapestry5.mongodb.MongoDBSource}
 */
public class MongoDBSourceImpl implements MongoDBSource
{
    private final Logger logger;

    private final MongoClient mongoClient;


    public MongoDBSourceImpl(Logger logger,
            @Symbol(MongoDBSymbols.CONNECTIONS_PER_HOSTS) int connectionPerHost,
            @Symbol(MongoDBSymbols.READ_PREFERENCE) ReadPreference readPreference,
            @Symbol(MongoDBSymbols.WRITE_CONCERN) WriteConcern writeConcern,
            List<ServerAddress> serverAddresses)
    {
        this.logger = logger;

		MongoClientOptions options = new MongoClientOptions.Builder()
				.connectionsPerHost(connectionPerHost)
				.writeConcern(writeConcern).readPreference(readPreference)
				.build();

        if (serverAddresses.isEmpty())
		{
			try
			{
				mongoClient = new MongoClient(new ServerAddress(), options);
			}
			catch (UnknownHostException uhe)
			{
				throw new RuntimeException(uhe);
			}
		}
        else
		{
            mongoClient = new MongoClient(serverAddresses, options);
		}
    }


    @Override
    public MongoClient getMongo()
    {
        return this.mongoClient;
    }
}
