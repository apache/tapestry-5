package t5build

import org.gradle.api.*
import org.gradle.api.tasks.*

/**
 * Used by the tapestry-core module, to split t5-core-dom into two flavors: one for prototype, one for jQuery.
 */
class PreprocessCoffeeScript extends DefaultTask {

    {
        description = "Splits CoffeeScript source files into multiple flavors."
        group = "build"
    }

    def flavors = ["prototype", "jquery"]
    def srcDir = "src/main/preprocessed-coffeescript"
    def outputDir = "${project.buildDir}/postprocessed-coffeescript"

    @InputDirectory
    File getSrcDir() { project.file(srcDir) }

    @OutputDirectory
    File getOutputDir() { project.file(outputDir) }

    @TaskAction
    void doSplit() {
        logger.info "Splitting CoffeeScript sources from $srcDir into $outputDir ($flavors)"

        def outputDirFile = getOutputDir()
        // Recursively delete output directory if it exists
        outputDirFile.deleteDir()

        def tree = project.fileTree srcDir, {
            include '**/*.coffee'
        }

        tree.visit { visit ->
            if (visit.directory) return

            def inputFile = visit.file
            def inputPath = visit.path

            flavors.each { flavor ->

                def dotx = inputPath.lastIndexOf "."

                def outputPath = inputPath.substring(0, dotx) + "-${flavor}.coffee"

                def outputFile = new File(outputDirFile, outputPath)

                logger.info "Generating ${outputPath} from ${inputPath}"

                outputFile.parentFile.mkdirs()

                split inputFile, outputFile, flavor
            }
        }
    }

    // Very sloppy; doesn't actually differentiate between #if and #elseif (nesting is not actually
    // supported). Some more C Macro support would be nice, too.
    def ifPattern = ~/^#(else)?if\s+(\w+)$/

    void split(File inputFile, File outputFile, String flavor) {

        def ignoring = false

        outputFile.withPrintWriter { pw ->

            inputFile.eachLine { line ->

                def matcher = ifPattern.matcher line

                if (matcher.matches()) {

                    // ignore the block unless it matches the flavor
                    ignoring = matcher[0][2] != flavor

                    // And don't copy the "#if" line at all.
                    return;
                }

                // Note that we don't check for nested #if, and those aren't supported.

                if (line == "#endif") {
                    ignoring = false;
                    return;
                }

                if (ignoring) {
                    return;
                }

                // Copy the line to the output:

                pw.println line
            }
        }
    }
}
