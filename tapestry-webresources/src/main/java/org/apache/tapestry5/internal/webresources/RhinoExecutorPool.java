// Copyright 2013-2014 The Apache Software Foundation
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

package org.apache.tapestry5.internal.webresources;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages a pool of initialized {@link RhinoExecutor} instances.  The instances are initialized for a particular
 */
public class RhinoExecutorPool
{
    private final OperationTracker tracker;

    private final List<Resource> scripts;

    private final Queue<RhinoExecutor> executors = new ConcurrentLinkedQueue<RhinoExecutor>();

    private final ContextFactory contextFactory = new ContextFactory();

    private final int languageVersion;

    public RhinoExecutorPool(OperationTracker tracker, List<Resource> scripts)
    {
        this(tracker, scripts, Context.VERSION_DEFAULT);
    }

    public RhinoExecutorPool(OperationTracker tracker, List<Resource> scripts, int languageVersion)
    {
        this.tracker = tracker;
        this.scripts = scripts;
        this.languageVersion = languageVersion;
    }

    /**
     * Gets or creates an available executor. It is expected that {@link #put(RhinoExecutor)} will
     * be invoked after the executor completes.
     *
     * @return executor
     */
    public RhinoExecutor get()
    {

        RhinoExecutor executor = executors.poll();
        if (executor != null)
        {
            return executor;
        }

        return createExecutor();
    }

    private void put(RhinoExecutor executor)
    {
        executors.add(executor);
    }

    private RhinoExecutor createExecutor()
    {
        return tracker.invoke(String.format("Creating Rhino executor for source(s) %s.",
                InternalUtils.join(scripts)),
                new Invokable<RhinoExecutor>()
                {
                    @Override
                    public RhinoExecutor invoke()
                    {
                        final Context context = contextFactory.enterContext();

                        final ScriptableObject scope = context.initStandardObjects();

                        try
                        {
                            context.setOptimizationLevel(-1);
                            context.setLanguageVersion(languageVersion);

                            for (Resource script : scripts)
                            {
                                loadScript(context, scope, script);
                            }

                        } finally
                        {
                            Context.exit();
                        }

                        return new RhinoExecutor()
                        {
                            @Override
                            public ScriptableObject invokeFunction(String functionName, Object... arguments)
                            {
                                contextFactory.enterContext(context);

                                try
                                {
                                    NativeFunction function = (NativeFunction) scope.get(functionName, scope);

                                    return (ScriptableObject) function.call(context, scope, null, arguments);
                                } finally
                                {
                                    Context.exit();
                                }
                            }

                            @Override
                            public void discard()
                            {
                                put(this);
                            }
                        };
                    }
                });
    }

    private void loadScript(final Context context, final ScriptableObject scope, final Resource script)
    {
        tracker.run(String.format("Loading script %s.", script),
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        InputStream in = null;
                        Reader r = null;

                        try
                        {
                            in = script.openStream();
                            r = new InputStreamReader(in, StandardCharsets.UTF_8);

                            context.evaluateReader(scope, r, script.toString(), 1, null);
                        } catch (IOException ex)
                        {
                            throw new RuntimeException(String.format("Unable to read script %s: %s",
                                    script,
                                    ExceptionUtils.toMessage(ex)
                            ), ex);
                        } finally
                        {
                            InternalUtils.close(r);
                            InternalUtils.close(in);
                        }
                    }
                });

    }


}
