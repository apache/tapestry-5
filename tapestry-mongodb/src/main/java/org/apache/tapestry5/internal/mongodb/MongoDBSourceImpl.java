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

    public MongoClient getMongo()
    {
        return this.mongoClient;
    }
}
