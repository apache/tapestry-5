package t5build

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.SourceTask
import org.gradle.api.file.ConfigurableFileCollection

import java.security.MessageDigest

class GenerateChecksums extends SourceTask {

    enum Algorithm {
        MD5('MD5', 32, 'md5'),
        SHA256('SHA-256', 64, 'sha256')

        final String name
        final int padding
        final String extension

        Algorithm(String name, int padding, String extension) {
            this.name = name
            this.padding = padding
            this.extension = extension
        }
    }

    @OutputDirectory
    File outputDir


    @TaskAction
    void generate() {
        source.each { file ->
            // Create a map of MessageDigest instances, one for each algorithm,
            // so it's easier to update them and use later
            def digests = Algorithm.values().collectEntries { alg ->
                [(alg): MessageDigest.getInstance(alg.name)]
            }

            // use inputstream so to avoid loading whole file into memory
            file.withInputStream { is ->
                byte[] buffer = new byte[8192]
                int bytesRead
                while ((bytesRead = is.read(buffer)) != -1) {
                    digests.values().each { digest ->
                        digest.update(buffer, 0, bytesRead)
                    }
                }
            }

            // Write checksum files
            digests.each { algo, digest ->
                def checksum = new BigInteger(1, digest.digest()).toString(16).padLeft(algo.padding, '0')
                new File(outputDir, "${file.name}.${algo.extension}").text = checksum
            }
        }
    }
}
