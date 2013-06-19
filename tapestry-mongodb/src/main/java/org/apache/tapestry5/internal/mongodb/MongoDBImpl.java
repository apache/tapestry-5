// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.mongodb;

import com.mongodb.DB;
import com.mongodb.Mongo;
import org.apache.tapestry5.ioc.services.ThreadCleanupListener;
import org.apache.tapestry5.mongodb.MongoDB;
import org.apache.tapestry5.mongodb.MongoDBSource;
import org.slf4j.Logger;

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
