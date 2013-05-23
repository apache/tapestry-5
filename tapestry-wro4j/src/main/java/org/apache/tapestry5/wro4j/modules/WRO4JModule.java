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

package org.apache.tapestry5.wro4j.modules;

import com.github.sommeri.less4j.core.parser.AntlrException;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.wro4j.*;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ObjectRenderer;
import org.apache.tapestry5.services.assets.ResourceMinimizer;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.apache.tapestry5.wro4j.services.ResourceProcessorSource;
import ro.isdc.wro.extensions.processor.css.Less4jProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.support.coffeescript.CoffeeScript;
import ro.isdc.wro.model.resource.processor.impl.css.CssCompressorProcessor;

import java.util.List;

/**
 * Configures CoffeeScript-to-JavaScript compilation.
 *
 * @since 5.4
 */
public class WRO4JModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(ResourceProcessorSource.class, ResourceProcessorSourceImpl.class);
        binder.bind(ResourceTransformerFactory.class, ResourceTransformerFactoryImpl.class);
    }

    /**
     * Configures the default set of processors.
     * <dl>
     * <dt>CoffeeScriptCompiler</dt> <dd>{@link RhinoCoffeeScriptProcessor}, configured as with --bare</dd>
     * <dt>CSSMinimizer</dt> <dd>{@link CssCompressorProcessor} (see <a href="https://github.com/andyroberts/csscompressor">csscompressor on GitHub</a></dd>
     * <dt>JavaScriptMinimizer</dt>
     * <dd>{@link GoogleClosureCompressorProcessor} configured for simple optimizations. Advanced optimizations assume that all code is loaded
     * in a single bundle, not a given for Tapestry.</dd>
     * <dt>LessCompiler</dt> <dd>Compiles Less source files into CSS.</dd>
     * </dl>
     *
     * @param configuration
     * @param locator
     */
    @Contribute(ResourceProcessorSource.class)
    public static void provideDefaultProcessors(MappedConfiguration<String, ObjectCreator> configuration, final ObjectLocator locator)
    {
        configuration.add("CoffeeScriptCompiler",
                new ObjectCreator()
                {
                    public Object createObject()
                    {
                        return new RhinoCoffeeScriptProcessor()
                        {
                            @Override
                            protected CoffeeScript newCoffeeScript()
                            {
                                return new CoffeeScript().setOptions("bare");
                            }
                        };
                    }
                }
        );


        configuration.add("CSSMinimizer", new ObjectCreator()
        {
            public Object createObject()
            {
                return new CssCompressorProcessor();
            }
        });

        configuration.add("JavaScriptMinimizer", new ObjectCreator()
        {
            public Object createObject()
            {
                return new GoogleClosureCompressorProcessor();
            }
        });

        configuration.add("LessCompiler", new ObjectCreator()
        {
            public Object createObject()
            {
                return new Less4jProcessor();
            }
        });
    }

    @Contribute(StreamableResourceSource.class)
    public static void provideCompilations
            (MappedConfiguration<String, ResourceTransformer> configuration, ResourceTransformerFactory factory)
    {
        configuration.add("coffee",
                factory.createCompiler("text/javascript", "CoffeeScriptCompiler", "CoffeeScript", "JavaScript"));

        configuration.add("less", factory.createCompiler("text/css", "LessCompiler", "Less", "CSS"));
    }

    @Contribute(ResourceMinimizer.class)
    @Primary
    public static void setupDefaultResourceMinimizers(MappedConfiguration<String, ResourceMinimizer> configuration)
    {
        configuration.addInstance("text/css", CSSMinimizer.class);
        configuration.addInstance("text/javascript", JavaScriptMinimizer.class);
    }

    @Contribute(ObjectRenderer.class)
    @Primary
    public static void decodeLessErrors(MappedConfiguration<Class, ObjectRenderer> configuration)
    {
        configuration.add(AntlrException.class, new ObjectRenderer<AntlrException>()
        {
            public void render(AntlrException e, MarkupWriter writer)
            {
                List<String> strings = CollectionFactory.newList();

                if (InternalUtils.isNonBlank(e.getMessage()))
                {
                    strings.add(e.getMessage());
                }

                // Inside WRO4J we see that the LessSource is a StringSource with no useful toString(), so
                // it is omitted. We may need to create our own processors, stripping away a couple of layers of
                // WRO4J to get proper exception reporting!

                if (e.getLine() > 0)
                {
                    strings.add("line " + e.getLine());
                }

                if (e.getCharacter() > 0)
                {
                    strings.add("position " + e.getCharacter());
                }

                writer.write(InternalUtils.join(strings, " - "));
            }
        });
    }
}
