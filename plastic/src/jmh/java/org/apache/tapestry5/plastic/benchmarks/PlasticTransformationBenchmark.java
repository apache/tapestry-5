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

package org.apache.tapestry5.plastic.benchmarks;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.apache.tapestry5.internal.plastic.StandardDelegate;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticManager;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.plastic.PropertyAccessType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Class transformation, which is where a cold Tapestry application spends most of its time.
 *
 * {@code PageLoadBenchmark} in tapestry-core shows a page costing tens of milliseconds to load
 * cold against tens of microseconds to assemble warm. Practically all of that gap is here:
 * reading class bytes, parsing them with ASM, rewriting them, and defining the result. Every page
 * class, component class and mixin goes through it once, and every IoC service gets a generated
 * proxy the same way.
 *
 * Plastic caches per class name, and its cache lives in the {@code PlasticManager}'s class loader,
 * so a class can only be transformed once per manager. Rather than rebuilding state inside the
 * measurement with an invocation-level fixture — which JMH rightly warns about — each benchmark
 * builds its own manager and {@link #createManager()} measures that cost on its own. Subtract it
 * to get the transformation cost:
 *
 * <pre>
 * transformClass - createManager   ~ cost of transforming one page-sized class
 * createProxy    - createManager   ~ cost of one IoC service proxy
 * </pre>
 *
 * <pre>
 * ./gradlew :plastic:jmh -Pjmh.include=PlasticTransformation -Pjmh.profilers=gc
 * </pre>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class PlasticTransformationBenchmark
{
    private static final String SUBJECT = "plasticbenchmarks.subjects.BenchmarkSubject";

    private static final String SUBJECT_PACKAGE = "plasticbenchmarks.subjects";

    /**
     * Roughly what Tapestry does to a component class: turn every field into a conduit-backed
     * property and advise every method.
     */
    private static final PlasticClassTransformer COMPONENT_LIKE = new PlasticClassTransformer()
    {
        @Override
        public void transform(PlasticClass plasticClass)
        {
            for (PlasticField field : plasticClass.getAllFields())
            {
                field.createAccessors(PropertyAccessType.READ_WRITE);
            }

            for (PlasticMethod method : plasticClass.getMethods())
            {
                method.addAdvice(PROCEED);
            }
        }
    };

    private static final MethodAdvice PROCEED = new MethodAdvice()
    {
        @Override
        public void advise(MethodInvocation invocation)
        {
            invocation.proceed();
        }
    };

    /**
     * The baseline to subtract: building a manager and its class loader, transforming nothing.
     */
    @Benchmark
    public PlasticManager createManager()
    {
        return newManager();
    }

    @Benchmark
    public ClassInstantiator<Object> transformClass()
    {
        return newManager().getClassInstantiator(SUBJECT);
    }

    /**
     * What tapestry-ioc pays for every service it proxies during registry startup. 
     */
    @Benchmark
    public ClassInstantiator<Runnable> createProxy()
    {
        return newManager().createProxy(Runnable.class, plasticClass ->
                plasticClass.introduceMethod(RUNNABLE_RUN).changeImplementation(builder -> builder.returnDefaultValue()));
    }

    private static final org.apache.tapestry5.plastic.MethodDescription RUNNABLE_RUN =
            new org.apache.tapestry5.plastic.MethodDescription("void", "run");

    private static PlasticManager newManager()
    {
        return PlasticManager.withContextClassLoader()
                .delegate(new StandardDelegate(COMPONENT_LIKE))
                .packages(Collections.singletonList(SUBJECT_PACKAGE))
                .create();
    }
}
