// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.pages;

import org.apache.tapestry5.annotations.WhitelistAccessOnly;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.metrics.Metric;
import org.apache.tapestry5.ioc.services.metrics.MetricCollector;
import org.apache.tapestry5.services.BeanModelSource;

import java.util.List;

@WhitelistAccessOnly
/**
 * Contributes to the {@link T5Dashboard} page, providing application metrics from the {@link org.apache.tapestry5.ioc.services.metrics.MetricCollector}.
 */
public class AppMetrics
{
    @Inject
    MetricCollector collector;

    @Inject
    BeanModelSource beanModelSource;

    @Inject
    private Messages messages;

    public final BeanModel<Metric> metricModel = beanModelSource.createDisplayModel(Metric.class, messages);

    public List<Metric> getRootMetrics()
    {
        return collector.getRootMetrics();
    }
}
