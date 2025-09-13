package t5build

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.File

class Scp extends SshTask {

    @InputFiles @SkipWhenEmpty
    def source

    @Input
    String destination

    @Input
    boolean isDir = false

    @TaskAction
    void doActions() {
        if (isDir) {
            scpDir(source, destination)
            return
        }
        project.files(source).each { doFile(it) }
    }

    private void doFile(File file) {
        if (file.isDirectory()) {
            file.eachFile { doFile(it) }
        } else {
            scpFile(file, destination)
        }
    }
}