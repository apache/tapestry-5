package org.apache.tapestry5.internal.mongodb;

import org.apache.tapestry5.ioc.services.ThreadCleanupListener;
import org.apache.tapestry5.mongodb.MongoDB;
import org.apache.tapestry5.mongodb.MongoDBSource;
import org.slf4j.Logger;

import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * Default implementation for {@link org.apache.tapestry5.mongodb.MongoDB}
 */
public class MongoDBImpl implements MongoDB, ThreadCleanupListener
{
    private final Logger logger;

	private final Mongo mongo;

    private final String defaultDbName;
    private final boolean consistentRequest;

	private final boolean secureMode;
	private final String dbUsername;
	private final String dbPassword;

    private DB db;

    public MongoDBImpl(Logger logger,
		   MongoDBSource mongoDBSource,
           String defaultDbName, boolean consistentRequest,
		   boolean secureMode, String dbUsername, String dbPassword)
    {
        this.logger = logger;

        this.mongo = mongoDBSource.getMongo();

        this.defaultDbName = defaultDbName;
        this.consistentRequest = consistentRequest;

		this.secureMode = secureMode;
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;
    }

    public DB getDefaultMongoDb()
    {
        return buildDbSession(defaultDbName);
    }

    public DB getMongoDb(String dbname)
    {
		return buildDbSession(dbname);
    }

    public void threadDidCleanup()
    {
        if (consistentRequest)
            db.requestDone();
    }


	private final DB buildDbSession(String dbname)
	{
		db = mongo.getDB(dbname);

		if (consistentRequest)
		{
			db.requestStart();
			db.requestEnsureConnection();
		}

		if (secureMode)
		{
			db.authenticate(dbUsername, dbPassword.toCharArray());
		}

		return db;
	}
}
