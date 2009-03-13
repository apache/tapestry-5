// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.hibernate.pages;

import java.util.Collection;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.ContentType;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;

/**
 * Page used to see the Hibernate statistics.
 * 
 * @since 5.1.0.2
 *
 */
@ContentType("text/html")
public class Statistics {
   @Inject
   private Session session;

   @Property
   @Inject
   @Symbol(SymbolConstants.PRODUCTION_MODE)
   private boolean productionMode;

   @Property
   private String currentEntityName;

   @Property
   private String currentCollectionRoleName;

   @Property
   private String currentQuery;

   @Property
   private String currentSecondLevelCacheRegionName;

   @Property
   private org.hibernate.stat.Statistics statistics;

   void onActivate() {
     this.statistics = this.session.getSessionFactory().getStatistics();
   }

   @SuppressWarnings("unchecked")
   public Collection<ClassMetadata> getAllClassMetadata() {
     return this.session.getSessionFactory().getAllClassMetadata().values
();
   }

   public EntityStatistics getEntityStatistics() {
     return this.statistics.getEntityStatistics(this.currentEntityName);
   }

   public CollectionStatistics getCollectionStatistics() {
     return this.statistics
           .getCollectionStatistics(this.currentCollectionRoleName);
   }

   public QueryStatistics getQueryStatistics() {
     return this.statistics.getQueryStatistics(this.currentQuery);
   }

   public SecondLevelCacheStatistics getSecondLevelCacheStatistics() {
     return this.statistics
           .getSecondLevelCacheStatistics
(this.currentSecondLevelCacheRegionName);
   }
}