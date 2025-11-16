package t5build

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class SshExec extends SshTask {

    @Input
    List<String[]> commandLines = []

    void commandLine(String... commandLine) {
        commandLines << commandLine
    }

    @TaskAction
    void doActions() {
        commandLines.each { commandLine ->
            ssh(*commandLine)
        }
    }
}