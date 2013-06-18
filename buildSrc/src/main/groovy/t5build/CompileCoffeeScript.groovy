package t5build

import ro.isdc.wro.model.resource.*
import ro.isdc.wro.extensions.processor.js.*
import ro.isdc.wro.extensions.processor.support.coffeescript.*
import org.gradle.api.*
import org.gradle.api.tasks.*

class CompileCoffeeScript extends DefaultTask {

    {
        description = "Compiles CoffeeScript sources into JavaScript"
        group = "build"
    }

    def srcDir = "src/main/coffeescript"

    def outputDir = "${project.buildDir}/compiled-coffeescript"

    @InputDirectory
    File getSrcDir() { project.file(srcDir) }

    @OutputDirectory
    File getOutputDir() { project.file(outputDir) }

    @TaskAction
    void doCompile() {
        logger.info "Compiling CoffeeScript sources from $srcDir into $outputDir"

        def outputDirFile = getOutputDir()
        // Recursively delete output directory if it exists
        outputDirFile.deleteDir()

        def tree = project.fileTree srcDir, {
            include '**/*.coffee'
        }

        def processor = new RhinoCoffeeScriptProcessor()

        tree.visit { visit ->
            if (visit.directory) return

            def inputFile = visit.file
            def inputPath = visit.path
            def outputPath = inputPath.replaceAll(/\.coffee$/, '.js')
            def outputFile = new File(outputDirFile, outputPath)

            logger.info "Compiling ${inputPath}"

            outputFile.parentFile.mkdirs()

            def resource = Resource.create(inputFile.absolutePath, ResourceType.JS)

            processor.process(resource, inputFile.newReader(), outputFile.newWriter())
        }
    }

}
