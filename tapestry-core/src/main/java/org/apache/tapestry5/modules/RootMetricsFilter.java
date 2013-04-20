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

package org.apache.tapestry5.modules;

import org.apache.tapestry5.ioc.services.metrics.Metric;
import org.apache.tapestry5.ioc.services.metrics.MetricCollector;
import org.apache.tapestry5.services.HttpServletRequestFilter;
import org.apache.tapestry5.services.HttpServletRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RootMetricsFilter implements HttpServletRequestFilter
{

    private final Metric count, requestTime;

    public RootMetricsFilter(MetricCollector collector)
    {
        count = collector.createRootMetric("request-count", Metric.Type.TOTAL, Metric.Units.COUNTER);
        requestTime = collector.createRootMetric("request-time", Metric.Type.RATE, Metric.Units.MILLISECONDS);
    }

    public boolean service(HttpServletRequest request, HttpServletResponse response, HttpServletRequestHandler handler) throws IOException
    {
        long start = System.currentTimeMillis();

        boolean handled = handler.service(request, response);

        // We don't count the unhandled requests

        if (handled) {
            count.increment();
            requestTime.accumulate(System.currentTimeMillis() - start);
        }

        return handled;
    }
}
