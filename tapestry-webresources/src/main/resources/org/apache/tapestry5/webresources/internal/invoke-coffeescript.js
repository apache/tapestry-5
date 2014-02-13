// Compiles CoffeeScript source to JavaScript.
//
// input - string containing contents of the file
// filename - name of file, used to report errors
//
// Returns { output: <compiled JavaScript> } or { exception: <exception message> }
function compileCoffeeScriptSource(input, filename) {
    try {
        return { output: CoffeeScript.compile(input, {header: true, filename: filename}) };
    }
    catch (err) {
        return { exception: err.toString() };
    }
}
