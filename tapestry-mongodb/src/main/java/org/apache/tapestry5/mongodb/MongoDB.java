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

import com.mongodb.DB;

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
