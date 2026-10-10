// Copyright 2026 The Apache Software Foundation
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
package org.apache.tapestry5.ioc.internal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.beanmodel.services.PlasticProxyFactoryImpl;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.services.AssembledPipeline;
import org.apache.tapestry5.ioc.services.PipelineBuilder;
import org.apache.tapestry5.ioc.test.RecordingLogger;
import org.apache.tapestry5.ioc.test.internal.services.StandardFilter;
import org.apache.tapestry5.ioc.test.internal.services.StandardService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

class PipelineBuilderImplTest
{
    private static final String STANDARD_SERVICE = StandardService.class.getName();

    private static final String STANDARD_FILTER = StandardFilter.class.getName();

    private static PlasticProxyFactory proxyFactory;

    private PipelineBuilder builder;

    private RecordingLogger logger;

    @BeforeAll
    static void createProxyFactory()
    {
        proxyFactory = new PlasticProxyFactoryImpl(PipelineBuilderImplTest.class.getClassLoader(), null);
    }

    /**
     * A fresh builder for each test, since a builder keeps track of the pipelines it has assembled.
     */
    @BeforeEach
    void createBuilder()
    {
        builder = new PipelineBuilderImpl(proxyFactory, new DefaultImplementationBuilderImpl(proxyFactory));

        logger = new RecordingLogger();
    }

    /**
     * A filter that delegates to the next service, with a fixed {@code toString()} so that the description of the
     * assembled pipeline is predictable.
     */
    private static StandardFilter namedFilter(final String name)
    {
        return new StandardFilter()
        {
            @Override
            public int run(int i, StandardService service)
            {
                return service.run(i);
            }

            @Override
            public String toString()
            {
                return name;
            }
        };
    }

    /**
     * A terminator with a fixed {@code toString()}, for the same reason.
     */
    private static StandardService namedTerminator(final String name)
    {
        return new StandardService()
        {
            @Override
            public int run(int i)
            {
                return i;
            }

            @Override
            public String toString()
            {
                return name;
            }
        };
    }

    @Test
    void standardPipelineWithFilters()
    {
        StandardFilter subtracter = (i, service) -> service.run(i) - 2;

        StandardFilter multiplier = (i, service) -> 2 * service.run(i);

        StandardFilter adder = (i, service) -> service.run(i + 3);

        StandardService terminator = i -> i;

        StandardService pipeline = builder.build(logger, StandardService.class, StandardFilter.class,
                Arrays.asList(subtracter, multiplier, adder), terminator);

        assertEquals(14, pipeline.run(5));
        assertEquals(24, pipeline.run(10));
    }

    @Test
    void aPipelineWithoutFiltersIsSimplyTheTerminator()
    {
        StandardService terminator = i -> i;

        StandardService pipeline = builder.build(logger, StandardService.class, StandardFilter.class,
                Collections.emptyList(), terminator);

        assertSame(terminator, pipeline);
    }

    @Test
    void aPipelineWithNoFiltersAndNoTerminatorDoesNothing()
    {
        StandardService pipeline = builder.build(logger, StandardService.class, StandardFilter.class,
                Collections.emptyList());

        assertEquals(0, pipeline.run(99));
    }

    @Test
    void toStringOfPipelineDescribesTheWholeChain()
    {
        StandardService pipeline = builder.build(logger, StandardService.class, StandardFilter.class,
                Arrays.asList(namedFilter("FIRST"), namedFilter("SECOND")), namedTerminator("TERMINATOR"));

        assertEquals(String.format("<PipelineBridge from %s to %s>: FIRST -> SECOND -> TERMINATOR", STANDARD_SERVICE,
                STANDARD_FILTER), pipeline.toString());
    }

    @Test
    void everyStepOfThePipelineDescribesItselfOnwards()
    {
        StandardService pipeline = builder.build(logger, StandardService.class, StandardFilter.class,
                Arrays.asList(namedFilter("FIRST"), namedFilter("SECOND")), namedTerminator("TERMINATOR"));

        Object secondStep = ((PipelineStep) pipeline).getPipelineNext();

        assertEquals(String.format("<PipelineBridge from %s to %s>: SECOND -> TERMINATOR", STANDARD_SERVICE,
                STANDARD_FILTER), secondStep.toString());
    }

    @Test
    void thePipelineExposesItsFiltersInOrder()
    {
        StandardFilter first = (i, service) -> service.run(i);

        StandardFilter second = (i, service) -> service.run(i);

        StandardService terminator = i -> i;

        StandardService pipeline = builder.build(logger, StandardService.class, StandardFilter.class,
                Arrays.asList(first, second), terminator);

        PipelineStep firstStep = (PipelineStep) pipeline;

        assertSame(first, firstStep.getPipelineFilter());

        PipelineStep secondStep = (PipelineStep) firstStep.getPipelineNext();

        assertSame(second, secondStep.getPipelineFilter());
        assertSame(terminator, secondStep.getPipelineNext());
    }

    @Test
    void toStringOfPipelineEndsWithTheDefaultTerminator()
    {
        StandardService pipeline = builder.build(logger, StandardService.class, StandardFilter.class,
                Collections.singletonList(namedFilter("ONLY")));

        assertEquals(String.format("<PipelineBridge from %s to %s>: ONLY -> <NoOp %s>", STANDARD_SERVICE,
                STANDARD_FILTER, STANDARD_SERVICE), pipeline.toString());
    }

    @Test
    void assembledPipelinesAreTracked()
    {
        builder.build(logger, StandardService.class, StandardFilter.class,
                Arrays.asList(namedFilter("FIRST"), namedFilter("SECOND")), namedTerminator("TERMINATOR"));

        List<AssembledPipeline> pipelines = builder.getAssembledPipelines();

        assertEquals(1, pipelines.size());

        AssembledPipeline pipeline = pipelines.get(0);

        assertEquals(StandardService.class, pipeline.getServiceInterface());
        assertEquals(StandardFilter.class, pipeline.getFilterInterface());
        assertEquals(Arrays.asList("FIRST", "SECOND"), pipeline.getFilters());
        assertEquals("TERMINATOR", pipeline.getTerminator());

        assertEquals(String.format("<PipelineBridge from %s to %s>: FIRST -> SECOND -> TERMINATOR", STANDARD_SERVICE,
                STANDARD_FILTER), pipeline.toString());
    }

    @Test
    void aPipelineWithoutFiltersIsTrackedAsWell()
    {
        builder.build(logger, StandardService.class, StandardFilter.class, Collections.emptyList(),
                namedTerminator("TERMINATOR"));

        AssembledPipeline pipeline = builder.getAssembledPipelines().get(0);

        assertTrue(pipeline.getFilters().isEmpty());
        assertEquals("TERMINATOR", pipeline.getTerminator());
    }

    @Test
    void theAssembledPipelineIsLoggedAtDebugLevel()
    {
        StandardService pipeline = builder.build(logger, StandardService.class, StandardFilter.class,
                Collections.singletonList(namedFilter("ONLY")));

        assertEquals(Collections.singletonList("Assembled pipeline: " + pipeline), logger.getMessages(Level.DEBUG));
    }
}
