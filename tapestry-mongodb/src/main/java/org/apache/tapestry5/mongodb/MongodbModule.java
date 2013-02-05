package org.apache.tapestry5.mongodb;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import org.apache.tapestry5.internal.mongodb.MongoDBImpl;
import org.apache.tapestry5.internal.mongodb.MongoDBSourceImpl;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.slf4j.Logger;

/**
 * Defines services which are responsible for MongoDB initializations and connections.
 */
public class MongodbModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(MongoDBSource.class, MongoDBSourceImpl.class);
    }

    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(MongoDBSymbols.CONNECTIONS_PER_HOSTS, "10");
        configuration.add(MongoDBSymbols.WRITE_CONCERN, "ACKNOWLEDGED");
        configuration.add(MongoDBSymbols.READ_PREFERENCE, "PRIMARY");
        configuration.add(MongoDBSymbols.CONSISTENT_REQUEST, "false");

		// Authentication (Mongo in secure mode)

		configuration.add(MongoDBSymbols.SECURE_MODE, "false");
		configuration.add(MongoDBSymbols.DB_USERNAME, "");
		configuration.add(MongoDBSymbols.DB_PASSWORD, "");
    }

    @Scope(ScopeConstants.PERTHREAD)
    public static MongoDB buildMongoDB(Logger logger, final MongoDBSource mongoDBSource,
                     PerthreadManager perthreadManager,
                     @Symbol(MongoDBSymbols.DEFAULT_DB_NAME) String defaultDbName,
                     @Symbol(MongoDBSymbols.CONSISTENT_REQUEST) boolean consistentRequest,
					 @Symbol(MongoDBSymbols.SECURE_MODE) boolean secureMode,
					 @Symbol(MongoDBSymbols.DB_USERNAME) String dbUsername,
					 @Symbol(MongoDBSymbols.DB_PASSWORD) String dbPassword)
    {
        final MongoDBImpl mongoDB = new MongoDBImpl(logger, mongoDBSource,
                defaultDbName, consistentRequest, secureMode, dbUsername, dbPassword);

		perthreadManager.addThreadCleanupListener(mongoDB);

        return mongoDB;
    }

    /**
     * Contribute coercions for {@link WriteConcern} and {@link ReadPreference} to have them from
     * {@link org.apache.tapestry5.ioc.annotations.Symbol}
     *
     * @param configuration lets help the {@link org.apache.tapestry5.ioc.services.TypeCoercer} service
     */
    public static void contributeTypeCoercer(Configuration<CoercionTuple> configuration)
    {
        configuration.add(new CoercionTuple(String.class, WriteConcern.class,
                new Coercion<String, WriteConcern>() {
                    @Override
                    public WriteConcern coerce(String input)
                    {
                        if (input.equalsIgnoreCase("FSYNC_SAFE"))
                        {
                            return WriteConcern.FSYNC_SAFE;
                        }
                        else if (input.equalsIgnoreCase("JOURNAL_SAFE"))
                        {
                            return WriteConcern.JOURNAL_SAFE;
                        }
                        else if (input.equalsIgnoreCase("MAJORITY"))
                        {
                            return WriteConcern.MAJORITY;
                        }
                        else if (input.equalsIgnoreCase("NONE"))
                        {
                            return WriteConcern.NONE;
                        }
                        else if (input.equalsIgnoreCase("REPLICAS_SAFE"))
                        {
                            return WriteConcern.REPLICAS_SAFE;
                        }
                        else if (input.equalsIgnoreCase("SAFE"))
                        {
                            return WriteConcern.SAFE;
                        }
						else if (input.equalsIgnoreCase("NORMAL"))
						{
							return WriteConcern.NORMAL;
						}
                        else // WriteConcern.ACKNOWLEDGED IS OUR DEFAULT
                        {
                            return WriteConcern.ACKNOWLEDGED;
                        }
                    }
                }
        ));

        configuration.add(new CoercionTuple(String.class, ReadPreference.class, new Coercion<String, ReadPreference>() {
            @Override
            public ReadPreference coerce(String input)
            {
                if (input.equalsIgnoreCase("SECONDARY"))
                {
                    return ReadPreference.secondary();
                }
                else // PRIMARY IS OUR DEFAULT
                {
                    return ReadPreference.primary();
                }
            }
        }));
    }
}
