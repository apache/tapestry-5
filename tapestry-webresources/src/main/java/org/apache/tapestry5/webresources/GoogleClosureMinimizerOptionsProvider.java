package org.apache.tapestry5.webresources;

import java.util.Optional;

import org.apache.tapestry5.services.assets.StreamableResource;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;

/**
 * Provide CompilerOptions for the GoogleClosureMinimizer.
 * TAP5-2661
 *
 * @since 5.7
 */
public interface GoogleClosureMinimizerOptionsProvider
{
    /**
     * Returns the compiler options to be used by GoogleClosureMinimizer.
     * 
     * An empty Optional will result is the StreamableResource to be not minimized.
     * @return Optional of the supposed compiler options, or empty to disable minimizer
     */
    Optional<CompilerOptions> providerOptions(StreamableResource resource);
}
