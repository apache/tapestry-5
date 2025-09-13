package t5build

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal

abstract class SshTask extends DefaultTask {

    @InputFiles
    FileCollection sshAntClasspath

    @Input
    String host

    @Input
    String userName

    // TODO: Passwords should not be plain @Input.
    @Input
    String password

    @Input
    boolean verbose = false

    private boolean antInited = false

    protected void initAnt() {
        if (antInited) {
            return
        }
        ant.taskdef(name: 'scp',
                    classname: 'org.apache.tools.ant.taskdefs.optional.ssh.Scp',
                    classpath: sshAntClasspath.asPath,
                    loaderref: 'ssh')

        ant.taskdef(name: 'sshexec',
                    classname: 'org.apache.tools.ant.taskdefs.optional.ssh.SSHExec',
                    classpath: sshAntClasspath.asPath,
                    loaderref: 'ssh')
        antInited = true
    }

    protected void withInfoLogging(Closure action) {
        def oldLogLevel = getLogging().getLevel()
        getLogging().setLevel([LogLevel.INFO, oldLogLevel].min())
        try {
            action()
        } finally {
            if (oldLogLevel != null) {
                getLogging().setLevel(oldLogLevel)
            }
        }
    }

    protected void scpFile(Object source, String destination) {
        initAnt()
        withInfoLogging {
            // TODO: This keyfile is hardcoded and uses an old algorithm (dsa)
            ant.scp(localFile: project.files(source).singleFile,
                    remoteToFile: "${userName}@${host}:${destination}",
                    keyfile: "${System.properties['user.home']}/.ssh/id_dsa",
                    verbose: verbose)
        }
    }

    protected void scpDir(Object source, String destination) {
        initAnt()
        withInfoLogging {
            ant.sshexec(host: host,
                        username: userName,
                        password: password,
                        command: "mkdir -p ${destination}")

            ant.scp(remoteTodir: "${userName}@${host}:${destination}",
                    keyfile: "${System.properties['user.home']}/.ssh/id_dsa",
                    verbose: verbose) {
                project.files(source).addToAntBuilder(ant, 'fileSet', FileCollection.AntType.FileSet)
            }
        }
    }

    protected void ssh(Object... commandLine) {
        initAnt()
        withInfoLogging {
            ant.sshexec(host: host,
                        username: userName,
                        password: password,
                        command: commandLine.join(' '))
        }
    }
}
