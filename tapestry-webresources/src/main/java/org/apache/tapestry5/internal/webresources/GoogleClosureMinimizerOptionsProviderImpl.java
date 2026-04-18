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

import java.util.Optional;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.webresources.GoogleClosureMinimizerOptionsProvider;
import org.apache.tapestry5.webresources.WebResourcesSymbols;

import com.google.javascript.jscomp.BlackHoleErrorManager;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
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
        options.setErrorHandler(new BlackHoleErrorManager());

        return Optional.of(options);
    }
}
