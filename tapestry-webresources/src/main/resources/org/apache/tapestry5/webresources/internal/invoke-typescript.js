//https://github.com/Microsoft/TypeScript/wiki/Using-the-Compiler-API
function transpile(fileNames) {
  var compilerOptions = {
    noEmitOnError : true,
    emitDecoratorMetadata: true,
    experimentalDecorators: true,
    newLine: 'LF',
    noImplicitAny : true,
    target : ts.ScriptTarget.ES5,
    module : "AMD"
  };
  
  var transpileOptions = {
    compilerOptions : compilerOptions,
    reportDiagnostics: false
  };
  
  var result = ts.transpileModule(fileNames, transpileOptions);

  return {
    output : result.outputText
  };

}
