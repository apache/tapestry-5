package org.apache.tapestry5.internal.webresources;

import java.util.Optional;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.webresources.GoogleClosureMinimizerOptionsProvider;
import org.apache.tapestry5.webresources.WebResourcesSymbols;

import com.google.javascript.jscomp.BlackHoleErrorManager;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DependencyOptions;
import com.google.javascript.jscomp.DiagnosticGroup;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.deps.ModuleLoader;

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

        compilationLevel.setOptionsForCompilationLevel(options);

        options.setLanguageIn(CompilerOptions.LanguageMode.UNSTABLE);
        options.setWarningLevel(DiagnosticGroup.forType(ModuleLoader.INVALID_MODULE_PATH), CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.NON_STANDARD_JSDOC, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.PARSING, CheckLevel.OFF);
        options.setModuleResolutionMode(ModuleLoader.ResolutionMode.BROWSER);
        options.setDependencyOptions(DependencyOptions.none());
        options.setCodingConvention(new ClosureCodingConvention());

        options.setErrorHandler(new BlackHoleErrorManager());

        return Optional.of(options);
    }
}
