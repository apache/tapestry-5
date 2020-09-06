// Copyright 2009, 2011, 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.hibernate.web.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.UnknownActivationContextCheck;
import org.apache.tapestry5.annotations.WhitelistAccessOnly;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.stat.*;

import java.util.Collection;

/**
 * Page used to see the Hibernate statistics.
 *
 * @since 5.1.0.2
 */
@UnknownActivationContextCheck(false)
@WhitelistAccessOnly
public class HibernateStatistics
{
    @Inject
    private Session session;

    @Property
    @Inject
    @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
    private boolean productionMode;

    @Property
    private String currentEntityName;

    @Property
    private String currentCollectionRoleName;

    @Property
    private String currentQuery;

    @Property
    private String currentSecondLevelCacheRegionName;

    public Statistics getStatistics()
    {
        return session.getSessionFactory().getStatistics();
    }


    @SuppressWarnings("unchecked")
    public Collection<ClassMetadata> getAllClassMetadata()
    {
        return session.getSessionFactory().getAllClassMetadata().values();
    }

    public EntityStatistics getEntityStatistics()
    {
        return getStatistics().getEntityStatistics(currentEntityName);
    }

    public CollectionStatistics getCollectionStatistics()
    {
        return getStatistics().getCollectionStatistics(currentCollectionRoleName);
    }

    public QueryStatistics getQueryStatistics()
    {
        return getStatistics().getQueryStatistics(currentQuery);
    }

    public SecondLevelCacheStatistics getSecondLevelCacheStatistics()
    {
        return getStatistics().getSecondLevelCacheStatistics(currentSecondLevelCacheRegionName);
    }
}