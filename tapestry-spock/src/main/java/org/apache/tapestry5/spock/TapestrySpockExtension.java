/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tapestry5.spock;

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.util.*;

import org.apache.tapestry5.ioc.annotations.*;

/**
 * Facilitates <a href="https://spockframework.org/">Spock</a>-based integration testing
 * of Tapestry modules. Spock is a testing and specification framework for Java and Groovy
 * applications.
 * 
 * Supports injection of Tapestry services into Spock specifications.
 * 
 * <p><b>Usage example:</b>
 *
 * <pre>
 * &#64;ImportModule(UniverseModule)
 * class UniverseSpec extends Specification {
 * 
 *   &#64;Inject
 *   UniverseService service
 *
 *   UniverseService copy = service
 *
 *   def "service knows the answer to the universe"() {
 *     expect:
 *     copy == service        // injection occurred before 'copy' was initialized
 *     service.answer() == 42 // what else did you expect?!
 *   }
 * }
 * </pre>
 * 
 * <p><code>&#64;ImportModule</code> indicates which Tapestry module(s) should be started
 *  (and subsequently shut down). The deprecated <code>&#64;SubModule</code> annotation
 *  is still supported for compatibility reasons.
 *  
 * <p><code>&#64;Inject</code> marks fields which should be injected with a Tapestry service or
 * symbol. Related Tapestry annotations, such as <code>&#64;Service</code> and <code>&#64;Symbol</code>,
 * are also supported.
 * 
 * <p>Note: Only field (and no constructor) injection is supported.
 * 
 * <p>To interact directly with the Tapestry registry, an injection point of type
 * <code>ObjectLocator</code> may be defined. However, this should be rarely needed.
 *
 * <p>For every specification annotated with <code>&#64;ImportModule</code> or
 * <code>&#64;SubModule</code>, the Tapestry registry will be started up (and subsequently shut down)
 * once. Because fields are injected <em>before</em> field initializers and the <code>setup()</code>/
 * <code>setupSpec()</code> methods are run, they can be safely accessed from these places.
 *
 * <p>Fields marked as <code>&#64;Shared</code> are injected once per specification; regular
 * fields once per feature (iteration). However, this does <em>not</em> mean that each
 * feature will receive a fresh service instance; rather, it is left to the Tapestry
 * registry to control the lifecycle of a service. Most Tapestry services use the default
 * "singleton" scope, which results in the same service instance being shared between all
 * features.
 *
 * <p>Features that require their own service instance(s) should be moved into separate
 * specifications. To avoid code fragmentation and duplication, you might want to put
 * multiple (micro-)specifications into the same source file, and factor out their
 * commonalities into a base class.
 *
 *
 * @author Peter Niederwieser
 */
public class TapestrySpockExtension extends AbstractGlobalExtension
{

    // since Tapestry 5.4
    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation> importModuleAnnotation =
        (Class)ReflectionUtil.loadClassIfAvailable("org.apache.tapestry5.ioc.annotations.ImportModule");

    // deprecated as of Tapestry 5.4
    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation> submoduleAnnotation =
        (Class)ReflectionUtil.loadClassIfAvailable("org.apache.tapestry5.ioc.annotations.SubModule");

    @Override
    public void visitSpec(final SpecInfo spec)
    {
        Set<Class<?>> modules = collectModules(spec);
        if (modules == null) return;

        IMethodInterceptor interceptor = new TapestryInterceptor(spec, modules);
        spec.addSharedInitializerInterceptor(interceptor);
        spec.addInitializerInterceptor(interceptor);
        spec.addCleanupSpecInterceptor(interceptor);
    }

    // Returns null if no ImportModule or SubModule annotation was found.
    // Returns an empty list if one or more ImportModule or SubModule annotations were found,
    // but they didn't specify any modules. This distinction is important to
    // allow activation of the extension w/o specifying any modules.
    private Set<Class<?>> collectModules(SpecInfo spec)
    {
        Set<Class<?>> modules = null;

        for (SpecInfo curr : spec.getSpecsTopToBottom())
        {
            if (importModuleAnnotation != null && spec.isAnnotationPresent(importModuleAnnotation))
            {
                ImportModule importModule = curr.getAnnotation(ImportModule.class);
                if (importModule != null)
                {
                    if (modules == null) { modules = new HashSet<>(); }
                    modules.addAll(Arrays.<Class<?>>asList(importModule.value()));
                }
            }
            
            // Support for deprecated @SubModule.
            if (submoduleAnnotation != null && spec.isAnnotationPresent(submoduleAnnotation))
            {
                @SuppressWarnings("deprecation")
                SubModule subModule = curr.getAnnotation(SubModule.class);
                if (subModule != null)
                {
                    if (modules == null) { modules = new HashSet<>(); }
                    modules.addAll(Arrays.<Class<?>>asList(subModule.value()));
                }
            }
        }

        return modules;
    }

}
