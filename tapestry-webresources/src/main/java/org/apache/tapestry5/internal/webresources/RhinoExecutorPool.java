package org.apache.tapestry5.internal.webresources;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.ExceptionUtils;
import org.apache.tapestry5.ioc.util.Stack;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * Manages a pool of initialized {@link RhinoExecutor} instances.  The instances are initialized for a particular
 */
public class RhinoExecutorPool
{
    private final OperationTracker tracker;

    private final List<Resource> scripts;

    private final Stack<RhinoExecutor> executors = CollectionFactory.newStack();

    private final ContextFactory contextFactory = new ContextFactory();

    public RhinoExecutorPool(OperationTracker tracker, List<Resource> scripts)
    {
        this.tracker = tracker;
        this.scripts = scripts;
    }

    /**
     * Gets or creates an available executor. It is expected that {@link #put(RhinoExecutor)} will
     * be invoked after the executor completes.
     *
     * @return executor
     */
    public synchronized RhinoExecutor get()
    {

        if (executors.isEmpty())
        {
            return createExecutor();
        }

        return executors.pop();
    }

    private synchronized void put(RhinoExecutor executor)
    {
        executors.push(executor);
    }

    private RhinoExecutor createExecutor()
    {
        return tracker.invoke(String.format("Creating Rhino executor for source(s) %s.",
                InternalUtils.join(scripts)),
                new Invokable<RhinoExecutor>()
                {
                    public RhinoExecutor invoke()
                    {
                        final Context context = contextFactory.enterContext();

                        final ScriptableObject scope = context.initStandardObjects();

                        try
                        {
                            context.setOptimizationLevel(-1);

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
                    public void run()
                    {
                        InputStream in = null;
                        Reader r = null;

                        try
                        {
                            in = script.openStream();
                            r = new InputStreamReader(in);

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
