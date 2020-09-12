// Copyright 2006, 2007, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.test;

import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.Order;
import org.apache.tapestry5.ioc.services.StrategyBuilder;

import java.util.List;
import java.util.Map;

/**
 * Module used to demonstrate decorator ordering.
 */

public class BarneyModule {
  @Match(
      {"UnorderedNames", "Fred", "PrivateFredAlias"})
  @Order("after:Beta")
  public Object decorateGamma(Object delegate, DecoratorList list) {
    list.add("gamma");

    return null;
  }

  public Sizer buildSizer(final Map<Class, Sizer> configuration, StrategyBuilder builder) {

    return builder.build(Sizer.class, configuration);
  }

  public void contributeSizer(MappedConfiguration<Class, Sizer> configuration) {
    Sizer listSizer = new Sizer() {
      @Override
      public int size(Object object) {
        List list = (List) object;

        return list.size();
      }
    };

    Sizer mapSizer = new Sizer() {
      @Override
      public int size(Object object) {
        Map map = (Map) object;

        return map.size();
      }
    };


    configuration.add(List.class, listSizer);
    configuration.add(Map.class, mapSizer);
  }

  @Contribute(Sizer.class)
  public void moreSizerContributions(MappedConfiguration<Class, Sizer> configuration) {
    Sizer defaultSizer = new Sizer() {
      @Override
      public int size(Object object) {
        return 1;
      }
    };

    Sizer nullSizer = new Sizer() {
      @Override
      public int size(Object object) {
        return 0;
      }
    };

    configuration.add(Object.class, defaultSizer);
    configuration.add(void.class, nullSizer);

  }

  /**
   * Put DecoratorList in module barney, where so it won't accidentally be decorated (which recusively builds the
   * service, and is caught as a failure).
   */
  public DecoratorList buildDecoratorList() {
    return new DecoratorList() {
      private List<String> names = CollectionFactory.newList();

      @Override
      public void add(String name) {
        names.add(name);
      }

      @Override
      public List<String> getNames() {
        return names;
      }
    };
  }

  public void contributeUnorderedNames(Configuration<String> configuration) {
    configuration.add("Gamma");
  }

  public void contributeStringLookup(MappedConfiguration<String, String> configuration) {
    configuration.add("barney", "BARNEY");
    configuration.add("betty", "BETTY");
  }
}
