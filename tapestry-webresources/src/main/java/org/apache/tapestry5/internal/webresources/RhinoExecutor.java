package org.apache.tapestry5.internal.webresources;


import org.mozilla.javascript.ScriptableObject;

public interface RhinoExecutor
{
    /**
     * Invokes the named function, which must return a scriptable object (typically, a JavaScript Object).
     *
     * @param functionName
     *         name of function visible to the executor's scope (e.g., loaded from the scripts associated
     *         with the executor).
     * @param arguments
     *         Arguments to pass to the object which must be convertable to JavaScript types; Strings work well here.
     * @return result of invoking the function.
     */
    ScriptableObject invokeFunction(String functionName, Object... arguments);

    /**
     * Discards the executor, returning it to the pool for reuse.
     */
    void discard();
}
