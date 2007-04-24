// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry.ioc.services.PipelineBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Integration tests for the PipelineBuilder service.
 */
public class PipelineBuilderImplTest extends IOCInternalTestCase
{

    private PipelineBuilder _builder;

    private Registry _registry;

    @BeforeClass
    public void setup_builder()
    {
        _registry = buildRegistry();
        _builder = _registry.getService("PipelineBuilder", PipelineBuilder.class);
    }

    @AfterClass
    public void shutdown_builder()
    {
        _registry.shutdown();

        _builder = null;
        _registry = null;
    }

    @Test
    public void pipeline_with_filters()
    {
        Log log = newLog();

        replay();

        StandardFilter subtracter = new StandardFilter()
        {
            public int run(int i, StandardService service)
            {
                return service.run(i) - 2;
            }
        };

        StandardFilter multiplier = new StandardFilter()
        {
            public int run(int i, StandardService service)
            {
                return 2 * service.run(i);
            }
        };

        StandardFilter adder = new StandardFilter()
        {
            public int run(int i, StandardService service)
            {
                return service.run(i + 3);
            }
        };

        StandardService terminator = new StandardService()
        {
            public int run(int i)
            {
                return i;
            }
        };

        StandardService pipeline = _builder.build(
                log,
                StandardService.class,
                StandardFilter.class,
                Arrays.asList(subtracter, multiplier, adder),
                terminator);

        // Should be order subtracter, multipler, adder
        assertEquals(pipeline.run(5), 14);
        assertEquals(pipeline.run(10), 24);

        verify();
    }

    @Test
    public void pipeline_without_filters_is_terminator()
    {
        Log log = newLog();
        StandardService terminator = newMock(StandardService.class);

        replay();

        List<StandardFilter> filters = newList();

        StandardService pipeline = _builder.build(
                log,
                StandardService.class,
                StandardFilter.class,
                filters,
                terminator);

        assertSame(pipeline, terminator);

        verify();
    }

    @Test
    public void pipeline_with_default_terminator()
    {
        Log log = newLog();

        replay();

        List<StandardFilter> filters = newList();

        StandardService pipeline = _builder.build(
                log,
                StandardService.class,
                StandardFilter.class,
                filters);

        assertEquals(pipeline.run(99), 0);

        verify();
    }
}
