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

package org.apache.tapestry5.mongodb;

/**
 * Configuration symbols, for use with contributions to
 * {@link org.apache.tapestry5.ioc.services.ApplicationDefaults}.
 * All {@link org.apache.tapestry5.ioc.annotations.Symbol} are relative to the usage of
 * <a href="http://www.mongodb.org">MongoDB</a> support within Apache Tapestry.
 *
 * @since 5.4
 */
public class MongoDBSymbols
{
    /**
     * The {@link com.mongodb.WriteConcern} to use. Default to {@link com.mongodb.WriteConcern#ACKNOWLEDGED}.
     */
    public static final String WRITE_CONCERN = "tapestry.mongodb.write_concern";

    /**
     * The {@link com.mongodb.ReadPreference} to use. Default to {@link com.mongodb.ReadPreference#primary()}.
     */
    public static final String READ_PREFERENCE = "tapestry.mongodb.read_preference";

    // ########################################################################

    /**
     * The maximum number of connections allowed per host for this Mongo instance. Defaults to 10.
     */
    public static final String CONNECTIONS_PER_HOSTS = "tapestry.mongodb.conns-per-host";

    /**
     * The MongoDB default database name to connect to. No default provided.
     */
    public static final String DEFAULT_DB_NAME = "tapestry.mongodb.default_db_name";

    /**
     * Where to use or not the <em>consisten request</em> paradigm. Default to false;
     */
    public static final String CONSISTENT_REQUEST = "tapestry.mongodb.consistent_request";

	// ########################################################################

	/**
	 * <code>true</code> if you need to connect to Mongo DB in <em>secure mode</em>,
	 * <code>false</code> otherwise.
	 * You need to set DB_USERNAME and DB_PASSWORD too if this is set to <code>true</code>.
	 * Defaults to <code>false</code>
	 */
	public static final String SECURE_MODE = "tapestry.mongodb.secure_mode";

	/**
	 * Username to use to connect Mongo DB.
	 * Defaults to empty string.
	 */
	public static final String DB_USERNAME = "tapestry.mongodb.db_username";

	/**
	 * Password to use to connect Mongo DB.
	 * Defaults to empty string.
	 */
	public static final String DB_PASSWORD = "tapestry.mongodb.db_password";
}
