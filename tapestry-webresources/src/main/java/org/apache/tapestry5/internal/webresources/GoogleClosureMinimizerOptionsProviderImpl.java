package org.apache.tapestry5.internal.webresources;

import java.util.Optional;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.webresources.GoogleClosureMinimizerOptionsProvider;
import org.apache.tapestry5.webresources.WebResourcesSymbols;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticGroups;

public class GoogleClosureMinimizerOptionsProviderImpl implements GoogleClosureMinimizerOptionsProvider
{

    private CompilationLevel compilationLevel;

    public GoogleClosureMinimizerOptionsProviderImpl(@Symbol(WebResourcesSymbols.COMPILATION_LEVEL)
                                                     CompilationLevel compilationLevel)
    {
        this.compilationLevel = compilationLevel;
    }

    @Override
    public Optional<CompilerOptions> providerOptions(StreamableResource resource)
    {
        CompilerOptions options = new CompilerOptions();

        options.setCodingConvention(new ClosureCodingConvention());
        options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.WARNING);

        compilationLevel.setOptionsForCompilationLevel(options);

        return Optional.of(options);
    }

}
