package org.apache.tapestry5.mongodb;

import com.mongodb.DB;
import org.slf4j.Logger;

/**
 *
 */
public interface MongoDB
{
    /**
     * Obtain a shared instance of the MongoDB database connection object connected
     * to the default database
     *
     * @return the {@link com.mongodb.DB} connection object
     */
    public DB getDefaultMongoDb();

    /**
     * Obtain a shared instance of the MongoDB database connection object for the
     * specific database
     *
     * @param dbname the database name to connect to
     * @return the {@link DB} connection object
     */
    public DB getMongoDb(String dbname);
}
